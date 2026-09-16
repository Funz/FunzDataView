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
 */
package org.asnr.funz.data.view;

/**
 * Lets the host application (which owns the light/dark/high-contrast preference) tell the embedded HTML/ace.js
 * viewers which colors to use, since this module has no notion of the host's own Swing/JavaFX theme.
 *
 * @author Nicolas CHabalier - Artenum SARL
 */
public final class AceEditorTheme {

    /**
     * Represents the color mode the embedded viewers should use.
     */
    public enum Mode {

        /**
         * Light background, dark text.
         */
        LIGHT,

        /**
         * Dark background, light text.
         */
        DARK,

        /**
         * Pure black background, pure white text.
         */
        HIGH_CONTRAST
    }

    private static volatile Mode mode = Mode.LIGHT;

    private AceEditorTheme() {
        // Suppress default constructor for noninstantiability.
        throw new AssertionError();
    }

    /**
     * @param newMode
     *         the color mode the host application currently uses
     */
    public static void setMode(final Mode newMode) {
        AceEditorTheme.mode = newMode;
    }

    /**
     * @return the color mode the embedded viewers should use
     */
    public static Mode getMode() {
        return AceEditorTheme.mode;
    }

    /**
     * @return {@code true} if the embedded viewers should use a dark background (either {@link Mode#DARK} or
     *         {@link Mode#HIGH_CONTRAST})
     */
    public static boolean isDark() {
        return AceEditorTheme.mode != Mode.LIGHT;
    }
}
