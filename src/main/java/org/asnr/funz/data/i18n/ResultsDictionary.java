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
package org.asnr.funz.data.i18n;

import org.funz.doeplugin.DesignConstants;
import org.keridwen.core.i18n.entry.EntryType;

import com.artenum.tk.i18n.GuiDictionary;

import javafx.fxml.FXML;

/**
 * Dictionary entries for the data view.
 *
 * @author Arnaud Trouche - Artenum SARL
 */
@SuppressWarnings({ "MissingJavadoc" })
public enum ResultsDictionary implements GuiDictionary {
    ALGORITHM_STATUS_NO_DESIGN_NEEDS_VARIABLES(DesignConstants.NODESIGNER_ID + " needs user defined variables"),
    ALGORITHM_STATUS_NO_ISSUE_DEFINED("No issue defined"),
    @FXML
    ALWAYS_UPDATE("Live Update"),
    ANALYSIS("Analysis"),
    BINARY_FILE("Selected file is binary"),
    @FXML
    COLLAPSE_16("icons/16/collapse.png"),
    @FXML
    COLLAPSE_TOOLTIP("Collapse"),
    COLUMNS("Columns ..."),
    COLUMN_SELECTOR("Column selector"),
    @FXML
    COMPARE_FILES("Compare files"),
    COPY("Copy"),
    @FXML
    DATA("Data"),
    @FXML
    DELETE_16("icons/16/delete.png"),
    DIFFERENTIAL_VIEW("Differential View"),
    EMPTY_FILE("Selected file is empty"),
    ERROR_SELECT_FILE("Please, could you select a file"),
    ERROR_WHILE_GETTING_RESULT("An error occurred while trying to get the results : %s"),
    ERROR_WHILE_LOADING_REPORT("Unable to load the generated report."),
    ERROR_WHILE_OPENING_FILE("Unable to open the file %s"),
    ERROR_WHILE_SAVING_PROJECT("Error while saving project : %s"),
    @FXML
    EXPAND_16("icons/16/expand.png"),
    @FXML
    EXPAND_TOOLTIP("Expand"),
    EXPORT_TO("Export to ..."),
    @FXML
    FILES("Files"),
    FILES_EDITOR_COLOR_TOGGLE(EntryType.COLOR, "#B4FAB4"),
    GROUP_VARIABLE("Group variable:"),
    HTML("HTML"),
    INPUT_VARIABLES_STATUS_INDEPENDENT_CASES(" independant case(s)"),
    INPUT_VARIABLES_STATUS_INVALID_VARIABLES("Invalid variable(s):"),
    LEFT_16("icons/16/left.png"),
    MODEL_STATUS_OK("OK."),
    NAME("Name"),
    NO_EXTENSION_IN_NAME(
            "Extension file 'plugins/file/%s' does not contains any extensions in its name (ext-EXT1-EXT2-...-EXTN"
                    + ".html)"),
    NO_RESULT_MESSAGE("There is currently no result to display."),
    NO_VARIABLE_IN_FILE(
            "Extension file 'plugins/file/%s' does not contains the necessary variables to be used to display results"
                    + "."),
    @FXML
    OPEN_IN_SYSTEM("Open in system"),
    OUTPUT_FUNCTION("Output function:"),
    OUTPUT_FUNCTION_STATUS_INVALID_OUTPUT_FUNCTION("Invalid output function %s: %s not set"),
    OUTPUT_FUNCTION_STATUS_NO_OUTPUT_FUNCTION_SELECTED("No output function selected"),
    PARSING_RESULTS("Parsing results..."),
    RIGHT_16("icons/16/right.png"),
    SEARCH_BAR_NEXT_TOOLTIP("Previous occurrence"),
    SEARCH_BAR_PREVIOUS_TOOLTIP("Next occurrence"),
    SELECT_ALL("Select all"),
    SELECT_FILE("Please select a file"),
    UNABLE_TO_LOAD_FILE("Unable to load file '%s': %s"),
    UNSELECT_ALL("Unselect all"),
    @FXML
    UPDATE_NOW("Update"),
    VALUES("Values"),
    @FXML
    VIEWS("Views"),
    WARNING_COLOR_TXT("<font color='orange'>");

    private final EntryType entryType;
    private final String localized;

    ResultsDictionary(final EntryType aType, final String aString) {
        this.entryType = aType;
        this.localized = aString;
    }

    ResultsDictionary(final String aString) {
        this(EntryType.STRING, aString);
    }

    @Override
    public String getDefaultString() {
        return this.localized;
    }

    @Override
    public EntryType getEntryType() {
        return this.entryType;
    }

}
