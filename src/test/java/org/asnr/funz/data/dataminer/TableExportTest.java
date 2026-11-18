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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link TableExport}.
 */
public class TableExportTest {

    private static final List<String> HEADER = List.of("var.medium", "keff");

    private static final List<List<Object>> ROWS = List.of(
            Arrays.asList("water; 20°C", 1.00234),
            Arrays.asList("say \"hi\"", "?"),
            Arrays.asList("2d", null));

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void theCsvValuesAreQuotedWhenNeededWithoutSeparatorAtTheEndOfTheLines() throws IOException {
        final File file = this.folder.newFile("results.csv");

        TableExport.writeCsv(file, TableExportTest.HEADER, TableExportTest.ROWS);

        Assert.assertEquals(List.of("var.medium;keff", "\"water; 20°C\";1.00234", "\"say \"\"hi\"\"\";?", "2d;"),
                Files.readAllLines(file.toPath(), java.nio.charset.Charset.defaultCharset()));
    }

    @Test
    public void theClipboardTextIsSeparatedByTabulations() {
        final String text = TableExport.toClipboardText(TableExportTest.HEADER,
                List.of(Arrays.asList("water", new double[] { 1.0, 0.001 })));

        Assert.assertEquals(List.of("var.medium\tkeff", "water\t[1.0, 0.001]"), text.lines().toList());
    }

    @Test
    public void theNumbersOfTheWorkbookOfExcelAreNumbers() throws IOException {
        final File file = this.folder.newFile("results.xlsx");

        TableExport.writeWorkbook(file, TableExportTest.HEADER, TableExportTest.ROWS);

        try (final Workbook workbook = WorkbookFactory.create(file)) {
            Assert.assertTrue(workbook instanceof XSSFWorkbook);
            final Row first = workbook.getSheetAt(0).getRow(1);
            Assert.assertEquals(CellType.NUMERIC, first.getCell(1).getCellType());
            Assert.assertEquals(1.00234, first.getCell(1).getNumericCellValue(), 0);
            // the texts that Java reads as numbers but are names
            Assert.assertEquals("2d", workbook.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());
            Assert.assertEquals("?", workbook.getSheetAt(0).getRow(2).getCell(1).getStringCellValue());
        }
    }

    @Test
    public void aXlsFileIsInTheOldFormatOfExcel() throws IOException {
        final File file = this.folder.newFile("results.xls");

        TableExport.writeWorkbook(file, TableExportTest.HEADER, TableExportTest.ROWS);

        try (final Workbook workbook = WorkbookFactory.create(file)) {
            Assert.assertTrue(workbook instanceof HSSFWorkbook);
            Assert.assertEquals("keff", workbook.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
        }
    }
}
