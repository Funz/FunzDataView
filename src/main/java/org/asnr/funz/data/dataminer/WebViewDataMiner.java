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
package org.asnr.funz.data.dataminer;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.asnr.funz.data.view.HtmlVariablesUtils;
import org.asnr.funz.model.ExtendedProject;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * Data miner to display a HTML file.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public class WebViewDataMiner extends AbstractDataMiner implements DataMiner {

    /**
     * The main view.
     */
    private final WebView view;

    /**
     * The file content is loaded into this variable.
     */
    private final String templateContent;

    private final Map<HtmlVariablesUtils.Variable, Supplier<String>> valuesMap;

    /**
     * @param file
     *         the file to load
     * @return the name of the dataminer
     */
    private static String getName(final File file) {
        final String name = file.getName().replace(".html", "").replace("\"", "");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Default constructor.
     *
     * @param project
     *         the current {@link ExtendedProject}
     * @param htmlFile
     *         the HTML file
     * @param parameterSupplier
     *         extracts the correct parameter names from the project
     * @param valuesSupplier
     *         extracts the values from the model as string
     */
    public WebViewDataMiner(final ExtendedProject project, final File htmlFile,
            final Supplier<String> parameterSupplier, final Supplier<String> valuesSupplier) {
        super(project, WebViewDataMiner.getName(htmlFile));

        final File templateFile = htmlFile.getAbsoluteFile();
        this.templateContent = HtmlVariablesUtils.getTemplateContent(templateFile);

        // Map providing the various variable values
        this.valuesMap = new EnumMap<>(HtmlVariablesUtils.Variable.class);
        this.valuesMap.put(HtmlVariablesUtils.Variable.PARAMETERS, parameterSupplier);
        this.valuesMap.put(HtmlVariablesUtils.Variable.VALUES, valuesSupplier);
        this.valuesMap.put(HtmlVariablesUtils.Variable.NAME, this::getName);
        this.valuesMap.put(HtmlVariablesUtils.Variable.PATH, templateFile::getParent);
        this.valuesMap.put(HtmlVariablesUtils.Variable.DIR, project.getResultsDir()::getAbsolutePath);
        this.valuesMap.put(HtmlVariablesUtils.Variable.BASE, project.getResultsDir()::getAbsolutePath);

        this.view = new WebView();
        Platform.runLater(this::update);
    }

    @Override
    public void clear() {
        // Nothing to do
    }

    @Override
    public void refreshVariables() {
        this.update();
    }

    @Override
    public void refreshCases() {
        this.update();
    }

    @Override
    public Node getView() {
        return this.view;
    }

    private void update() {
        final String contentToLoad = HtmlVariablesUtils.replaceVariables(this.templateContent, this.valuesMap);
        Platform.runLater(() -> this.view.getEngine().loadContent(contentToLoad));
    }

}
