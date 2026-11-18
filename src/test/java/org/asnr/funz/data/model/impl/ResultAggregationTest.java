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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ResultAggregation}.
 */
public class ResultAggregationTest {

    @Test
    public void theMergedResultIsTheMeanOfAllTheValues() {
        Assert.assertEquals(2.0, Double.parseDouble(ResultAggregation.mergeResult(List.of("1", "2", "3"))), 1e-12);
        Assert.assertEquals(2.5,
                Double.parseDouble(ResultAggregation.mergeResult(List.of("1", "2", "3", "4"))), 1e-12);
    }

    @Test
    public void theMergedResultOfTextsIsTheTextWhenTheyAreTheSame() {
        Assert.assertEquals("FAILED", ResultAggregation.mergeResult(List.of("FAILED", "FAILED", "FAILED")));
        Assert.assertEquals("?", ResultAggregation.mergeResult(List.of("1.0", "FAILED", "2.0")));
        Assert.assertEquals("?", ResultAggregation.mergeResult(Arrays.asList("1.0", null)));
        Assert.assertEquals("?", ResultAggregation.mergeResult(List.of()));
    }

    @Test
    public void theMergedVariableListsItsDifferentValuesOnce() {
        Assert.assertEquals("a,b", ResultAggregation.mergeVariable(Arrays.asList("a", "b", "a", null)));
        Assert.assertEquals("a", ResultAggregation.mergeVariable(List.of("a", "a")));
    }

    @Test
    public void theGroupsKeepTheOrderOfTheItemsAndDoNotMixValuesThatJoinTheSame() {
        final List<List<String>> items = List.of(List.of("a-b", "c"), List.of("a", "b-c"), List.of("a-b", "c"));

        final List<List<List<String>>> groups = ResultAggregation.groupBy(items, item -> item);

        Assert.assertEquals(List.of(List.of(items.get(0), items.get(2)), List.of(items.get(1))), groups);
    }
}
