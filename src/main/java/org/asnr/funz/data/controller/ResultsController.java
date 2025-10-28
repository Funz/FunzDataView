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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.asnr.funz.data.dataminer.TableDataMiner;
import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.model.ExtendedProject;

import com.artenum.tk.ui.util.OsgiFxmlLoader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;

/**
 * Controller for the main view of the results pane
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public final class ResultsController implements Initializable {

    /**
     * Controller of the tab displaying the result files.
     */
    private final FilesTabController filesController;

    /**
     * Controller of the tab displaying in different ways the result values.
     */
    private final DataTabController viewsController;

    /**
     * Controller of the tab displaying in different ways the DOE values.
     */
    private final AnalysisTabController doeController;

    @FXML
    private TabPane tabs;

    @FXML
    private Tab filesTab;

    @FXML
    private Tab dataTab;

    private boolean onlyFilesTab = false;

    /**
     * Creates a new Analysis central controller for the given Project
     *
     * @param aProject
     *         the current project
     */
    public ResultsController(final ExtendedProject aProject) {
        this.filesController = new FilesTabController(aProject);
        this.viewsController = new DataTabController(aProject);
        this.doeController = new AnalysisTabController(aProject);
    }

    /**
     * Creates a new Analysis central controller for the given Project
     *
     * @param aProject
     *         the current project
     * @param onlyFilesTab
     *         only display the "Files" tab
     */
    public ResultsController(final ExtendedProject aProject, final boolean onlyFilesTab) {
        this(aProject);
        this.onlyFilesTab = onlyFilesTab;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Fill the different tabs
        this.filesTab.setContent(this.filesController.getView());

        if (!this.onlyFilesTab) {
            this.dataTab.setContent(this.viewsController.getView());
            final Tab analysisTab = new Tab(ResultsDictionary.ANALYSIS.getString(), this.doeController.getView());

            this.doeController.hasContentProperty().addListener((c, o, n) -> Platform.runLater(() -> {
                if (!o.booleanValue() && n.booleanValue()) {
                    this.tabs.getTabs().add(analysisTab);
                } else if (o.booleanValue() && !n.booleanValue()) {
                    this.tabs.getTabs().remove(analysisTab);
                }
            }));

            // Notify the results tab when it is selected
            ResultsController.addDynamicUpdate(this.dataTab, this.viewsController);
            ResultsController.addDynamicUpdate(analysisTab, this.doeController);

            // Refresh DOE HTML files
            this.doeController.refresh(false);
        } else {
            this.tabs.getTabs().remove(this.dataTab);
        }
    }

    /**
     * @return the view of the results
     */
    public TabPane view() {
        return (TabPane) OsgiFxmlLoader.getRoot(this, "PaneContent", ResultsDictionary.class);
    }

    public TreeView<File> getFileTreeView() {
        return this.filesController.getFileTreeView();
    }

    public void selectFileItem(final String fileRegex) {
        this.selectFileItem(null, fileRegex);
    }

    public void selectFileItem(final String pathRegex, final String fileRegex) {
        this.tabs.getSelectionModel().select(this.filesTab);
        this.filesController.selectItem(pathRegex, fileRegex);
    }

    public void setFileEditorDisable(final boolean disable) {
        this.filesController.setEditorDisable(disable);
    }

    public void selectTableTab() {
        final SingleSelectionModel<Tab> selectionModel = this.tabs.getSelectionModel();
        selectionModel.select(this.dataTab);
    }

    /**
     * Set output data to display by default (checkbox to select by default in views)
     */
    public void setDefaultOutputSelection(final List<String> outputs) {
        this.viewsController.setDefaultOutputSelection(outputs);
    }

    /**
     * Get list of all selected output name
     */
    public List<String> getSelectedColumnsNames() {
        return this.viewsController.getSelectedColumnsNames();
    }

    public javafx.scene.control.ContextMenu getTableContextMenu() {
        return this.viewsController.getTableContextMenu();
    }

    public TableDataMiner getTableDataMiner() {
        return this.viewsController.getTableDataMiner();
    }

    public void openFile(final File file) {
        this.filesController.open(file);
    }

    public void exportTo(final File file, final String extension) {
        this.viewsController.getTableDataMiner().exportTo(file, extension);
    }

    private static void addDynamicUpdate(final Tab tab, final AbstractDynamicUpdateController controller) {
        tab.selectedProperty().addListener((o, wasSelected, isSelected) -> {
            if (!wasSelected.booleanValue() && isSelected.booleanValue()) {
                controller.setShowing(true);
            } else if (wasSelected.booleanValue() && !isSelected.booleanValue()) {
                controller.setShowing(false);
            }
        });
    }

}
