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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.asnr.funz.data.i18n.ResultsDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing an HTML template able to display files with specific extensions.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
public class HtmlFileViewer {

    private static final Logger logger = LoggerFactory.getLogger(HtmlFileViewer.class);

    /**
     * The HTML file used to load the file.
     */
    private final File templateFile;

    /**
     * The file content of the template to be loaded into the webview.
     */
    private final String templateContent;

    /**
     * List of the extensions handled by this template. <br>
     * If the list is empty, then this template is considered as <b>deactivated</b>
     */
    private final List<String> extensions;

    /**
     * Map of the functions to be used to replace the values of the variables with the real content.
     */
    private final Map<HtmlVariablesUtils.Variable, Supplier<String>> substitutions;

    /**
     * @param template
     *         the file that contains the HTML code for custom file displaying
     */
    public HtmlFileViewer(final File template) {
        this.templateFile = template;
        this.extensions = new ArrayList<>();

        // Load file
        this.templateContent = HtmlVariablesUtils.getTemplateContent(this.templateFile);

        // Setup variable replacement
        this.substitutions = new EnumMap<>(HtmlVariablesUtils.Variable.class);
        this.substitutions.put(HtmlVariablesUtils.Variable.PATH, this.templateFile.getParentFile()::getAbsolutePath);

        // Extract extensions
        if (!"".equals(this.templateContent)) {
            this.extractExtensions();
        }
    }

    private void extractExtensions() {
        boolean ok = true;
        final String fileName = this.templateFile.getName().replace(".html", "");

        // Check the file has extensions
        if (fileName.indexOf('-') == -1) {
            if (HtmlFileViewer.logger.isInfoEnabled()) {
                HtmlFileViewer.logger.info(
                        ResultsDictionary.NO_EXTENSION_IN_NAME.getString(this.templateFile.getName()));
            }
            ok = false;
        }

        // Check file contains variables
        final String contentVariable = HtmlVariablesUtils.Variable.CONTENT.getValue();
        final String pathVariable = HtmlVariablesUtils.Variable.FILENAME.getValue();
        if (!this.templateContent.contains(contentVariable) && !this.templateContent.contains(pathVariable)) {
            ok = false;
            if (HtmlFileViewer.logger.isInfoEnabled()) {
                HtmlFileViewer.logger.info(
                        ResultsDictionary.NO_VARIABLE_IN_FILE.getString(this.templateFile.getName()));
            }
        }

        // Finally register the extensions
        if (ok) {
            final String[] stringExtensions = fileName.split("-");
            this.extensions.addAll(Arrays.asList(stringExtensions).subList(1, stringExtensions.length));
        }

    }

    /**
     * @return the list of the extensions handled by this template.
     */
    public Collection<String> getExtensions() {
        return this.extensions;
    }

    /**
     * @param fileToLoad
     *         the file to load into the HTML
     * @return the content of the template to load into the webview with the correct variable substitutions.
     */
    public String getContent(final File fileToLoad) {
        final File absoluteFile = fileToLoad.getAbsoluteFile();
        if (this.templateContent.contains(HtmlVariablesUtils.Variable.FILENAME.getValue())) {
            this.substitutions.put(HtmlVariablesUtils.Variable.FILENAME, absoluteFile::getName);
        }
        if (this.templateContent.contains(HtmlVariablesUtils.Variable.DIR.getValue())) {
            this.substitutions.put(HtmlVariablesUtils.Variable.DIR, absoluteFile::getParent);
        }
        if (this.templateContent.contains(HtmlVariablesUtils.Variable.BASE.getValue())) {
            this.substitutions.put(HtmlVariablesUtils.Variable.BASE, absoluteFile::getParent);
        }
        if (this.templateContent.contains(HtmlVariablesUtils.Variable.CONTENT.getValue())) {
            this.substitutions.put(HtmlVariablesUtils.Variable.CONTENT, () -> {
                try {
                    return HtmlFileViewer.loadFileContent(absoluteFile);
                } catch (final IOException e) {
                    HtmlFileViewer.logger.warn(
                            ResultsDictionary.UNABLE_TO_LOAD_FILE.getString(absoluteFile.getName(), e.getMessage()));
                    HtmlFileViewer.logger.debug(e.getMessage(), e);
                    return "Cannot read " + absoluteFile;
                }
            });
        }
        return HtmlVariablesUtils.replaceVariables(this.templateContent, this.substitutions);
    }

    /**
     * Loads the provided file content. Two cases:
     * <ul>
     *     <li>File is ASCII: its content is returned</li>
     *     <li>File is binary: its binary content is encoded to Base64</li>
     * </ul>
     *
     * @param file
     *         the file to load
     * @return the content of the file (text or Base64)
     *
     * @throws IOException
     *         if unable to read file
     */
    static String loadFileContent(final File file) throws IOException {
        if (HtmlFileViewer.isBinaryFile(file)) {
            HtmlFileViewer.logger.debug("Loading file {} as binary", file);
            final byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } else {
            HtmlFileViewer.logger.debug("Loading file {} as ASCII", file);
            return Files.readString(file.toPath());
        }
    }

    private static boolean isBinaryFile(final File file) throws IOException {
        try (final InputStream is = new FileInputStream(file)) {
            final byte[] buffer = new byte[1024]; // Read first 1KB
            final int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                return false; // Empty file = non-binary
            }

            for (int i = 0; i < bytesRead; i++) {
                final byte b = buffer[i];
                if (b < 0x09 || (b > 0x0D && b < 0x20)) { // Non-printable ASCII characters
                    return true;
                }
            }
            return false;
        }
    }
}
