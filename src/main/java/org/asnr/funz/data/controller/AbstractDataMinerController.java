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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asnr.funz.data.dataminer.DataMiner;
import org.asnr.funz.data.dataminer.WebViewDataMiner;
import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.view.HtmlVariablesUtils;
import org.asnr.funz.model.ExtendedProject;
import org.funz.parameter.Case;
import org.funz.util.Data;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

/**
 * Controller of the tab displaying several results renderers that will display the results in different ways.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
abstract class AbstractDataMinerController extends AbstractDynamicUpdateController {

    /**
     * Current project
     */
    private final ExtendedProject project;

    /**
     * The data miner wrappers used to interact with them.
     */
    private final List<DataMinerWrapperController> dataMinerWrappers;

    /**
     * Represents the directory where the templates are located.
     */
    private final File templateDirectory;

    /**
     * List of currently displayed data miners.
     */
    private final List<WebViewDataMiner> webDataMiners;

    private final TabPane tabs;

    /**
     * Creates a {@link AbstractDataMinerController} for the given {@link ExtendedProject}.
     *
     * @param project
     *         the current {@link ExtendedProject}
     * @param location
     *         the {@link HtmlVariablesUtils.Location} of the HTML template
     */
    protected AbstractDataMinerController(final ExtendedProject project, final HtmlVariablesUtils.Location location) {
        this.project = project;
        this.templateDirectory = location.getFolder();
        this.dataMinerWrappers = new ArrayList<>();
        this.webDataMiners = new ArrayList<>();

        this.tabs = new TabPane();
        this.tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        this.tabs.setSide(Side.BOTTOM);

        this.setCallback(() -> this.refresh(false));
        this.project.addStatusListener((modifiedProject, newStatus) -> {
            switch (newStatus) {
            case RESULTS, FAILED, RUNNING -> this.refresh(false);
            case INITIAL -> this.clear();
            }
        });

        this.project.addCaseModifiedListener((index, what) -> {
            final Case caseToAdd = this.project.getCases().get(index);
            if (caseToAdd.hasRun() && what == Case.MODIFIED_STATE) {
                this.refresh(true);
            }
        });
    }

    /**
     * Adds the given {@link DataMiner} to the view.
     *
     * @param dataMiner
     *         {@link DataMiner}
     */
    void addDataMiner(final DataMiner dataMiner) {
        final DataMinerWrapperController controller = new DataMinerWrapperController(dataMiner);

        this.dataMinerWrappers.add(controller);
        Platform.runLater(() -> {
            final List<Tab> theTabs = new ArrayList<>(this.tabs.getTabs());
            theTabs.add(controller.getTab());
            theTabs.sort(AbstractDataMinerController::compareTabNames);

            this.tabs.getTabs().clear();
            this.tabs.getTabs().addAll(theTabs);
        });
    }

    /**
     * Removes the given {@link DataMiner} from the view.
     *
     * @param dataMiner
     *         {@link DataMiner}
     */
    void removeDataMiner(final DataMiner dataMiner) {
        DataMinerWrapperController wrapperToRemove = null;
        for (final DataMinerWrapperController wrapper : this.dataMinerWrappers) {
            if (wrapper.getDataMiner().equals(dataMiner)) {
                wrapperToRemove = wrapper;
            }
        }

        if (wrapperToRemove != null) {
            final DataMinerWrapperController finalWrapper = wrapperToRemove;
            Platform.runLater(() -> this.tabs.getTabs().remove(finalWrapper.getTab()));
            this.dataMinerWrappers.remove(wrapperToRemove);
        }
    }

    /**
     * @return the view
     */
    Node getView() {
        return this.tabs;
    }

    @Override
    void setShowing(final boolean isShowing) {
        if (isShowing) {
            this.updateHtmlDataMiners();
        }
        super.setShowing(isShowing);
        this.dataMinerWrappers.forEach(w -> w.setShowing(isShowing));
    }

    /**
     * Refresh all the {@link DataMiner}s.
     *
     * @param liveUpdate
     *         if {@code true} this refresh is a "live" refresh and should only be executed if the checkbox is
     *         checked
     */
    protected void refresh(final boolean liveUpdate) {
        if (this.isShowing()) {
            this.updateHtmlDataMiners();
            for (final DataMinerWrapperController wrapper : this.dataMinerWrappers) {
                wrapper.refresh(liveUpdate);
            }
        } else {
            this.needUpdate();
        }
    }

    /**
     * @return the project
     */
    protected final ExtendedProject getProject() {
        return this.project;
    }

    /**
     * @return the list of {@link DataMinerWrapperController}
     */
    protected final Collection<DataMinerWrapperController> getDataMiners() {
        return this.dataMinerWrappers;
    }

    /**
     * Clear all {@link DataMiner}s.
     */
    private void clear() {
        for (final DataMinerWrapperController wrapper : this.dataMinerWrappers) {
            wrapper.getDataMiner().clear();
        }
    }

    private void updateHtmlDataMiners() {
        if (this.webDataMiners.isEmpty() && this.templateDirectory != null) {
            final File[] htmlFiles = this.templateDirectory.listFiles((dir, name) -> name.endsWith("html"));

            if (htmlFiles != null) {
                for (final File template : htmlFiles) {
                    final WebViewDataMiner dataMiner = new WebViewDataMiner(this.getProject(), template,
                            this::getParameters, this::getValuesAsString);
                    this.webDataMiners.add(dataMiner);
                    this.addDataMiner(dataMiner);
                }
            }
        }
    }

    /**
     * @return the list of parameters applicable for this data miner
     */
    protected abstract String getParameters();

    /**
     * @return the output values as string
     */
    protected abstract String getValuesAsString();

    /**
     * @param variables
     *         the variables to use
     * @param cases
     *         the case to use
     * @return the String representation of the parameters
     */
    protected String getParameters(final Collection<String> variables, final List<CaseResults> cases) {
        // Prepare map
        final Map<String, Object[]> parameters = HashMap.newHashMap(variables.size());
        for (final String variableName : variables) {
            parameters.put(variableName, new Object[cases.size()]);
        }

        // Fill map
        for (int index = 0; index < cases.size(); index++) {
            for (final String variableName : variables) {
                String parameterValue = cases.get(index).getValueForVariable(variableName);

                // If it is a String we encapsulate it with simple quote (double quotes cannot be passed to html for
                // web view as the parcoords.html ?)
                if (!AbstractDataMinerController.isNumeric(parameterValue)) {
                    parameterValue = "'" + parameterValue + "'";
                }

                parameters.get(variableName)[index] = parameterValue;
            }
        }

        return Data.asString(parameters);
    }

    private static boolean isNumeric(final String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static int compareTabNames(final Tab o1, final Tab o2) {
        final String t1 = o1.getText();
        final String t2 = o2.getText();

        final String tableName = ResultsDictionary.VALUES.getString();
        if (t1.equals(tableName)) {
            return -1;
        }
        if (t2.equals(tableName)) {
            return +1;
        }
        return t1.compareTo(t2);
    }

}
