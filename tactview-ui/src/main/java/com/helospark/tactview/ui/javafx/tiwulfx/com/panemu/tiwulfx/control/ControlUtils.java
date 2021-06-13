/*
 * Copyright (C) 2015 Panemu.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import javafx.geometry.Side;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;

/**
 *
 * @author amrullah
 */
class ControlUtils {

    public static void generateTabPath(Path path, double startX, double startY, double width, double height) {
        path.getElements().clear();
        MoveTo moveTo = new MoveTo();
        moveTo.setX(startX);
        moveTo.setY(startY);
        path.getElements().add(moveTo);//start
        path.getElements().add(new HLineTo(startX + width));//path width
        path.getElements().add(new VLineTo(startY + height));//path height
        path.getElements().add(new HLineTo(startX));//path bottom left
        path.getElements().add(new VLineTo(startY));//back to start
    }

    public static void generateTabPath(Path path, double tabPos, double width, double height, Side side) {
        int tabHeight = 28;
        int start = 2;
        tabPos = Math.max(start, tabPos);
        path.getElements().clear();
        MoveTo moveTo = new MoveTo();
        moveTo.setX(start);
        moveTo.setY(tabHeight);
        path.getElements().add(moveTo);//start

        path.getElements().add(new HLineTo(width));//path width
        path.getElements().add(new VLineTo(height));//path height
        path.getElements().add(new HLineTo(start));//path bottom left
        path.getElements().add(new VLineTo(tabHeight));//back to start

        if (side.equals(Side.TOP)) {
            if (tabPos > 20) {
                path.getElements().add(new MoveTo(tabPos, tabHeight + 20));
                path.getElements().add(new LineTo(Math.max(start, tabPos - 10), tabHeight + 15));
                path.getElements().add(new HLineTo(tabPos + 10));
                path.getElements().add(new LineTo(tabPos, tabHeight + 5));
            } else {
                double tip = Math.max(tabPos, start + 5);
                path.getElements().add(new MoveTo(tip, tabHeight + 5));
                path.getElements().add(new LineTo(tip + 10, tabHeight + 5));
                path.getElements().add(new LineTo(tip, tabHeight + 15));
                path.getElements().add(new VLineTo(tabHeight + 5));
            }
        } else {
            double distance = 10;
            if (tabPos > 20) {
                path.getElements().add(new MoveTo(tabPos, height - tabHeight - distance + 20));
                path.getElements().add(new LineTo(Math.max(start, tabPos - 10), height - distance - tabHeight + 15));
                path.getElements().add(new HLineTo(tabPos + 10));
                path.getElements().add(new LineTo(tabPos, height - tabHeight - distance + 20));
            } else {
                double tip = Math.max(tabPos, start + 5);
                path.getElements().add(new MoveTo(tip, height - tabHeight + 5));
                path.getElements().add(new LineTo(tip + 10, height - tabHeight + 5));
                path.getElements().add(new LineTo(tip, height - tabHeight + 15));
                path.getElements().add(new VLineTo(height - tabHeight + 5));
            }
        }
    }
}
