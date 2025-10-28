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
package org.asnr.funz.data.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.keridwen.core.settings.ApplicationSettings;
import org.mozilla.universalchardet.ReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to centralise the usage of various variable in extensions HTML.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
public final class HtmlVariablesUtils {

    private static final Logger logger = LoggerFactory.getLogger(HtmlVariablesUtils.class);

    /**
     * Prefix of all Application Settings concerning the HTML custom renderers.
     */
    private static final String PREFIX = "org.asnr.funz.dataview.html.";

    /**
     * @param template
     *         the file containing the HTML code for custom rendering
     * @return the content of the file
     */
    public static String getTemplateContent(final File template) {
        try (final BufferedReader reader = ReaderFactory.createBufferedReader(template)) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (final IOException e) {
            HtmlVariablesUtils.logger.warn("Unable to load dynamic renderer '{}': {}", template.getName(),
                    e.getMessage());
            HtmlVariablesUtils.logger.debug(e.getMessage(), e);
            return "";
        }
    }

    /**
     * From the content of the HTML file containing variables, replace them with the correct substitutions and return
     * the final content to be displayed
     *
     * @param input
     *         the input HTML content
     * @param substitutions
     *         the substitutions to be used to replace variables with the correct value
     * @return the content to be shown inside webview
     */
    public static String replaceVariables(final String input, final Map<Variable, Supplier<String>> substitutions) {
        String result = input;
        for (final Entry<Variable, Supplier<String>> entry : substitutions.entrySet()) {
            result = result.replace(entry.getKey().value, entry.getValue().get());
        }

        // "Handmade" lib folder
        result = result.replace(Variable.LIB.value, new File("lib").getAbsolutePath());

        return result;
    }

    /**
     * Represents the various locations possible for custom HTML renderer.
     *
     * @author Arnaud Trouche - ARTENUM SARL
     */
    public enum Location {

        /**
         * Location for custom file renderers.
         */
        EXTENSIONS("extensions"),

        /**
         * Location for custom Data renderer for discrete cases.
         */
        VIEWS("views"),

        /**
         * Location for custom Data renderer for DOE cases.
         */
        ANALYSIS("analysis");

        private static final String SUFFIX = ".location";

        private final String property;
        private final File folder;

        Location(final String prop) {
            this.property = prop;
            this.folder = ApplicationSettings.getPropertyAsFile(this.getProperty());
        }

        private String getProperty() {
            return HtmlVariablesUtils.PREFIX + this.property + Location.SUFFIX;
        }

        /**
         * @return the folder containing the various HTML files.
         */
        public File getFolder() {
            return this.folder;
        }

    }

    /**
     * Represent a variable that can be present in HTML files.<br>
     * Then, they are replaced inside {@link HtmlVariablesUtils#replaceVariables(String, Map)} method.
     *
     * @author Arnaud Trouche - ARTENUM SARL
     */
    @SuppressWarnings("MissingJavadoc")
    public enum Variable {
        CONTENT("$$CONTENT$$"),
        BASE("__BASE__"),
        DIR("$$DIR$$"),
        FILENAME("$$FILENAME$$"),
        LIB("$$LIB$$"),
        NAME("$$NAME$$"),
        PATH("$$PATH$$"),
        PARAMETERS("$$X$$"),
        VALUES("$$Y$$");

        private final String value;

        Variable(final String value) {
            this.value = value;
        }

        /**
         * @return the value of the variable
         */
        public String getValue() {
            return this.value;
        }

    }

    private HtmlVariablesUtils() {
        // Suppress default constructor for noninstantiability.
        throw new AssertionError();
    }
}
