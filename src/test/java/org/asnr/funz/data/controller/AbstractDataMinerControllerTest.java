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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the values that {@link AbstractDataMinerController} gives to the views.
 */
public class AbstractDataMinerControllerTest {

    @Test
    public void aTextValueHasNoQuoteThatWouldEndIt() {
        Assert.assertEquals("'eau d\u2019Evian \u201Dpure\u201D C:/data'",
                AbstractDataMinerController.quoted("eau d'Evian \"pure\" C:\\data"));
    }

    @Test
    public void theResultsThatAreNotNumbersAreMissingInTheViews() {
        final Object[] values = AbstractDataMinerController.numericValues(new Object[] { "FAILED", 1.02, "?" });

        Assert.assertArrayEquals(new Object[] { "?", 1.02, "?" }, values);
    }

    @Test
    public void aResultWithoutNumberIsNotInTheViews() {
        Assert.assertNull(AbstractDataMinerController.numericValues(new Object[] { "FAILED", "?" }));
    }
}
