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
package org.asnr.funz.data.model.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.asnr.funz.data.model.DiscreteCaseResults;
import org.funz.parameter.Case;
import org.funz.util.Data;

import javafx.beans.value.ObservableValueBase;

/**
 * Extracts asynchronously the results of a case.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
class MergeCaseExtractor extends ObservableValueBase<DiscreteCaseResults> implements DiscreteCaseResults {

    private final List<CaseExtractor> subCases;

    private final Collection<String> variables;

    /**
     * @param subCases
     *         the cases of the group
     * @param variables
     *         the variables, whose different values are listed: the means of the other values are shown
     */
    public MergeCaseExtractor(final List<CaseExtractor> subCases, final Collection<String> variables) {
        this.subCases = subCases;
        this.variables = variables;
    }

    /**
     * Updates the case
     */
    public synchronized void update() {
        // Nothing to od
    }

    @Override
    public Case getCase() {
        return this.subCases.getFirst().getCase();
    }

    @Override
    public DiscreteCaseResults getValue() {
        return this;
    }

    @Override
    public String getValueForVariable(final String variableName) {
        return ResultAggregation.mergeVariable(
                this.subCases.stream().map(c -> c.getValueForVariable(variableName)).toList());
    }

    @Override
    public Object getResult(final String customMainFunction) {
        return this.getRawResult(customMainFunction);
    }

    @Override
    public String getStringResult(final String selectedOutputFunction) {
        return Data.asString(this.getResult(selectedOutputFunction));
    }

    @Override
    public Map<String, Object> getOtherResults(final String customMainFunction) {
        return this.mergeMaps(this.subCases.stream().map(c -> c.getOtherResults(customMainFunction)).toList());
    }

    @Override
    public boolean hasFtp() {
        return false;
    }

    @Override
    public String getFtpAddress() {
        return "";
    }

    @Override
    public boolean hasPercentProgress() {
        return false;
    }

    @Override
    public double getPercentProgress() {
        return -1;
    }

    @Override
    public boolean hasCountProgress() {
        return false;
    }

    @Override
    public double getCountProgress() {
        return -1;
    }

    @Override
    public boolean hasSparkLine(final String outputFunction) {
        return false;
    }

    @Override
    public List<Number> getSparkAbscissa(final String outputFunction) {
        return List.of();
    }

    @Override
    public List<List<Number>> getSparkValues(final String outputFunction) {
        return List.of();
    }

    /**
     * @return the current status
     */
    String getStatus() {
        return "FINISHED";
    }

    /**
     * @return the raw output
     */
    Map<String, Object> getRawOutput() {
        return this.mergeMaps(this.subCases.stream().map(CaseExtractor::getRawOutput).toList());
    }

    /**
     * Ignore parsing state to get the real result
     *
     * @param mainFunction
     *         the main function
     * @return the raw result
     */
    Object getRawResult(final String mainFunction) {
        return ResultAggregation.mergeResult(
                this.subCases.stream().map(c -> Data.asString(c.getRawResult(mainFunction))).toList());
    }

    /**
     * @return the merged values of the keys of the maps: a key missing in a map is merged from the other maps
     */
    private Map<String, Object> mergeMaps(final List<Map<String, Object>> maps) {
        final Map<String, Object> result = new LinkedHashMap<>();
        maps.stream().flatMap(map -> map.keySet().stream()).distinct().forEach(key -> {
            final List<String> values = maps.stream()
                    .filter(map -> map.containsKey(key))
                    .map(map -> Data.asString(map.get(key)))
                    .toList();
            result.put(key, this.variables.contains(key) ?
                    ResultAggregation.mergeVariable(values) :
                    ResultAggregation.mergeResult(values));
        });
        return result;
    }
}

