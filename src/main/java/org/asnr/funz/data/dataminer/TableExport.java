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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Writes the values of a table, with a header of the column names, as text (clipboard, CSV) or as a workbook.
 */
final class TableExport {

    /** The separator of the values of the CSV files. */
    static final String CSV_SEPARATOR = ";";

    /** The separator of the values copied to the clipboard: the one that spreadsheets split when pasting. */
    static final String CLIPBOARD_SEPARATOR = "\t";

    private TableExport() {
        throw new AssertionError();
    }

    /**
     * @param value
     *         a value of the table
     * @return its text: empty for no value, the elements of an array between brackets
     */
    static String format(final Object value) {
        return switch (value) {
        case null -> "";
        case final Object[] objects -> Arrays.toString(objects);
        case final double[] doubles -> Arrays.toString(doubles);
        default -> value.toString();
        };
    }

    /**
     * @param header
     *         the names of the columns
     * @param rows
     *         the values of the rows, in the order of the columns
     * @return the lines of the header and of the rows, their values separated by tabulations
     */
    static String toClipboardText(final List<String> header, final List<List<Object>> rows) {
        final StringBuilder text = new StringBuilder(String.join(TableExport.CLIPBOARD_SEPARATOR, header));
        for (final List<Object> row : rows) {
            text.append(System.lineSeparator());
            text.append(row.stream().map(TableExport::format).collect(Collectors.joining(
                    TableExport.CLIPBOARD_SEPARATOR)));
        }
        text.append(System.lineSeparator());
        return text.toString();
    }

    /**
     * Writes a CSV file: the values containing the separator, a quote or a new line are quoted.
     *
     * @param file
     *         the file to write
     * @param header
     *         the names of the columns
     * @param rows
     *         the values of the rows, in the order of the columns
     * @throws IOException
     *         when the file cannot be written
     */
    static void writeCsv(final File file, final List<String> header, final List<List<Object>> rows)
            throws IOException {
        try (final Writer writer = new FileWriter(file)) {
            TableExport.writeCsvLine(writer, header);
            for (final List<Object> row : rows) {
                TableExport.writeCsvLine(writer, row);
            }
        }
    }

    private static void writeCsvLine(final Writer writer, final List<?> values) throws IOException {
        writer.write(values.stream().map(TableExport::format).map(TableExport::csvValue)
                .collect(Collectors.joining(TableExport.CSV_SEPARATOR)));
        writer.write(System.lineSeparator());
    }

    private static String csvValue(final String value) {
        if (value.contains(TableExport.CSV_SEPARATOR) || value.contains("\"") || value.contains("\n")
                || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    /**
     * Writes a workbook of Excel (XLSX): the numbers are written as numbers, so that the spreadsheet computes with
     * them.
     *
     * @param file
     *         the file to write
     * @param header
     *         the names of the columns
     * @param rows
     *         the values of the rows, in the order of the columns
     * @throws IOException
     *         when the file cannot be written
     */
    static void writeWorkbook(final File file, final List<String> header, final List<List<Object>> rows)
            throws IOException {
        try (final Workbook workbook = new XSSFWorkbook(); final FileOutputStream output = new FileOutputStream(file)) {
            final Sheet sheet = workbook.createSheet("Export");
            final Row headerRow = sheet.createRow(0);
            for (int column = 0; column < header.size(); column++) {
                headerRow.createCell(column).setCellValue(header.get(column));
            }
            for (int index = 0; index < rows.size(); index++) {
                final Row row = sheet.createRow(index + 1);
                final List<Object> values = rows.get(index);
                for (int column = 0; column < values.size(); column++) {
                    final String text = TableExport.format(values.get(column));
                    final Double number = TableExport.toNumber(text);
                    if (number == null) {
                        row.createCell(column).setCellValue(text);
                    } else {
                        row.createCell(column).setCellValue(number);
                    }
                }
            }
            workbook.write(output);
        }
    }

    /**
     * @return the finite number written in the text, null when it is not one
     */
    private static Double toNumber(final String text) {
        // Java also reads "2d" and "2f" as numbers: they are names
        if (text.isEmpty() || Character.isLetter(text.charAt(text.length() - 1))) {
            return null;
        }
        try {
            final double number = Double.parseDouble(text);
            return Double.isFinite(number) ? number : null;
        } catch (final NumberFormatException e) {
            return null;
        }
    }
}
