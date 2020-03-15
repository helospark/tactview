package com.helospark.tactview.ui.javafx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        List<MenuElement> menuElements = createTree();
        return buildMenuBar(menuElements);
    }

    private List<MenuElement> createTree() {
        List<MenuElement> menuElements = new ArrayList<>();

        for (MenuContribution contribution : menuContributions) {
            MenuElement menuElement = null;
            List<String> path = contribution.getPath();
            for (int i = 0; i < path.size() - 1; ++i) {
                String currentPathName = path.get(i);
                List<MenuElement> children = menuElement == null ? menuElements : menuElement.children;
                menuElement = addOrGetIntermediateElementFor(children, currentPathName);
            }
            String leafMenuItem = path.get(path.size() - 1);
            if (contribution instanceof SelectableMenuContribution) {
                menuElement.children.add(new LeafMenuItem(leafMenuItem, (SelectableMenuContribution) contribution));
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
            }
        }

        return menuBar;
    }

    private MenuItem recursivelyCreateMenuItems(MenuElement element) {
        if (element.children.size() > 0) {
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

    static class SeparatorMenuElement extends MenuElement {

    }

}
