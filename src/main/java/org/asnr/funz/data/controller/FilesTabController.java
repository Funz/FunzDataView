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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.data.view.HtmlFileViewer;
import org.asnr.funz.data.view.HtmlVariablesUtils;
import org.asnr.funz.data.view.common.SearchBar;
import org.asnr.funz.model.ExtendedProject;
import org.funz.parameter.Case;
import org.funz.util.Disk;
import org.keridwen.modelling.ace.text.editor.DiffTextEditor;
import org.keridwen.modelling.ace.text.editor.SuperTextEditor;
import org.keridwen.modelling.ace.text.editor.TextEditorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artenum.tk.ui.control.tree.fx.FxTreeUtils;
import com.artenum.tk.ui.control.tree.fx.SearchableTreeItem;
import com.artenum.tk.ui.util.FxHelper;
import com.artenum.tk.ui.util.OsgiFxmlLoader;
import com.artenum.tk.web.WebViewSynchronousScriptExecutor;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Controller for the result files tree and its viewer.
 *
 * @author Yann RICHET - IRSN
 * @author Arnaud TROUCHE - Artenum SARL
 */
final class FilesTabController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(FilesTabController.class);
    /**
     * Current {@link ExtendedProject}.
     */
    private final ExtendedProject project;

    /**
     * Map storing the extensions directly handled by the HTML files.
     */
    private final Map<String, HtmlFileViewer> extensionsToFileViewer;
    /**
     * {@link SuperTextEditor} where the content of the selected file is displayed.
     */
    private final SuperTextEditor editor;

    /**
     * Content loader for the webview.
     */
    private WebViewSynchronousScriptExecutor webviewLoader;

    @FXML
    private SplitPane splitPane;
    @FXML
    private TextField filesSearchField;
    @FXML
    private Button deleteSearchButton;
    @FXML
    private TreeView<File> filesTree;
    @FXML
    private ContextMenu filesTreeContextMenu;
    @FXML
    private MenuItem openDiffViewer;

    @FXML
    private VBox rightSide;
    @FXML
    private ToolBar editorSearchContainer;
    @FXML
    private StackPane editorContainer;
    @FXML
    private WebView webview;

    @FXML
    private CheckBox alwaysUpdateCheckbox;

    /**
     * Default constructor
     *
     * @param project
     *         {@link ExtendedProject}
     */
    public FilesTabController(final ExtendedProject project) {
        this.project = project;
        this.extensionsToFileViewer = this.loadHtmlFiles();
        this.editor = TextEditorFactory.getNanoTextEditor(true);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.project.addStatusListener((modifiedProject, newStatus) -> {
            switch (newStatus) {
            case INITIAL, RUNNING -> Platform.runLater(this::clearTree);
            case RESULTS, FAILED -> Platform.runLater(this::fillTree);
            }
        });

        this.project.addCaseModifiedListener((index, what) -> {
            final Case caseToAdd = this.project.getCases().get(index);
            if (this.alwaysUpdateCheckbox.isSelected() && caseToAdd.hasRun() && what == Case.MODIFIED_STATE) {
                this.fillTree();
            }
        });
        this.webviewLoader = new WebViewSynchronousScriptExecutor(this.webview);

        this.deleteSearchButton.setGraphic(new ImageView(ResultsDictionary.DELETE_16.getPicture()));
        this.deleteSearchButton.disableProperty().bind(this.filesSearchField.textProperty().isEmpty());

        this.initializeFileTree();
        this.filesTree.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<TreeItem<File>>) c -> this.openDiffViewer.setDisable(c.getList().size() != 2));

        this.filesSearchField.textProperty().addListener((obs, oldValue, newValue) -> {
            // expand the tree to see the results
            this.expandButtonPressed();
            final SearchableTreeItem<File> maybeRoot = (SearchableTreeItem<File>) this.filesTree.getRoot();
            if (maybeRoot != null) {
                maybeRoot.predicateProperty()
                        .set(t -> t == null || t.getName().contains(this.filesSearchField.getCharacters()));
            }
            this.filesSearchField.requestFocus();
        });

        this.editorSearchContainer.getItems().add(new SearchBar(this.editor));
        this.editorContainer.getChildren().clear();
        this.addNodeSafely(this.editor.getView());

        this.fillTree();
        this.open(null);
    }

    public TreeView<File> getFileTreeView() {
        return this.filesTree;
    }

    public void setEditorDisable(final boolean disable) {
        this.editor.setDisable(disable);
    }

    /**
     * Action to perform when the user requests an open in system.
     */
    @FXML
    private void openFileInSystem() {
        final File file = this.filesTree.getSelectionModel().getSelectedItem().getValue();
        if (file != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (final IOException e) {
                    FilesTabController.log.error(e.toString(), e);
                }
            });
        }
    }

    /**
     * Act
     */
    @FXML
    private void openDiffViewer() {
        final ObservableList<TreeItem<File>> files = this.filesTree.getSelectionModel().getSelectedItems();

        if (files.size() == 2) {
            final DiffTextEditor diffTextEditor = TextEditorFactory.getDiffTextEditor();

            final Stage stage = new Stage();
            stage.setTitle(ResultsDictionary.DIFFERENTIAL_VIEW.getString());
            final double width = 950;
            final double height = 680;
            stage.setScene(new Scene(diffTextEditor.getView(), width, height));
            stage.initOwner(FxHelper.getWindow(this.filesTree));
            stage.show();

            final File file1 = files.get(0).getValue();
            final File file2 = files.get(1).getValue();
            Executors.defaultThreadFactory().newThread(() -> {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                Platform.runLater(() -> {
                    diffTextEditor.setDisable(true);
                    diffTextEditor.openFiles(file1, file2);
                });
            }).start();
        }
    }

    /**
     * Action to perform when user requests an expand.
     */
    @FXML
    private void expandButtonPressed() {
        FxTreeUtils.expand((SearchableTreeItem<File>) this.filesTree.getRoot());
    }

    /**
     * Action to perform when user requests an expand.
     */
    @FXML
    private void collapseButtonPressed() {
        FxTreeUtils.collapse((SearchableTreeItem<File>) this.filesTree.getRoot());
    }

    /**
     * Action to perform when user request a clear of search.
     */
    @FXML
    private void deleteSearchPressed() {
        this.filesSearchField.setText(null);
    }

    /**
     * Action to perform when user request a forced update.
     */
    @FXML
    private void updateNowButtonPressed() {
        this.fillTree();
    }

    /**
     * @return the FX view
     */
    Parent getView() {
        return OsgiFxmlLoader.getRoot(this, "Files", ResultsDictionary.class);
    }

    /**
     * @return the mapping from extensions to HTML files.
     */
    private Map<String, HtmlFileViewer> loadHtmlFiles() {
        final Map<String, HtmlFileViewer> extensions = new HashMap<>();

        final File htmlExtensionsFolder = HtmlVariablesUtils.Location.EXTENSIONS.getFolder();
        final File[] htmlFiles = htmlExtensionsFolder.listFiles((dir, name) -> name.endsWith(".html"));

        if (htmlFiles != null) {
            for (final File extensionFile : htmlFiles) {
                final HtmlFileViewer viewer = new HtmlFileViewer(extensionFile);
                for (final String ext : viewer.getExtensions()) {
                    extensions.put(ext, viewer);
                }
            }
        }

        return extensions;
    }

    private void initializeFileTree() {
        this.filesTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // We remove the context menu from the table, we are going to put it on the rows instead
        this.filesTree.setContextMenu(null);

        // We display the context menu only when there is content in the cell
        // Replace the item factory to show only the short name of the file
        this.filesTree.setCellFactory(param -> new TreeCell<>() {
            @Override
            protected void updateItem(final File item, final boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    this.setText(item.getName());
                    this.setContextMenu(FilesTabController.this.filesTreeContextMenu);
                } else {
                    this.setText(null);
                    this.setContextMenu(null);
                }
            }
        });

        // Change in selection toggles the display on the editor
        this.filesTree.getSelectionModel().selectedItemProperty().addListener((obs, oldV, treeItem) -> {
            if (treeItem == null) {
                this.open(null);
            } else {
                this.open(treeItem.getValue());
            }
        });
    }

    public void selectItem(final String pathRegex, final String fileRegex) {
        final TreeItem<File> root = this.filesTree.getRoot();
        if (root != null) {

            final TreeItem<File> treeItem = FilesTabController.getTreeViewItem(root, pathRegex, fileRegex);
            if (treeItem != null) {
                this.filesTree.getSelectionModel().select(treeItem);
                this.splitPane.setDividerPositions(0.05);
            }

        }
    }

    /**
     * Return TreeItem with first file name found matching the regex
     *
     * @param item
     * @param pathRegex
     * @return
     */
    private static TreeItem<File> getTreeViewItem(final TreeItem<File> item, final String pathRegex,
            final String fileRegex) {
        if (Pattern.matches(fileRegex, item.getValue().getName()) && //
                (pathRegex == null || Pattern.matches(pathRegex, item.getValue().getAbsolutePath()))) {
            return item;
        }

        for (final TreeItem<File> child : item.getChildren()) {
            final TreeItem<File> s = FilesTabController.getTreeViewItem(child, pathRegex, fileRegex);
            if (s != null) {
                return s;
            }

        }

        return null;
    }


    /**
     * Add a node to the editor container if not already present.
     * @param node the node to add
     */
    void addNodeSafely(Node node) {
        if (!editorContainer.getChildren().contains(node)) {
            editorContainer.getChildren().add(node);
        }
    }

    void open(final File file) {
        this.editorContainer.getChildren().clear();
        this.rightSide.getChildren().remove(this.editorSearchContainer);

        final String extension = file == null ? "" : file.getName().substring(file.getName().indexOf('.') + 1);
        if (file == null || file.isDirectory()) {
            // We don't have a file
            this.addNodeSafely(new Label(ResultsDictionary.SELECT_FILE.getString()));

        } else if (file.length() == 0L) {
            // We cannot read the file
            this.addNodeSafely(new Label(ResultsDictionary.EMPTY_FILE.getString()));

        } else if (this.extensionsToFileViewer.containsKey(extension)) {

            final Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    final HtmlFileViewer fileViewer = FilesTabController.this.extensionsToFileViewer.get(extension);
                    FilesTabController.this.webviewLoader.loadHtmlContent(fileViewer.getContent(file));
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> FilesTabController.this.editorContainer.getChildren()
                            .add(FilesTabController.this.webview));
                }
            };
            new Thread(task).start();
        } else if (Disk.isBinary(file)) {
            // We cannot read the file
            this.addNodeSafely(new Label(ResultsDictionary.BINARY_FILE.getString()));

        } else {

            final Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    FilesTabController.this.editor.openFile(file);
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        FilesTabController.this.editorContainer.getChildren()
                                .add(FilesTabController.this.editor.getView());
                        FilesTabController.this.rightSide.getChildren()
                                .add(0, FilesTabController.this.editorSearchContainer);
                    });
                }
            };
            new Thread(task).start();
        }

    }

    private void fillTree() {
        this.loadContentIntoTree(this.project.getResultsDir());
    }

    private SearchableTreeItem<File> createItem(final File file) {
        final SearchableTreeItem<File> curentItem = new SearchableTreeItem<>(file);

        // List children
        if (file.isDirectory()) {
            // Sorts the directory before the files and then create an item for each
            Arrays.stream(file.listFiles()).sorted((o1, o2) -> {
                if (o1.isDirectory() && !o2.isDirectory()) {
                    return -1;
                } else if (!o1.isDirectory() && o2.isDirectory()) {
                    return 1;
                } else {
                    return o1.compareTo(o2);
                }
            }).forEach(f -> curentItem.getItems().add(this.createItem(f)));
        }

        return curentItem;
    }

    private void loadContentIntoTree(final File directory) {
        this.filesTree.setRoot(this.createItem(directory));
    }

    /**
     * Clear the tree (and the viewer) from all its content.
     */
    private void clearTree() {
        this.filesTree.setRoot(null);
        this.editor.closeFile();
        this.filesSearchField.setText(null);
    }

}
