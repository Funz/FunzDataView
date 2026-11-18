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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Merges the values of the cases grouped by the values of their variables.
 */
final class ResultAggregation {

    /** The merged value of results that are neither numbers nor all the same. */
    static final String UNDEFINED = "?";

    private ResultAggregation() {
        throw new AssertionError();
    }

    /**
     * @param <T>
     *         the type of the items
     * @param items
     *         the items to group
     * @param key
     *         the values that the items of a group share
     * @return the groups, in the order of their first item
     */
    static <T> List<List<T>> groupBy(final Collection<T> items, final Function<T, List<String>> key) {
        final Map<List<String>, List<T>> groups = new LinkedHashMap<>();
        for (final T item : items) {
            groups.computeIfAbsent(key.apply(item), k -> new ArrayList<>()).add(item);
        }
        return new ArrayList<>(groups.values());
    }

    /**
     * @param values
     *         the values of a variable in the cases of a group
     * @return the different values, in their order, separated by commas
     */
    static String mergeVariable(final List<String> values) {
        return values.stream().filter(Objects::nonNull).distinct().collect(Collectors.joining(","));
    }

    /**
     * @param values
     *         the values of a result in the cases of a group
     * @return the mean of the values when they are all numbers, the value when they are all the same,
     *         {@link #UNDEFINED} otherwise
     */
    static String mergeResult(final List<String> values) {
        if (values.isEmpty()) {
            return UNDEFINED;
        }
        double sum = 0;
        for (final String value : values) {
            try {
                sum += Double.parseDouble(value);
            } catch (final NumberFormatException | NullPointerException e) {
                return values.stream().distinct().count() == 1 ? values.getFirst() : UNDEFINED;
            }
        }
        return String.valueOf(sum / values.size());
    }
}
