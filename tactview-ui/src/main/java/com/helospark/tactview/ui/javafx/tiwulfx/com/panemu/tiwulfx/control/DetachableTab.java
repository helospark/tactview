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

import java.util.Objects;

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
    private String id;

    public DetachableTab(String id) {
        this.id = id;
        super.setId(id);
    }

    public DetachableTab(String text, Node content, String id) {
        super(text, content);
        this.id = id;
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

    public String getTabId() {
        return id;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof DetachableTab)) {
            return false;
        }
        DetachableTab castOther = (DetachableTab) other;
        return Objects.equals(id, castOther.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
