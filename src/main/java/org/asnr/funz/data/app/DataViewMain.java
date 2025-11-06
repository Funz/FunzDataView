/*
 * Project        : FunzDataView
 * Website        : https://www.tbd
 * Copyright      : © ASNR
 *                  31 Avenue de la Division Leclerc
 *                  92260 Fontenay-aux-Roses, France
 *                  https://www.asnr.fr
 * Licence        : cf. LICENSE.txt
 * Developed By   : Artenum SARL
 * Authors        : Arnaud Trouche
 *                  Nicolas Chabalier
 *                  Julien Forest
 * Contract       : contract N°50000976 - order N°34007261
 */
package org.asnr.funz.data.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.asnr.funz.data.controller.ResultsController;
import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.model.ExtendedProject;
import org.funz.Project;
import org.funz.doeplugin.DesignConstants;
import org.keridwen.core.settings.file.RecentFiles;

import com.artenum.tk.ui.dialog.Alerts;
import com.artenum.tk.ui.file.RecentFilesUiOpenButtonFx;
import com.artenum.tk.ui.i18n.ArtTkDictionary;
import com.artenum.tk.ui.util.FxHelper;
import com.artenum.tk.ui.util.OsgiFxmlLoader;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Sample entry point for FunzDataView.<br>
 * <b>Not working right now: needs the HTML files and Funz plugins ...</b>
 *
 * @author Arnaud Trouche - Artenum SARL
 */
public final class DataViewMain extends Application implements Initializable {

    private static final int WIDTH = 1000;

    private static final int HEIGHT = 800;

    /**
     * @param args
     *         the laucnh arguments (ignored)
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    private final RecentFiles filesManager;

    @FXML
    private SplitMenuButton openButton;

    @FXML
    private BorderPane pane;

    /**
     * Default constructor.
     */
    public DataViewMain() {
        this.filesManager = RecentFiles.create(DataViewMain.class);
    }

    @Override
    public void start(final Stage primaryStage) {
        final Parent root = OsgiFxmlLoader.getRoot(this, "Application", ArtTkDictionary.class, ResultsDictionary.class);

        if (root != null) {

            // Create scene
            final Scene scene = new Scene(root, DataViewMain.WIDTH, DataViewMain.HEIGHT);

            // Set up the stage
            primaryStage.setWidth(DataViewMain.WIDTH);
            primaryStage.setHeight(DataViewMain.HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Funz Data View");

            // Show
            primaryStage.show();

        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Setup button
        RecentFilesUiOpenButtonFx.useButton(this.filesManager, this.openButton, this::loadProject);
    }

    /**
     * Show file chooser to select Funz project.
     */
    @FXML
    void showFileChooser() {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Funz project folder");
        chooser.setInitialDirectory(this.filesManager.getLastAnyRecentFile());
        final File chosenFile = chooser.showDialog(FxHelper.getWindow(this.pane));

        if (chosenFile != null) {
            this.loadProject(chosenFile);
        }
    }

    private void loadProject(final File projectFolder) {
        final ExtendedProject readProject;
        try {
            readProject = DataViewMain.readProject(projectFolder);
        } catch (final Exception e) {
            Alerts.showAlert(FxHelper.getWindow(this.pane), Alert.AlertType.ERROR, e.toString());
            return;
        }
        this.filesManager.addFile(projectFolder);

        // Create the result controller
        final ResultsController controller = new ResultsController(readProject);

        // Display
        this.pane.setCenter(controller.view());
    }

    private static ExtendedProject readProject(final File projectFolder) throws Exception {
        final File projectFile = DataViewMain.getProjectXmlFile(projectFolder);

        final ExtendedProject prj = new ExtendedProject(projectFile);
        prj.autoSelectOutputFunction();

        if (!prj.getDesignerId().equals(DesignConstants.NODESIGNER_ID)) {
            prj.loadDesignSessions();
        }
        prj.loadCases();
        if ((prj.getCases() != null) && !prj.getCases().isEmpty()) {
            prj.fireResultsSet();
        }
        return prj;
    }

    private static File getProjectXmlFile(final File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException("Project folder '" + dir + "' does not exists");
        } else {
            final File projectFile = new File(dir, Project.PROJECT_FILE);
            if (!projectFile.isFile()) {
                throw new IOException("Missing '" + Project.PROJECT_FILE + "' in project directory '" + dir + "'");
            }
            return projectFile;
        }
    }
}
