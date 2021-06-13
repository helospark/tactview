/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah .
 */
package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.common;

import javafx.scene.Scene;

/**
 *
 * @author Amrullah
 */
public class TiwulFXUtil {

    /**
     * Set default TiwulFX css style to a scene. The default is /com/panemu/tiwulfx/res/tiwulfx.css located inside tiwulfx jar file.
     *
     * @param scene
     */
    public static void setTiwulFXStyleSheet(Scene scene) {
        scene.getStylesheets().add("/com/panemu/tiwulfx/res/tiwulfx.css");
    }

}
