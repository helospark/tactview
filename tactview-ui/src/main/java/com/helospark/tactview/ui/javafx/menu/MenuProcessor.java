package com.helospark.tactview.ui.javafx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

@Component
public class MenuProcessor {
    private List<MenuContribution> menuContributions;

    public MenuProcessor(List<MenuContribution> menuContributions) {
        this.menuContributions = menuContributions;
    }

    public MenuBar createMenuBar() {
        List<MenuElement> menuElements = createTree(menuContributions);
        return buildMenuBar(menuElements);
    }

    private List<MenuElement> createTree(List<MenuContribution> allContributions) {
        return createTreeWithParent(allContributions, null);
    }

    private List<MenuElement> createTreeWithParent(List<MenuContribution> allContributions, MenuElement parent) {
        List<MenuElement> menuElements = new ArrayList<>();

        for (MenuContribution contribution : allContributions) {
            MenuElement menuElement = null;
            List<String> path = contribution.getPath();

            if (parent == null) {
                for (int i = 0; i < path.size() - 1; ++i) {
                    String currentPathName = path.get(i);
                    List<MenuElement> children = menuElement == null ? menuElements : menuElement.children;
                    menuElement = addOrGetIntermediateElementFor(children, currentPathName);
                }
            } else {
                menuElement = parent;
            }

            String leafMenuItem = path.get(path.size() - 1);
            if (contribution instanceof SelectableMenuContribution) {
                menuElement.children.add(new LeafMenuItem(leafMenuItem, (SelectableMenuContribution) contribution));
            } else if (contribution instanceof DynamicallyGeneratedParentMenuContribution) {
                menuElement.children.add(new DynamicMenuItem(leafMenuItem, (DynamicallyGeneratedParentMenuContribution) contribution));
            } else {
                menuElement.children.add(new SeparatorMenuElement());
            }
        }
        return menuElements;
    }

    private MenuBar buildMenuBar(List<MenuElement> menuElements) {
        MenuBar menuBar = new MenuBar();

        for (MenuElement element : menuElements) {
            MenuItem menuItem = recursivelyCreateMenuItems(element);
            if (menuItem instanceof Menu) {
                menuBar.getMenus().add((Menu) menuItem);

                ((Menu) menuItem).showingProperty().addListener(e -> {
                    regenerateDynamicMenuItemContents(menuItem);
                });
            }
        }

        return menuBar;
    }

    private void regenerateDynamicMenuItemContents(MenuItem menuItem) {
        for (MenuItem menu : ((Menu) menuItem).getItems()) {
            if (menu instanceof Menu && menu.getUserData() != null && menu.getUserData() instanceof DynamicMenuItem) {
                List<MenuContribution> children = ((DynamicMenuItem) menu.getUserData()).dynamicMenuContribution.getChildren();
                MenuElement tmpMenuElement = new IntermediateMenuItem("tmp");
                createTreeWithParent(children, tmpMenuElement);
                List<MenuItem> el = tmpMenuElement.children.stream()
                        .map(a -> recursivelyCreateMenuItems(a))
                        .collect(Collectors.toList());

                ((Menu) menu).getItems().clear();
                ((Menu) menu).getItems().addAll(el);
            } else if (menu instanceof Menu) {
                regenerateDynamicMenuItemContents(menu);
            }
        }
    }

    private MenuItem recursivelyCreateMenuItems(MenuElement element) {
        if (element instanceof DynamicMenuItem) {
            Menu menu = new Menu(element.name);
            menu.setUserData(element);

            return menu;
        } else if (element.children.size() > 0) {
            Menu menu = new Menu(element.name);
            for (var child : element.children) {
                if (element.children.size() > 0) {
                    MenuItem childMenuItem = recursivelyCreateMenuItems(child);
                    menu.getItems().add(childMenuItem);
                }
            }
            return menu;
        } else if (element instanceof SeparatorMenuElement) {
            return new javafx.scene.control.SeparatorMenuItem();
        } else {
            MenuItem leafMenuItem = new MenuItem(element.name);
            leafMenuItem.setOnAction(e -> element.menuContribution.onAction(e));

            element.menuContribution.getAccelerator().ifPresent(key -> leafMenuItem.setAccelerator(key));

            return leafMenuItem;
        }
    }

    private MenuElement addOrGetIntermediateElementFor(List<MenuElement> menuElements, String currentPathName) {
        for (MenuElement element : menuElements) {
            if (Objects.equals(element.name, currentPathName)) {
                return element;
            }
        }

        MenuElement element = new IntermediateMenuItem(currentPathName);
        menuElements.add(element);

        return element;
    }

    static abstract class MenuElement {
        String name;
        SelectableMenuContribution menuContribution = null;
        List<MenuElement> children = new ArrayList<>();
    }

    static class IntermediateMenuItem extends MenuElement {

        public IntermediateMenuItem(String name) {
            this.name = name;
        }

    }

    static class LeafMenuItem extends MenuElement {

        public LeafMenuItem(String name, SelectableMenuContribution menuContribution) {
            this.name = name;
            this.menuContribution = menuContribution;
        }

    }

    static class DynamicMenuItem extends MenuElement {
        DynamicallyGeneratedParentMenuContribution dynamicMenuContribution;

        public DynamicMenuItem(String name, DynamicallyGeneratedParentMenuContribution menuContribution) {
            this.name = name;
            this.dynamicMenuContribution = menuContribution;
        }

    }

    static class SeparatorMenuElement extends MenuElement {

    }

}
