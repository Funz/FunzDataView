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
package org.asnr.funz.data.view.utils;

import java.util.Comparator;

import org.asnr.funz.data.model.DiscreteCaseResults;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Utility methods for FX {@link TableView}.
 *
 * @author Arnaud Trouche - Artenum SARL
 */
public final class TableViewUtils {

    /**
     * @param <T>
     *         the type of data in the table
     * @param table
     *         the TableView to count
     * @return the number of visible columns for this table view
     */
    public static <T> long getVisibleColumns(final TableView<T> table) {
        return table.getColumns().stream().filter(TableColumn::isVisible).count();
    }

    /**
     * @param <T>
     *         the type of data in the table
     * @param table
     *         the TableView to resize
     */
    public static <T> void autoResizeColumns(final TableView<T> table) {
        final long nbCols = TableViewUtils.getVisibleColumns(table);

        // First unbind
        table.getColumns().forEach(c -> c.prefWidthProperty().unbind());

        // Bind size
        table.getColumns().stream()
                .forEach(c -> c.prefWidthProperty().bind(table.widthProperty().subtract(20).divide(nbCols)));

    }

    private TableViewUtils() {
        // Suppress default constructor for noninstantiability.
        throw new AssertionError();
    }

    public static Comparator<DiscreteCaseResults> createDiscretCaseResultsColumnComparator(final String name) {
        return (o1, o2) -> {
            final Object r1 = o1.getResult(name);
            final Object r2 = o2.getResult(name);
            if (r1 == null || r2 == null) {
                if (r1 != null) {
                    return 1;
                } else if (r2 != null) {
                    return -1;
                } else {
                    return 0;
                }
            }
            if (r1 instanceof final Comparable comparable) {
                return comparable.compareTo(r2);
            } else {
                if (r1 instanceof final double[] array1 && r2 instanceof final double[] array2 && array1.length > 0
                        && array2.length > 0) {
                    return Double.compare(array1[0], array2[0]);
                }
                return r1.toString().compareTo(r2.toString());
            }
        };
    }

    public static Comparator<String> createStringColumnComparator() {
        return (s1, s2) -> {

            if (s1 == null && s2 == null) {
                return 0;
            } else if (s1 != null) {
                return 1;
            } else if (s2 != null) {
                return -1;
            }

            try {
                final Double num1 = Double.parseDouble(s1);
                final Double num2 = Double.parseDouble(s2);
                return num1.compareTo(num2);
            } catch (final NumberFormatException e) {
                return s1.compareTo(s2);
            }
        };
    }

}
