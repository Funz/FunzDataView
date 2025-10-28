/*
 * Project        : FunzDataView
 * Website        : https://github.com/Funz/FunzDataView
 * Copyright      : © ASNR
 *                  31 Avenue de la Division Leclerc
 *                  92260 Fontenay-aux-Roses, France
 *                  https://www.asnr.fr
 * Licence        : cf. LICENSE
 * Developed By   : Artenum SARL
 * Authors        : Arnaud Trouche
 *                  Nicolas Chabalier
 *                  Julien Forest
 * Contract       : contract N°50000976 - order N°34007261
 */
package org.asnr.funz.data.controller;

import java.util.concurrent.Executors;

import javafx.application.Platform;

/**
 * Utility class that represent a controller that cannot know by itself if it on the screen or not and wants to update
 * its content only when shown on screen.<br/>
 * To do so, another controller should indicate with {@link #setShowing(boolean)} if the component is on the screen or
 * not.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
abstract class AbstractDynamicUpdateController {

    private Runnable updater;

    /**
     * Boolean indicating if the tree needs an update once it is shown on screen.
     */
    private boolean needUpdate = false;

    /**
     * Boolean indicating if the tree is shown on the screen.
     */
    private boolean isOnScreen = false;

    /**
     * <b>Must be called on FX Thread</b>
     *
     * @param isShowing
     *         whether the tab is showing or not
     */
    void setShowing(final boolean isShowing) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on FX thread");
        }

        this.isOnScreen = isShowing;
        if (this.isOnScreen && this.needUpdate) {
            this.needUpdate = false;
            Executors.defaultThreadFactory().newThread(this.updater).start();
        }
    }

    /**
     * Sets the callback that will be called when the view should be updated.
     *
     * @param toRun
     *         the method to run
     */
    protected void setCallback(final Runnable toRun) {
        this.updater = toRun;
    }

    /**
     * @return whether the component is on the screen
     */
    protected boolean isShowing() {
        return this.isOnScreen;
    }

    /**
     * Indicates that the component will need an update as soon as the component is showing
     */
    protected void needUpdate() {
        this.needUpdate = true;
    }
}
