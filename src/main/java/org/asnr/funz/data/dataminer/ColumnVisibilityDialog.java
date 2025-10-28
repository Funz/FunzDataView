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

import java.util.List;

import org.asnr.funz.data.i18n.ResultsDictionary;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Display dialog to select visible columns.
 *
 * @author Arnaud Trouche - Artenum SARL
 */
final class ColumnVisibilityDialog {

    /**
     * @param window
     *         the parent
     * @param visibilities
     *         the list of column visibilities
     * @return the user selection or the input visibilitis if cancelled
     */
    static List<ColumnVisibility> show(final Window window, final List<ColumnVisibility> visibilities) {
        final ColumnVisibilityDialog instance = new ColumnVisibilityDialog(window, visibilities);
        return instance.dialog.showAndWait().orElse(visibilities);
    }

    private final Dialog<List<ColumnVisibility>> dialog;

    private final TableView<ColumnVisibility> table;

    private ColumnVisibilityDialog(final Window window, final List<ColumnVisibility> currentVisibility) {
        // Select all / Unselect all
        final HBox toolbar = this.createToolbar();

        // Table
        this.table = new TableView<>();
        this.table.setEditable(true);
        final TableColumn<ColumnVisibility, Boolean> selection = new TableColumn<>();
        selection.setEditable(true);
        selection.setCellValueFactory(cdf -> cdf.getValue().visible());
        selection.setCellFactory(CheckBoxTableCell.forTableColumn(selection));
        selection.setMaxWidth(30);
        this.table.getColumns().add(selection);
        final TableColumn<ColumnVisibility, String> name = new TableColumn<>(ResultsDictionary.NAME.getString());
        name.setCellValueFactory(cdf -> new SimpleStringProperty(cdf.getValue().name()));
        this.table.getColumns().add(name);
        this.table.getItems().addAll(currentVisibility);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);

        // Dialog
        this.dialog = new Dialog<>();
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initOwner(window);
        this.dialog.setTitle(ResultsDictionary.COLUMN_SELECTOR.getString());

        // Buttons && Converter
        this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        this.dialog.setResultConverter(btn -> {
            if (btn == ButtonType.APPLY) {
                return this.table.getItems();
            } else {
                return currentVisibility;
            }
        });

        this.dialog.getDialogPane().setContent(new VBox(10, toolbar, this.table));
        VBox.setVgrow(this.table, Priority.ALWAYS);
    }

    private HBox createToolbar() {
        // Select all
        final Button selectAll = new Button(ResultsDictionary.SELECT_ALL.getString());
        selectAll.setOnAction(event -> {
            for (final ColumnVisibility row : this.table.getItems()) {
                if (!row.visible().get()) {
                    row.visible().set(true);
                }
            }
        });

        // Unselect all
        final Button unselectAll = new Button(ResultsDictionary.UNSELECT_ALL.getString());
        unselectAll.setOnAction(event -> {
            for (final ColumnVisibility row : this.table.getItems()) {
                if (row.visible().get()) {
                    row.visible().set(false);
                }
            }
        });

        // Resulting toolbar
        final HBox buttons = new HBox(10, selectAll, unselectAll);
        buttons.setAlignment(Pos.CENTER);
        return buttons;
    }

}
