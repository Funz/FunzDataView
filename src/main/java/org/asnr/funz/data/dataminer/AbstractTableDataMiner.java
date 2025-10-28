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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import javafx.scene.control.TablePosition;
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

        // Export to CSV/XLS button
        final MenuButton exportToButton = new MenuButton(ResultsDictionary.EXPORT_TO.getString());
        final MenuItem exportToCSVItem = new MenuItem("CSV");
        final MenuItem exportToXLSItem = new MenuItem("XLS");
        exportToCSVItem.setOnAction(event -> this.exportTo("CSV"));
        exportToXLSItem.setOnAction(event -> this.exportTo("XLS"));
        exportToButton.getItems().add(exportToCSVItem);
        exportToButton.getItems().add(exportToXLSItem);

        final HBox toolbar = new HBox(5, hBox, multipleColumnChooserButton, exportToButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.setPadding(new Insets(5));

        final Group visibilityGroup = new Group(toolbar);

        this.valuesTable = new TableView<>();
        this.valuesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
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
     * Copy selected rows to clipboard with the column names at first line
     */
    @SuppressWarnings("rawtypes")
    private void copySelectionToClipboard() {
        final ObservableList<TablePosition> posList = this.valuesTable.getSelectionModel().getSelectedCells();
        final StringBuilder clipboardString = new StringBuilder();
        clipboardString.append(this.getColumnString(","));
        clipboardString.append(System.lineSeparator());

        for (final TablePosition p : posList) {
            final int r = p.getRow();
            clipboardString.append(this.getRowString(r, ","));
            clipboardString.append(System.lineSeparator());

        }
        final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(clipboardString.toString());
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
     * Get a String of names of all displayed columns separate by the separator
     */
    private String getColumnString(final String separator) {
        String sep = "";
        final StringBuilder sb = new StringBuilder();
        for (final TableColumn<CaseResults, ?> column : this.valuesTable.getColumns()) {
            sb.append(sep);
            sep = separator;
            sb.append(column.getText());
        }
        return sb.toString();
    }

    /**
     * Get a string of all values displayed in the table separated by the separator
     */
    private String getRowString(final int row, final String separator) {
        String sep = "";
        final StringBuilder sb = new StringBuilder();
        for (final TableColumn<CaseResults, ?> column : this.valuesTable.getColumns()) {
            final Object cellData = column.getCellData(row);
            sb.append(sep);
            sep = separator;
            if (cellData instanceof final DiscreteCaseResults discreteCaseResults) {
                sb.append(discreteCaseResults.getStringResult(column.getText()));
            } else {
                sb.append(cellData.toString());
            }
        }
        return sb.toString();
    }

    public void exportTo(File file, final String extension) {
        if (!file.getName().contains(".")) {
            final File originalFile = new File(file.getAbsolutePath());
            file = new File(file.getAbsolutePath() + "." + extension.toLowerCase());
            int cmp = 1;
            while (file.exists()) {
                file = new File(originalFile.getAbsolutePath() + "(" + cmp + ")" + "." + extension.toLowerCase());
                cmp++;
            }
        }

        Preferences.userRoot().put(AbstractTableDataMiner.EXPORT_VARIABLE_LAST_DIR, file.getParent());

        final List<String> variableNames = new ArrayList<>(this.getVariables());
        final List<String> outputNames = new ArrayList<>();
        for (final TableColumn<CaseResults, C> column : this.outputColumns().keySet()) {
            outputNames.add(column.getText());
        }

        if (extension.equalsIgnoreCase("csv")) {
            // Export as CSV
            this.exportToCsv(file, variableNames, outputNames);
        } else if (extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx")) {
            // Export as XLS/XLSX
            this.exportToXlsx(file, variableNames, outputNames);
        } else {
            LoggerFactory.getLogger(this.getClass()).error("Unsupported file format: {}", extension);
        }
    }

    private void exportToCsv(final File file, final List<String> variableNames, final List<String> outputNames) {
        try (final FileWriter fw = new FileWriter(file)) {
            final CharSequence delimiter = ";";

            // Write header
            fw.write(
                    Stream.concat(variableNames.stream(), outputNames.stream()).collect(Collectors.joining(delimiter)));
            fw.write(System.lineSeparator());

            // Write rows
            for (final CaseResults caseResult : this.valuesTable.getItems()) {
                final StringBuilder sb = new StringBuilder();

                for (final String variableName : variableNames) {
                    sb.append(caseResult.getValueForVariable(variableName));
                    sb.append(delimiter);
                }

                for (final String columnName : outputNames) {
                    final Object result = this.getResultForColumn(caseResult, columnName);
                    sb.append(this.toString(result));
                    sb.append(delimiter);
                }

                fw.write(sb.toString());
                fw.write(System.lineSeparator());
            }
        } catch (final IOException e) {
            LoggerFactory.getLogger(this.getClass()).error(e.getMessage());
        }
    }

    private void exportToXlsx(final File file, final List<String> variableNames, final List<String> outputNames) {
        try (final Workbook workbook = new XSSFWorkbook(); // For .xls, use HSSFWorkbook
                final FileOutputStream fos = new FileOutputStream(file)) {

            final Sheet sheet = workbook.createSheet("Export");
            int rowIndex = 0;

            // Write header
            final Row headerRow = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            for (final String name : Stream.concat(variableNames.stream(), outputNames.stream())
                    .toArray(String[]::new)) {
                headerRow.createCell(cellIndex++).setCellValue(name);
            }

            // Write rows
            for (final CaseResults caseResult : this.valuesTable.getItems()) {
                final Row row = sheet.createRow(rowIndex++);
                cellIndex = 0;

                for (final String variableName : variableNames) {
                    row.createCell(cellIndex++).setCellValue(caseResult.getValueForVariable(variableName));
                }

                for (final String columnName : outputNames) {
                    final Object result = this.getResultForColumn(caseResult, columnName);
                    row.createCell(cellIndex++).setCellValue(this.toString(result));
                }
            }

            workbook.write(fos);
        } catch (final IOException e) {
            LoggerFactory.getLogger(this.getClass()).error(e.getMessage());
        }
    }

    protected Object getResultForColumn(final CaseResults caseResult, final String columnName) {
        return caseResult.getResult(columnName);
    }

    public void displayColumnDialog(final Window window) {
        // Extract column status
        final List<ColumnVisibility> status = this.allColumns.stream()
                .map(c -> new ColumnVisibility(c.getText(), this.valuesTable.getColumns().contains(c))).toList();

        // Show dialog
        final List<ColumnVisibility> result = ColumnVisibilityDialog.show(window, status);

        // Apply if changed
        this.updateColumns(result);
    }

    private void updateColumns(final List<ColumnVisibility> visibilities) {
        // Extract column name to visibility
        final Map<String, BooleanProperty> map = visibilities.stream()
                .collect(Collectors.toMap(ColumnVisibility::name, ColumnVisibility::visible));

        // Remove all columns
        this.valuesTable.getColumns().clear();

        // Add visible columns
        for (final TableColumn<CaseResults, ?> column : this.allColumns) {
            final boolean visible = Optional.ofNullable(map.get(column.getText())) //
                    .map(BooleanProperty::get) //
                    .orElse(false);
            if (visible) {
                // Re-add the column
                this.addColumn(column);
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
        } else if (extension.equals("XLS")) {
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XLS files (*.xls)", "*.xls"));
        }

        final File file = fileChooser.showSaveDialog(null);
        if (file == null) {
            JOptionPane.showMessageDialog(null, ResultsDictionary.ERROR_SELECT_FILE.getString());
        } else {
            this.exportTo(file, extension);
        }
    }

    private String toString(final Object arr) {
        if (arr instanceof final Object[] obj) {
            return Arrays.toString(obj);
        } else {
            if (arr instanceof final double[] doubles) {
                return Arrays.toString(doubles);
            } else {
                return arr.toString();
            }

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

        if (variables.size() > 1 || !variables.getFirst().equals(Project.SINGLE_PARAM_NAME)) {
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
     * @return discrete selected cases
     */
    @SuppressWarnings("rawtypes")
    public List<Case> getDiscreteSelectedCases() {
        final ObservableList<TablePosition> posList = this.valuesTable.getSelectionModel().getSelectedCells();
        final List<Case> selectedCases = new ArrayList<>();
        for (final TablePosition p : posList) {
            final int r = p.getRow();
            final CaseResults caseResult = this.valuesTable.getItems().get(r);
            if (caseResult instanceof final DiscreteCaseResults discrete) {
                selectedCases.add(discrete.getCase());
            }
        }
        return selectedCases;
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
