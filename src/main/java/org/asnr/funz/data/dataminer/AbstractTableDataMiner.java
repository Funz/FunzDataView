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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.model.DiscreteCaseResults;
import org.asnr.funz.data.model.impl.DefaultResultModel;
import org.asnr.funz.data.view.VariableResultTableCell;
import org.asnr.funz.data.view.utils.TableViewUtils;
import org.asnr.funz.model.ExtendedProject;
import org.funz.Project;
import org.funz.parameter.Case;
import org.slf4j.LoggerFactory;

import com.artenum.tk.ui.i18n.ArtTkDictionary;
import com.artenum.tk.ui.util.FxHelper;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * Data miner that displays the result values in a table.
 *
 * @param <R>
 *         the type of {@link CaseResults}
 * @param <C>
 *         the type of data inside the result column
 * @author Arnaud TROUCHE - Artenum SARL
 */
abstract class AbstractTableDataMiner<R extends CaseResults, C> extends AbstractDataMiner implements DataMiner {

    /**
     * Name of the preference used to store the latest CSV/XSL files.
     */
    private static final String EXPORT_VARIABLE_LAST_DIR = "org.asnr.funz.dataview.export.lastdir";

    /**
     * Variable/parameter name prefix for column
     */
    public static final String VAR_PREFIX = "var.";

    private final List<TableColumn<CaseResults, ?>> allColumns;

    private final ComboBox<String> groupVariableCombo;

    private final TableView<CaseResults> valuesTable;

    private final VBox view;

    private List<ColumnVisibility> defaultOutputSelection;

    private final ContextMenu contextMenu;

    private final HashMap<String, String> customParameters;

    /**
     * Default constructor.
     *
     * @param currentProject
     *         the current {@link ExtendedProject}
     */
    protected AbstractTableDataMiner(final ExtendedProject currentProject) {
        super(currentProject, ResultsDictionary.VALUES.getString());
        this.allColumns = new ArrayList<>();

        // Group variable and do the mean for values
        this.groupVariableCombo = new ComboBox<>();
        this.groupVariableCombo.getItems().addAll("None");
        this.groupVariableCombo.getItems().addAll(currentProject.getResultModel().getDiscreteVariables());
        this.groupVariableCombo.setValue("None");
        this.groupVariableCombo.setOnAction(e -> {
            final String selectedVariable = this.groupVariableCombo.getValue();
            ((DefaultResultModel) currentProject.getResultModel()).groupVariable(selectedVariable);
            this.refreshCases();
        });
        final Label groupVarLabel = new Label(ResultsDictionary.GROUP_VARIABLE.getString());
        final HBox hBox = new HBox(5, groupVarLabel, this.groupVariableCombo);
        hBox.setAlignment(Pos.CENTER);

        final Button multipleColumnChooserButton = new Button();
        multipleColumnChooserButton.setText(ResultsDictionary.COLUMNS.getString());
        multipleColumnChooserButton.setOnAction(
                e -> this.displayColumnDialog(FxHelper.getWindow(multipleColumnChooserButton)));

        // Export to CSV/XLSX button
        final MenuButton exportToButton = new MenuButton(ResultsDictionary.EXPORT_TO.getString());
        final MenuItem exportToCSVItem = new MenuItem("CSV");
        final MenuItem exportToXLSXItem = new MenuItem("XLSX");
        exportToCSVItem.setOnAction(event -> this.exportTo("CSV"));
        exportToXLSXItem.setOnAction(event -> this.exportTo("XLSX"));
        exportToButton.getItems().add(exportToCSVItem);
        exportToButton.getItems().add(exportToXLSXItem);

        final HBox toolbar = new HBox(5, hBox, multipleColumnChooserButton, exportToButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.setPadding(new Insets(5));

        final Group visibilityGroup = new Group(toolbar);

        this.valuesTable = new TableView<>();
        this.valuesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        this.valuesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Copy to clipboard selected rows with a menu
        final MenuItem item = new MenuItem(ResultsDictionary.COPY.getString());
        item.setOnAction(event -> this.copySelectionToClipboard());
        this.contextMenu = new ContextMenu();
        this.contextMenu.getItems().add(item);
        this.valuesTable.setContextMenu(this.contextMenu);

        // Copy to clipboard selected rows with CTRL+C
        final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        this.valuesTable.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event)) {
                this.copySelectionToClipboard();
            }
        });

        // Create view
        this.view = new VBox(visibilityGroup, this.valuesTable);
        this.view.setAlignment(Pos.CENTER_RIGHT);
        VBox.setVgrow(this.valuesTable, Priority.ALWAYS);

        this.customParameters = new HashMap<>();
    }

    public Map<String, String> getCustomParameters() {
        return this.customParameters;
    }

    /**
     * Copy the selected rows to the clipboard, with the names of the displayed columns at first line, separated by
     * tabulations so that a spreadsheet splits them when pasting
     */
    private void copySelectionToClipboard() {
        final List<String> header = this.valuesTable.getColumns().stream().map(TableColumn::getText).toList();
        final List<List<Object>> rows = this.valuesTable.getSelectionModel().getSelectedIndices().stream()
                .distinct()
                .sorted()
                .map(this::getRowValues)
                .toList();
        final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(TableExport.toClipboardText(header, rows));
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
    }

    @Override
    public Node getView() {
        return this.view;
    }

    @Override
    public void clear() {
        Platform.runLater(() -> {
            this.valuesTable.getColumns().clear();
            this.valuesTable.getColumns().add(new TableColumn<>(ResultsDictionary.DATA.getString()));
            this.valuesTable.getItems().clear();
        });
    }

    @Override
    public void refreshCases() {
        Platform.runLater(() -> this.valuesTable.getItems().setAll(this.getResults()));
    }

    @Override
    public void refreshVariables() {
        Platform.runLater(this::recomputeColumns);
    }

    /**
     * @return the valuesTable
     */
    public final TableView<CaseResults> getTable() {
        return this.valuesTable;
    }

    /**
     * @return the result for this table
     */
    protected abstract Collection<R> getResults();

    /**
     * @return the variable names applicable for this table
     */
    protected abstract Collection<String> getVariables();

    /**
     * Create the column displaying the result, depending on the type
     *
     * @param name
     *         the name of the column
     * @return the created column
     */
    protected abstract TableColumn<CaseResults, C> createColumn(String name);

    /**
     * Create additional columns
     *
     * @return the columns to add (true to add straightway, false otherwise)
     */
    protected abstract Map<TableColumn<CaseResults, C>, Boolean> outputColumns();

    /**
     * @return the values of the displayed columns in the given row
     */
    private List<Object> getRowValues(final int row) {
        final List<Object> values = new ArrayList<>();
        for (final TableColumn<CaseResults, ?> column : this.valuesTable.getColumns()) {
            final Object cellData = column.getCellData(row);
            if (cellData instanceof final DiscreteCaseResults discreteCaseResults) {
                values.add(discreteCaseResults.getStringResult(column.getText()));
            } else {
                values.add(cellData);
            }
        }
        return values;
    }

    /**
     * Exports the table in a file.
     *
     * @param file
     *         the file to write, completed by the extension of the format when it has none
     * @param extension
     *         the format: CSV, or XLSX for a workbook of Excel
     */
    public void exportTo(File file, final String extension) {
        if (!file.getName().contains(".")) {
            final String fileExtension = "." + extension.toLowerCase();
            final File originalFile = new File(file.getAbsolutePath());
            file = new File(file.getAbsolutePath() + fileExtension);
            int cmp = 1;
            while (file.exists()) {
                file = new File(originalFile.getAbsolutePath() + "(" + cmp + ")" + fileExtension);
                cmp++;
            }
        }

        Preferences.userRoot().put(AbstractTableDataMiner.EXPORT_VARIABLE_LAST_DIR, file.getParent());

        // Same ordering as the table display
        final List<String> variableNames = this.getVariables().stream().sorted().toList();
        final List<String> outputNames = this.outputColumns().keySet().stream()
                .map(TableColumn::getText)
                .sorted(String::compareToIgnoreCase)
                .toList();

        final List<String> header = Stream.concat(variableNames.stream(), outputNames.stream()).toList();
        final List<List<Object>> rows = new ArrayList<>();
        for (final CaseResults caseResult : this.valuesTable.getItems()) {
            final List<Object> row = new ArrayList<>();
            variableNames.forEach(variableName -> row.add(caseResult.getValueForVariable(variableName)));
            outputNames.forEach(columnName -> row.add(this.getResultForColumn(caseResult, columnName)));
            rows.add(row);
        }

        try {
            if (extension.equalsIgnoreCase("csv")) {
                TableExport.writeCsv(file, header, rows);
            } else if (extension.equalsIgnoreCase("xlsx")) {
                TableExport.writeWorkbook(file, header, rows);
            } else {
                LoggerFactory.getLogger(this.getClass()).error("Unsupported file format: {}", extension);
            }
        } catch (final IOException | RuntimeException e) {
            LoggerFactory.getLogger(this.getClass()).error("Unable to export the results in {}", file, e);
        }
    }

    protected Object getResultForColumn(final CaseResults caseResult, final String columnName) {
        return caseResult.getResult(columnName);
    }

    public void displayColumnDialog(final Window window) {
        // Keep the current table order for visible columns.
        final List<ColumnVisibility> status = this.valuesTable.getColumns().stream()
                .map(column -> new ColumnVisibility(column.getText(), true))
                .collect(Collectors.toCollection(ArrayList::new));

        // Add hidden columns after the visible ones, using their original order.
        this.allColumns.stream()
                .filter(column -> !this.valuesTable.getColumns().contains(column))
                .map(column -> new ColumnVisibility(column.getText(), false))
                .forEach(status::add);

        // Show dialog
        final List<ColumnVisibility> result =
                ColumnVisibilityDialog.show(window, status);

        // Apply the selected visibility/order.
        this.updateColumns(result);
    }

    private void updateColumns(final List<ColumnVisibility> visibilities) {
        // Remove all columns
        this.valuesTable.getColumns().clear();

        // Add visible columns in the requested order.
        for (final ColumnVisibility visibility : visibilities) {
            if (visibility.visible().get()) {
                this.allColumns.stream() //
                        .filter(column -> column.getText().equals(visibility.name())) //
                        .findFirst() //
                    .ifPresent(this.valuesTable.getColumns()::add);
            }
        }

        TableViewUtils.autoResizeColumns(this.valuesTable);
    }

    /**
     * Save the table in a CSV file
     */
    public void exportTo(final String extension) {

        // Creating a File chooser
        final FileChooser fileChooser = new FileChooser();
        final File lastSaveDir = new File(
                Preferences.userRoot().get(AbstractTableDataMiner.EXPORT_VARIABLE_LAST_DIR, "."));
        if (lastSaveDir.exists()) {
            fileChooser.setInitialDirectory(lastSaveDir);
        }
        fileChooser.setTitle(ArtTkDictionary.SAVE.getString());
        if (extension.equals("CSV")) {
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV files (*.csv)", "*.csv"));
        } else if (extension.equals("XLSX")) {
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
        }

        // nothing to do when the user cancels
        final File file = fileChooser.showSaveDialog(this.valuesTable.getScene().getWindow());
        if (file != null) {
            this.exportTo(file, extension);
        }
    }

    private void recomputeColumns() {
        this.allColumns.clear();
        this.valuesTable.getColumns().clear();

        // We create a column for each variable, if there are different from
        // "all-in-one"
        // They will always be visible
        this.parametersColumns();

        // We add a column for each result
        final List<Entry<TableColumn<CaseResults, C>, Boolean>> outputs = this.outputColumns().entrySet().stream() //
                .sorted((f, s) -> f.getKey().getText().compareToIgnoreCase(s.getKey().getText())) //
                .toList();

        for (final Entry<TableColumn<CaseResults, C>, Boolean> entry : outputs) {
            final TableColumn<CaseResults, C> column = entry.getKey();
            final boolean display = entry.getValue();
            if (display) {
                this.addColumn(column);
                this.allColumns.add(column);
            } else {
                this.allColumns.add(column);
            }

        }

        if (this.defaultOutputSelection != null) {
            this.updateColumns(this.defaultOutputSelection);
        }

        // Proportional sizes
        TableViewUtils.autoResizeColumns(this.valuesTable);
    }

    private void parametersColumns() {
        final List<String> variables = this.getVariables().stream().sorted().toList();

        if (variables.size() > 1 || (variables.size() == 1 && !variables.getFirst().equals(
                Project.SINGLE_PARAM_NAME))) {
            for (final String variableName : variables) {
                final String variableNameModified = AbstractTableDataMiner.VAR_PREFIX + variableName;
                final TableColumn<CaseResults, String> column = new TableColumn<>(variableNameModified);
                column.setComparator(TableViewUtils.createStringColumnComparator());
                column.setCellValueFactory(p -> {
                    final String valueForVariable = p.getValue().getValueForVariable(variableName);
                    if (valueForVariable != null) {
                        return new SimpleStringProperty(valueForVariable);
                    } else {
                        return new SimpleStringProperty(this.customParameters.getOrDefault(variableName, ""));
                    }
                });
                column.getStyleClass().add("align-center");
                column.setCellFactory(param -> new VariableResultTableCell());

                this.addColumn(column);
                this.allColumns.add(column);

            }
        }
    }

    /**
     * Add column to the right place: we want all parameter column first then all
     * output columns We add the column to the end or after all parameter columns if
     * it is a parameter column
     */
    private void addColumn(final TableColumn<CaseResults, ?> column) {
        if (column.getText().startsWith(AbstractTableDataMiner.VAR_PREFIX)) {
            this.valuesTable.getColumns().add(this.getLastParameterColumnIdx(), column);
        } else {
            this.valuesTable.getColumns().add(column);
        }
    }

    /**
     * @return the index of the last parameter column
     */
    private int getLastParameterColumnIdx() {
        int idx = 0;
        final ObservableList<TableColumn<CaseResults, ?>> columns = this.valuesTable.getColumns();
        final int nbColumns = columns.size();
        while (idx < nbColumns && columns.get(idx).getText().startsWith(AbstractTableDataMiner.VAR_PREFIX)) {
            idx++;
        }
        return idx;
    }

    /**
     * Set output data to display by default (column checkbox to select by default
     * in the table)
     */
    public void setDefaultOutputSelection(final List<String> outputs) {
        this.defaultOutputSelection = outputs.stream().map(n -> new ColumnVisibility(n, true)).toList();
    }

    /**
     * @return the cases of the selected rows, once each, in the order of their selection
     */
    public List<Case> getDiscreteSelectedCases() {
        return this.valuesTable.getSelectionModel().getSelectedItems().stream()
                .distinct()
                .filter(DiscreteCaseResults.class::isInstance)
                .map(caseResult -> ((DiscreteCaseResults) caseResult).getCase())
                .toList();
    }

    /**
     * Get list of all column name selected
     */
    public List<String> getSelectedColumnsNames() {
        return this.valuesTable.getColumns().stream().map(TableColumn::getText).toList();
    }

    public ContextMenu getContextMenu() {
        return this.contextMenu;
    }

}
