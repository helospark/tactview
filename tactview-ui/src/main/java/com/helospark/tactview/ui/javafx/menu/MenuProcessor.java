package com.helospark.tactview.ui.javafx.menu;

import java.util.ArrayList;
import java.util.List;

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
            menuElement.children.add(new LeafMenuItem(leafMenuItem, contribution));
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
        } else {
            MenuItem leafMenuItem = new MenuItem(element.name);
            leafMenuItem.setOnAction(e -> element.menuContribution.onAction(e));
            return leafMenuItem;
        }
    }

    private MenuElement addOrGetIntermediateElementFor(List<MenuElement> menuElements, String currentPathName) {
        for (MenuElement element : menuElements) {
            if (element.name.equals(currentPathName)) {
                return element;
            }
        }

        MenuElement element = new IntermediateMenuItem(currentPathName);
        menuElements.add(element);

        return element;
    }

    static abstract class MenuElement {
        String name;
        MenuContribution menuContribution = null;
        List<MenuElement> children = new ArrayList<>();
    }

    static class IntermediateMenuItem extends MenuElement {

        public IntermediateMenuItem(String name) {
            this.name = name;
        }

    }

    static class LeafMenuItem extends MenuElement {

        public LeafMenuItem(String name, MenuContribution menuContribution) {
            this.name = name;
            this.menuContribution = menuContribution;
        }

    }

}
