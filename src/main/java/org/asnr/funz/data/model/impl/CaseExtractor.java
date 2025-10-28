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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.data.model.DiscreteCaseResults;
import org.funz.Project;
import org.funz.parameter.Case;
import org.funz.util.Data;

import javafx.beans.value.ObservableValueBase;

/**
 * Extracts asynchronously the results of a case.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
class CaseExtractor extends ObservableValueBase<DiscreteCaseResults> implements DiscreteCaseResults {

    /**
     * Pattern to recognise percentage results such as "XX/XX".
     */
    private static final Pattern PERCENT_PATTERN = Pattern.compile("\\d+/\\d+");

    /**
     * The FTP prefix.
     */
    private static final String FTP_PREFIX = "ftp://";

    private final Project project;

    private final Case inputCase;

    private String status;
    private final Map<String, Object> output;

    private String ftp = "";

    private double percent = -1d;

    private double count = -1d;

    private boolean isParsing = false;

    /**
     * Stores the results that depends on the output function (results extracted from the output not the status).
     */
    private final Map<String, DiscreteCaseOutputResult> outputFunctionResults;

    /**
     * @param theProject
     *         the parent project for extraction of output
     * @param givenCase
     *         the case to analyse
     */
    public CaseExtractor(final Project theProject, final Case givenCase) {
        this.project = theProject;
        this.inputCase = givenCase;
        this.status = "";
        this.output = new HashMap<>();
        this.outputFunctionResults = new HashMap<>();
        this.update();
    }

    /**
     * Updates the case
     */
    public synchronized void update() {
        this.isParsing = true;

        final boolean isUpdated = this.extractResults();

        if (isUpdated) {
            if (((this.status == null) || this.status.isEmpty()) && ((this.output == null) || this.output.isEmpty())) {
                this.ftp = "";
                this.percent = -1d;
                this.count = -1d;
            } else {
                this.ftp();
                this.percent();
                this.count();
            }
            this.outputFunctionResults.values().forEach(DiscreteCaseOutputResult::update);
        }
        this.isParsing = false;
        this.fireValueChangedEvent();
    }

    private void ftp() {
        if (!this.status.isEmpty() && this.status.contains(CaseExtractor.FTP_PREFIX)) {
            this.ftp = CaseExtractor.FTP_PREFIX + this.status.split(CaseExtractor.FTP_PREFIX)[1].split(" ")[0].trim();
            this.status = this.status.replace(this.ftp, "");
        }
        // Do not update FTP if it was already setup
    }

    private void percent() {
        if (!this.status.isEmpty() && this.status.contains("%")) {
            final String[] quantity = this.status.split("%");
            this.percent = Double.parseDouble(quantity[0]) / 100.0;
            this.status = quantity[1];
        }
    }

    private void count() {
        final Matcher percentMatcher = CaseExtractor.PERCENT_PATTERN.matcher(this.status);
        if (percentMatcher.find()) {
            final String toSplit = this.status.substring(percentMatcher.start(), percentMatcher.end());
            final String quantity = toSplit.split("/")[0];
            String total = toSplit.split("/")[1];
            if (total.trim().contains(" ")) {
                total = total.split(" ")[0];
            }

            this.count = Double.parseDouble(quantity) / Double.parseDouble(total);
            this.status = this.status.replace(quantity + "/" + total, "");
        }
    }

    private boolean extractResults() {
        boolean updated = false;
        this.status = "";
        this.output.clear();

        // Extracts the correct result from the case outputs
        if (this.inputCase.hasRun() && (this.inputCase.getResult() != null)) {
            final Map<String, Object> content = this.inputCase.getResult();
            if (!this.output.equals(content)) {
                updated = true;
                this.output.putAll(content);
                for (final String key : this.inputCase.getInputValues().keySet()) {
                    this.output.remove(key);
                }
            }

        } else {
            final String newStatus = this.inputCase.getStatusInformation();
            if (!this.status.equals(newStatus)) {
                updated = true;
                this.status = newStatus;
            }
        }

        // Look at output functions
        if (!this.outputFunctionResults.keySet().equals(this.output.keySet())) {
            this.outputFunctionResults.clear();
            this.output.keySet().forEach(n -> this.outputFunctionResults.put(n, new DiscreteCaseOutputResult(this, n)));
            updated = true;
        }

        return updated;
    }

    @Override
    public Case getCase() {
        return this.inputCase;
    }

    @Override
    public DiscreteCaseResults getValue() {
        return this;
    }

    @Override
    public String getValueForVariable(final String variableName) {
        final Map<String, String> caseParameters = this.project.getCaseParameters(this.inputCase);
        if (Project.SINGLE_PARAM_NAME.equals(variableName) && ((caseParameters == null) || (
                caseParameters.get(variableName) == null))) {
            return "";
        }
        return caseParameters.get(variableName);
    }

    @Override
    public Object getResult(final String customMainFunction) {
        if (this.isParsing) {
            return ResultsDictionary.PARSING_RESULTS.getString();
        }

        return this.getRawResult(customMainFunction);
    }

    @Override
    public String getStringResult(final String selectedOutputFunction) {
        return Data.asString(this.getResult(selectedOutputFunction));
    }

    @Override
    public Map<String, Object> getOtherResults(final String customMainFunction) {
        return this.output.entrySet().stream()
                .filter(e -> !e.getKey().equals(customMainFunction) && (e.getValue() != null)).distinct()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public boolean hasFtp() {
        return !this.ftp.isEmpty();
    }

    @Override
    public String getFtpAddress() {
        return this.ftp;
    }

    @Override
    public boolean hasPercentProgress() {
        return this.percent > -0.5d;
    }

    @Override
    public double getPercentProgress() {
        return this.percent;
    }

    @Override
    public boolean hasCountProgress() {
        return this.count > -0.5d;
    }

    @Override
    public double getCountProgress() {
        return this.count;
    }

    @Override
    public boolean hasSparkLine(final String outputFunction) {
        final Optional<DiscreteCaseOutputResult> resultFunction = this.getResultFunction(outputFunction);
        return resultFunction.map(DiscreteCaseOutputResult::hasSparkLine).orElse(false);
    }

    @Override
    public List<Number> getSparkAbscissa(final String outputFunction) {
        final Optional<DiscreteCaseOutputResult> resultFunction = this.getResultFunction(outputFunction);
        return resultFunction.map(DiscreteCaseOutputResult::getSparkAbscissa).orElse(Collections.emptyList());
    }

    @Override
    public List<List<Number>> getSparkValues(final String outputFunction) {
        final Optional<DiscreteCaseOutputResult> resultFunction = this.getResultFunction(outputFunction);
        return resultFunction.map(DiscreteCaseOutputResult::getSparkValues).orElse(Collections.emptyList());
    }

    /**
     * @return the current status
     */
    String getStatus() {
        return this.status;
    }

    /**
     * @return the raw output
     */
    Map<String, Object> getRawOutput() {
        return this.output;
    }

    /**
     * Ignore parsing state to get the real result
     *
     * @param mainFunction
     *         the main function
     * @return the raw result
     */
    Object getRawResult(final String mainFunction) {
        if (this.output.isEmpty() || (this.project.getMainOutputFunctionName() == null)) {
            return this.status;
        }

        final String mainOutput = this.getCleanedFunction(mainFunction);
        final Object result = this.output.get(mainOutput);
        return result == null ? "?" : result;
    }

    private String getCleanedFunction(final String mainFunction) {
        String mainOutput = mainFunction;
        if ((mainOutput == null) || mainOutput.isEmpty()) {
            mainOutput = this.project.getMainOutputFunctionName();
        }
        mainOutput = mainOutput.replace("'", "").replace("[", "").replace("]", "");
        return mainOutput;
    }

    private Optional<DiscreteCaseOutputResult> getResultFunction(final String mainFunction) {
        final String mainOutput = this.getCleanedFunction(mainFunction);
        if (this.outputFunctionResults.containsKey(mainOutput)) {
            return Optional.of(this.outputFunctionResults.get(mainOutput));
        }
        return Optional.empty();
    }
}
