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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.funz.util.Data;

/**
 * Extracts results of a case for a specific output function.
 *
 * @author Arnaud Trouche - Artenum SARL
 */
class DiscreteCaseOutputResult {

    /**
     * Pattern to identify a double, used in {@link #DOUBLE_PATTERN} and {@link #DOUBLE_PAIR_PATTERN}.
     */
    private static final String DOUBLE_PATTERN_STRING = "\\-?\\d+(\\.\\d+)?([eE]\\-?\\d+)?";

    /**
     * Pattern to recognise doubles by pair results to be displayed as sparkline (when several).
     */
    private static final Pattern DOUBLE_PAIR_PATTERN = Pattern.compile(
            "(\\w?\\(\\w?" + DiscreteCaseOutputResult.DOUBLE_PATTERN_STRING + "\\w?,\\w?"
                    + DiscreteCaseOutputResult.DOUBLE_PATTERN_STRING + "\\w?\\)\\w?)");

    private final CaseExtractor parentResults;

    private final String outputFunction;

    private List<Number> abscissa = null;

    private List<List<Number>> values = null;

    /**
     * @param caseResults
     *         {@link  CaseExtractor}
     * @param outputFunctionName
     *         the output function name
     */
    DiscreteCaseOutputResult(final CaseExtractor caseResults, final String outputFunctionName) {
        this.parentResults = caseResults;
        this.outputFunction = outputFunctionName;
    }

    /**
     * Update the output: re-extract results.
     */
    void update() {
        final String status = this.parentResults.getStatus();
        final Map<String, Object> output = this.parentResults.getRawOutput();
        if (((status == null) || status.isEmpty()) && ((output == null) || output.isEmpty())) {
            this.abscissa = null;
            this.values = null;
        } else if ((status == null) || status.isEmpty()) {
            this.sparkline();
        }
    }

    /**
     * @return <code>true</code> if the results can be displayed as a sparkline
     */
    boolean hasSparkLine() {
        return this.values != null;
    }

    /**
     * @return the abscissa values for the sparkline
     */
    List<Number> getSparkAbscissa() {
        return this.abscissa;
    }

    /**
     * @return the series values for the sparkline
     */
    List<List<Number>> getSparkValues() {
        return this.values;
    }

    private void sparkline() {
        this.abscissa = null;
        this.values = null;

        final Object rawResult = this.parentResults.getRawResult(this.outputFunction);
        final Object[] array = DiscreteCaseOutputResult.extractArray(rawResult);
        if (array.length == 0) {
            return;
        }

        final List<Number> simpleResults = new ArrayList<>();
        final List<String> pairResults = new ArrayList<>();

        if (array.getClass().getComponentType().equals(Number.class)) {
            final Number[] nArray = (Number[]) array;
            simpleResults.addAll(Arrays.asList(nArray));
        } else {
            for (final Object item : array) {
                final String asString = Data.asString(item);

                final Matcher pairArrayMatcher = DiscreteCaseOutputResult.DOUBLE_PAIR_PATTERN.matcher(asString);
                if (pairArrayMatcher.find()) {
                    pairResults.add(asString.substring(pairArrayMatcher.start(), pairArrayMatcher.end()));
                }
            }
        }

        if (pairResults.size() > 2) {
            final List<Number> one = new ArrayList<>(pairResults.size());
            final List<Number> two = new ArrayList<>(pairResults.size());
            // Display a Sparkline with abcissa and ordinates values
            for (final String pair : pairResults) {
                final String[] pairs = pair.replace("(", "").replace(")", "").trim().split(",");
                one.add(Double.valueOf(pairs[0].trim()));
                two.add(Double.valueOf(pairs[1].trim()));
            }
            this.abscissa = one;
            this.values = Collections.singletonList(two);

        } else if (simpleResults.size() > 2) {
            // Display a Sparkline with simple values
            this.values = Collections.singletonList(simpleResults);
        }
    }

    private static Object[] extractArray(final Object object) {
        Object[] array = new Object[0];
        if (object == null) {
            return array;
        }
        final Class<? extends Object> objClass = object.getClass();
        if (!objClass.isArray()) {
            return array;
        }
        final Class<?> arrayComponent = objClass.getComponentType();

        // We have an array, now we need to process each primitive
        if (arrayComponent.equals(double.class)) {
            final double[] dArray = (double[]) object;
            array = Arrays.stream(dArray).boxed().toArray(Number[]::new);
        } else if (arrayComponent.equals(int.class)) {
            final int[] iArray = (int[]) object;
            array = Arrays.stream(iArray).boxed().toArray(Number[]::new);
        } else if (arrayComponent.equals(long.class)) {
            final long[] lArray = (long[]) object;
            array = Arrays.stream(lArray).boxed().toArray(Number[]::new);
        } else {
            array = (Object[]) object;
        }
        return array;
    }

}
