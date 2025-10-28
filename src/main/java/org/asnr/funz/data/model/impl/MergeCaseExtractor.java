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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

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

    private final BinaryOperator<String> variablesMerger;
    private final BinaryOperator<String> valuesMerger;
    private final Collection<String> variables;

    /**
     *
     */
    public MergeCaseExtractor(final List<CaseExtractor> subCases, final Collection<String> variables,
            final BinaryOperator<String> variablesMerger, final BinaryOperator<String> valuesMerger) {
        this.subCases = subCases;
        this.variablesMerger = variablesMerger;
        this.valuesMerger = valuesMerger;
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
        return this.subCases.stream().map(c -> c.getValueForVariable(variableName)).reduce(this.variablesMerger)
                .orElse("");
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
        return this.subCases.stream() //
                .map(c -> c.getOtherResults(customMainFunction)) //
                .reduce(this::mergeMap) //
                .orElse(Map.of());
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
        return this.subCases.stream() //
                .map(CaseExtractor::getRawOutput) //
                .reduce(this::mergeMap) //
                .orElse(Map.of());
    }

    /**
     * Ignore parsing state to get the real result
     *
     * @param mainFunction
     *         the main function
     * @return the raw result
     */
    Object getRawResult(final String mainFunction) {
        return this.subCases.stream().map(c -> c.getRawResult(mainFunction).toString()).reduce(this.valuesMerger)
                .orElse(null);
    }

    private Map<String, Object> mergeMap(final Map<String, Object> map1, final Map<String, Object> map2) {
        final Map<String, Object> result = HashMap.newHashMap(map1.size());

        for (final Map.Entry<String, Object> e1 : map1.entrySet()) {
            final String v1 = e1.getValue().toString();
            final String v2 = map2.get(e1.getKey()).toString();
            if (this.variables.contains(e1.getKey())) {
                result.put(e1.getKey(), this.variablesMerger.apply(v1, v2));
            } else {
                result.put(e1.getKey(), this.valuesMerger.apply(v1, v2));
            }
        }

        return result;
    }

}

