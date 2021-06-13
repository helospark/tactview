/*
 * Copyright (C) 2013 Panemu.
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 *
 * @author amrullah
 */
public class DetachableTab extends Tab {
    private BooleanProperty detachable = new SimpleBooleanProperty(true);

    public DetachableTab() {
        super();
    }

    public DetachableTab(String string) {
        super(string);
    }

    public DetachableTab(String text, Node content) {
        super(text, content);
    }

    public boolean isDetachable() {
        return detachable.get();
    }

    public void setDetachable(boolean detachable) {
        this.detachable.set(detachable);
    }

    public BooleanProperty detachableProperty() {
        return detachable;
    }

}
