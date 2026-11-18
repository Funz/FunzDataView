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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the formats of the export of {@link AbstractTableDataMiner}.
 */
public class AbstractTableDataMinerTest {

    @Test
    public void xlsIsExportedAsXlsxForTheCallersOfBefore() {
        Assert.assertEquals("XLSX", AbstractTableDataMiner.exportFormat("XLS"));
        Assert.assertEquals("XLSX", AbstractTableDataMiner.exportFormat("xlsx"));
        Assert.assertEquals("CSV", AbstractTableDataMiner.exportFormat("csv"));
    }
}
