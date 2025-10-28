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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asnr.funz.data.model.ResultModel;
import org.asnr.funz.model.ExtendedProject;

/**
 * Implements basic behaviour of dataminers.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
abstract class AbstractDataMiner implements DataMiner {

    /**
     * The current {@link ExtendedProject}, used to access {@link ResultModel}
     */
    private final List<ExtendedProject> projects;

    /**
     * The name of the data-miner, used in the tab text
     */
    private final String name;

    /**
     * @param project
     *         the current {@link ExtendedProject}
     * @param aName
     *         the name of the data-miner
     */
    protected AbstractDataMiner(final ExtendedProject project, final String aName) {
        this.projects = new ArrayList<>();
        this.name = aName;

        this.addExtendedProject(project);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean canDisplayData() {
        return !this.getResultModels().getFirst().getDiscreteResults().isEmpty();
    }

    /**
     * @param project
     *         the project to add to the data-miner projects
     */
    public void addExtendedProject(final ExtendedProject project) {
        this.projects.add(project);
    }

    public Map<String, String> getColumnsFormat() {
        final Map<String, String> columnsFormat = new HashMap<>();
        for (final ExtendedProject project : this.projects) {
            columnsFormat.putAll(project.getPlugin().getOutputFormat());
        }
        return columnsFormat;
    }

    /**
     * @return the {@link ResultModel} from the project
     */
    protected ResultModel getResultModel() {
        return this.projects.getFirst().getResultModel();
    }

    /**
     * @return the {@link ResultModel result models} for all projects present in this data-miner
     */
    protected final List<ResultModel> getResultModels() {
        final List<ResultModel> resultModels = new ArrayList<>();
        for (final ExtendedProject project : this.projects) {
            resultModels.add(project.getResultModel());
        }
        return resultModels;
    }

    /**
     * @return the list of the outputs names
     */
    protected final List<String> getOutputNames() {
        final List<String> outputNames = new ArrayList<>();
        for (final ExtendedProject project : this.projects) {
            outputNames.addAll(Arrays.asList(project.getSyncOutputNames()));
        }
        return outputNames;
    }

    /**
     * @return the name of the main output
     */
    protected final String getMainOutputName() {
        return this.projects.getFirst().getMainOutputFunctionName();
    }

}
