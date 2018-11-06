/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.migrationtool.gui;

import freemarker.template.TemplateException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import me.biesaart.utils.FileUtils;
import me.biesaart.utils.IOUtils;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.dialogs.*;
import nl.xillio.migrationtool.gui.WatchDir.FolderListener;
import nl.xillio.migrationtool.template.Templater;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.util.HotkeysHandler;
import nl.xillio.xill.util.settings.ProjectSettings;
import nl.xillio.xill.util.settings.Settings;
import nl.xillio.xill.util.settings.SettingsHandler;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;

public class ProjectPane extends AnchorPane implements FolderListener, ListChangeListener<TreeItem<Pair<File, String>>>, EventHandler<Event> {
    // Icons.
    private static final String NEW_PROJECT_ICON = "M228.734,0C102.41,0,0,102.41,0,228.735C0,355.06,102.41,457.469,228.734,457.469 c126.325,0,228.735-102.409,228.735-228.734C457.47,102.41,355.06,0,228.734,0z M359.268,265.476h-97.326v97.315 c0,16.668-13.506,30.186-30.181,30.186c-16.668,0-30.189-13.518-30.189-30.186v-97.315h-97.309 c-16.674,0-30.192-13.512-30.192-30.187c0-16.674,13.518-30.188,30.192-30.188h97.315v-97.31c0-16.674,13.515-30.183,30.189-30.183 c16.675,0,30.187,13.509,30.187,30.183v97.315h97.314c16.669,0,30.192,13.515,30.192,30.188 C389.46,251.97,375.937,265.476,359.268,265.476z";
    private static final String NEW_FOLDER_ICON = "M394.42,160.758h-0.916v-42.421c0-14.641-11.916-26.551-26.563-26.551H135.017l-17.209-45.167 c-1.797-4.69-6.289-7.793-11.31-7.793H12.105c-3.227,0-6.312,1.283-8.588,3.57C1.248,44.683-0.023,47.773,0,51.001l0.074,67.335 v227.955c0,14.641,11.916,26.551,26.551,26.551h340.321c0.261,0,0.509-0.07,0.763-0.076h26.717c9.522,0,17.241-7.714,17.241-17.242 V177.991C411.655,168.472,403.942,160.758,394.42,160.758z M369.287,160.758H68.891c-9.52,0-17.236,7.714-17.236,17.239v170.635 H26.614c-1.289,0-2.344-1.053-2.344-2.341V118.266l-0.157-55.23h74.029l17.215,45.164c1.785,4.69,6.289,7.796,11.313,7.796h240.258 c1.295,0,2.347,1.052,2.347,2.341v42.421H369.287z";
    private static final String NEW_FILE_ICON = "M327.081,0H90.231c-15.9,0-28.85,12.959-28.85,28.859v412.863c0,15.924,12.95,28.863,28.85,28.863H380.35 c15.911,0,28.855-12.939,28.855-28.863V89.234L327.081,0z M333.891,43.187l35.996,39.118h-35.996V43.187z M384.978,441.723 c0,2.542-2.087,4.629-4.628,4.629H90.231c-2.547,0-4.616-2.087-4.616-4.629V28.859c0-2.548,2.069-4.613,4.616-4.613h219.414v70.181 c0,6.682,5.443,12.099,12.129,12.099h63.198v335.196H384.978z";

    private static final SettingsHandler settings = SettingsHandler.getSettingsHandler();
    private static final String DEFAULT_PROJECT_NAME = "Samples";
    private static final String DEFAULT_PROJECT_PATH = "samples";
    private static final Logger LOGGER = Log.get();
    private static WatchDir watcher;

    private static Templater templater;

    @FXML
    private TreeView<Pair<File, String>> trvProjects;

    @FXML
    private Button btnUpload;
    @FXML
    private Button btnNew;
    @FXML
    private ToggleButton tbnShowAllFiles;

    // File filters.
    private final BotFileFilter robotFileFilter = new BotFileFilter();
    private final AnyFileFilter anyFileFilter = new AnyFileFilter();

    private final TreeItem<Pair<File, String>> root = new TreeItem<>(new Pair<>(new File("."), "Projects"));
    private final List<File> expandedFiles = new ArrayList<>();
    private FXController controller;

    // Context menu items.
    private Menu menuVersionControl;
    private MenuItem menuCut;
    private MenuItem menuCopy;
    private MenuItem menuPaste;
    private MenuItem menuRename;
    private MenuItem menuDelete;
    private MenuItem menuOpenFolder;
    private List<File> bulkFiles = new ArrayList<>(); // Files to copy or cut.
    private boolean copy = false; // True: copy, false: cut.

    private JGitRepository repo;
    // "New" button context menu items.
    private Menu menuNewBotFromTemplate;
    private MenuItem menuNewFolder;
    private MenuItem menuNewBot;

    /**
     * Initialize UI stuff
     */
    public ProjectPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectPane.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Node ui = loader.load();
            getChildren().add(ui);
            templater = new Templater();
        } catch (IOException e) {
            LOGGER.error("Error loading project pane: " + e.getMessage(), e);
        }

        trvProjects.setRoot(root);
        trvProjects.getSelectionModel().getSelectedItems().addListener(this);
        trvProjects.setShowRoot(false);
        trvProjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        root.setExpanded(true);

        try {
            watcher = new WatchDir();
            watcher.createThread().start();
        } catch (IOException e) {
            LOGGER.error("IOException when creating the WatchDir.", e);
        }

        trvProjects.setCellFactory(treeView -> new CustomTreeCell());

        // Add event listeners.
        this.addEventFilter(KeyEvent.KEY_PRESSED, this);
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, this);

        initializeSettings();
        loadProjects();
        addContextMenu();
        addNewButtonContextMenu();
    }

    private void initializeSettings() {
        // Register the hasRun setting which is being used to add the default project on first run.
        settings.simple().register(Settings.INFO, Settings.HAS_RUN, "false", "Whether the IDE has run before.");

        // Register and load the setting for showing all files.
        settings.simple().register(Settings.FILE, Settings.SHOW_ALL_FILES, "false", "Show non-Xill files.");
        tbnShowAllFiles.setSelected(settings.simple().getBoolean(Settings.FILE, Settings.SHOW_ALL_FILES));
    }

    private void addContextMenu() {
        // Cut.
        menuCut = new MenuItem("Cut");
        menuCut.setOnAction(e -> cut());

        // Copy.
        menuCopy = new MenuItem("Copy");
        menuCopy.setOnAction(e -> copy());

        // Paste.
        menuPaste = new MenuItem("Paste");
        menuPaste.setOnAction(e -> paste());

        // Rename.
        menuRename = new MenuItem("Rename...");
        menuRename.setOnAction(e -> renameButtonPressed());

        // New Folder
        MenuItem menuCreateFolder = new MenuItem("New Folder...");
        menuCreateFolder.setOnAction(e -> newFolderButtonPressed());

        // New File
        MenuItem menuNewFile = new MenuItem("New File...");
        menuNewFile.setOnAction(e -> newBot(null));

        // Delete.
        menuDelete = new MenuItem("Delete...");
        menuDelete.setOnAction(e -> deleteButtonPressed());

        // Version Control.
        versionControlMenu();

        // Upload.
        MenuItem menuUpload = new MenuItem("Upload...");
        menuUpload.setOnAction(e -> uploadButtonPressed());

        menuOpenFolder = new MenuItem("Open containing folder");
        menuOpenFolder.setOnAction(e -> {
            Thread openContainingFolderTread = new Thread(() -> {
                try {
                    Desktop.getDesktop().open(getCurrentItem().getValue().getKey().getParentFile());
                } catch (IOException ex) {
                    LOGGER.error("Failed to open containing folder.", ex);
                }
            });
            openContainingFolderTread.start();
        });


        // Create the context menu.
        ContextMenu menu = new ContextMenu(menuCut, menuCopy, menuPaste, menuRename, menuCreateFolder, menuNewFile, menuDelete, menuUpload, menuVersionControl);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            menu.getItems().add(menuOpenFolder);
        }

        trvProjects.setContextMenu(menu);
        trvProjects.setOnContextMenuRequested(e -> {
            // Only paste when there is just 1 item selected (the paste location) and there are files to paste.
            menuPaste.setDisable(getAllCurrentItems().size() != 1 || bulkFiles.isEmpty());

            // Check the project repository status.
            repo = new JGitRepository(getCurrentProject().getValue().getKey());
            menuVersionControl.setDisable((!repo.isInitialized()) || (!repo.hasRemote()));
        });
    }

    private void addNewButtonContextMenu() {
        MenuItem menuNewProject = new MenuItem("Add project...", ProjectPane.createIcon(ProjectPane.NEW_PROJECT_ICON));
        menuNewProject.setOnAction(e -> newProjectButtonPressed());

        menuNewFolder = new MenuItem("New folder...", ProjectPane.createIcon(ProjectPane.NEW_FOLDER_ICON));
        menuNewFolder.setOnAction(e -> newFolderButtonPressed());

        menuNewBot = new MenuItem("New file...", ProjectPane.createIcon(ProjectPane.NEW_FILE_ICON));
        menuNewBot.setOnAction(e -> newBot(null));

        menuNewBotFromTemplate = new Menu("New robot from template...");

        ContextMenu menu = new ContextMenu(menuNewProject, menuNewFolder, menuNewBot, menuNewBotFromTemplate);
        btnNew.setOnAction(e -> {
            Bounds bounds = btnNew.localToScreen(btnNew.getBoundsInParent());
            generateTemplateMenu();
            menu.show(btnNew, bounds.getMinX(), bounds.getMaxY());
        });
    }

    private void generateTemplateMenu() {
        menuNewBotFromTemplate.getItems().clear();
        try {
            templater.getTemplateNames().stream().map(MenuItem::new).forEach(menuNewBotFromTemplate.getItems()::add);
            menuNewBotFromTemplate.getItems().forEach(item -> item.setOnAction(event -> newBot(item.getText())));
        } catch (IOException e) {
            MenuItem errorItem = new MenuItem("Error while reading template folder");
            errorItem.getStyleClass().add("menu-item-error");
            menuNewBotFromTemplate.getItems().add(errorItem);
            LOGGER.error("Error while reading template folder", e);
        }

        // add default empty bot template
        menuNewBotFromTemplate.getItems().add(0, new SeparatorMenuItem());
        MenuItem emptyBot = new MenuItem("empty robot");
        emptyBot.setOnAction(event -> newBot(null));
        menuNewBotFromTemplate.getItems().add(0, emptyBot);
    }

    private void versionControlMenu() {
        MenuItem push = new MenuItem("Push...");
        push.setOnAction(e -> new GitPushDialog(repo).showAndWait());

        MenuItem pull = new MenuItem("Pull...");
        pull.setOnAction(e -> new GitPullDialog(repo).showAndWait());

        MenuItem branches = new MenuItem("Branches...");
        branches.setOnAction(e -> new GitBranchDialog(repo).showAndWait());

        menuVersionControl = new Menu("Version control", null, push, pull, new SeparatorMenuItem(), branches);
    }

    private static Group createIcon(final String shape) {
        SVGPath path = new SVGPath();
        path.setFill(javafx.scene.paint.Color.DARKGRAY);
        path.setScaleX(0.04);
        path.setScaleY(0.04);
        path.setContent(shape);
        return new Group(path);
    }

    /* Bulk file functionality. */

    private void cut() {
        copy = false;
        bulkFiles = getAllCurrentFiles();
    }

    private void copy() {
        copy = true;
        bulkFiles = getAllCurrentFiles();
    }

    private void paste() {
        paste(getCurrentItem().getValue().getKey(), bulkFiles, copy);
        // If the files were moved (not copied), clear the bulk files.
        if (!copy) {
            bulkFiles.clear();
        }
    }

    private void paste(File pasteLoc, List<File> files, boolean copy) {
        // Get the directory to paste in.
        final File destDir = pasteLoc.isDirectory() ? pasteLoc : pasteLoc.getParentFile();
        boolean overwriteAll = false;
        for (File oldFile : files) {
            // Check if the source file exists. If not is is probably already copied by moving a parent folder.
            if (!oldFile.exists()) {
                continue;
            }

            // Check if the file already exists. (This does not throw an IOException in FileUtils, so we need to check it here.)
            File destFile = new File(destDir, oldFile.getName());
            if (destFile.exists() && !overwriteAll) {
                // Show a dialog.
                ButtonType buttonTypeOverwrite = new ButtonType("Overwrite");
                ButtonType buttonTypeOverwriteAll = new ButtonType("Overwrite all");
                ButtonType buttonTypeSkip = new ButtonType("Skip");
                AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING,
                        "File already exists", "",
                        "The destination file (" + destFile.toString() + ") already exists.",
                        buttonTypeOverwrite, buttonTypeOverwriteAll, buttonTypeSkip, ButtonType.CANCEL);

                dialog.showAndWait();
                final Optional<ButtonType> result = dialog.getResult();

                if (result.isPresent()) {
                    if (result.get() == buttonTypeOverwriteAll) {
                        overwriteAll = true; // Overwrite all
                    } else if (result.get() == buttonTypeSkip) {
                        continue; // Skip
                    } else if (result.get() == ButtonType.CANCEL) {
                        break; // Cancel
                    } // else Overwrite
                } else {
                    break; // Dialog was closed - treat it as Cancel
                }
            }

            // Create the list of source (key) and TARGET (value) files that will be copied/moved
            final LinkedList<Pair<File, File>> fileList = new LinkedList<>();
            if (oldFile.isDirectory()) {
                FileUtils.listFiles(oldFile, null, true).forEach(f -> fileList.add(new Pair(f, new File(destDir, oldFile.getParentFile().toURI().relativize(f.toURI()).getPath()))));
            } else {
                fileList.add(new Pair(oldFile, new File(destDir, oldFile.getParentFile().toURI().relativize(oldFile.toURI()).getPath())));
            }

            // Test if any open source tab is modified
            if (!checkOpenTabsModified(fileList.stream().map(t -> t.getKey()).collect(Collectors.toList()))) {
                break;
            }

            // Copy or move the file or directory.
            if (!pasteCopyMove(oldFile, destDir, fileList, copy)) {
                break;
            }
        }
    }

    private boolean pasteCopyMove(final File source, final File target, final List<Pair<File, File>> fileList, final boolean copy) {
        try {
            if (copy) {
                if (source.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(source, target);
                } else {
                    FileUtils.copyFileToDirectory(source, target);
                }
                reloadTabs(fileList.stream().map(t -> t.getValue()).collect(Collectors.toList()));
            } else {
                // In case of overwriting the target must be deleted beforehand because FileUtils.move.. methods throws exception otherwise (while FileUtils.copy.. methods don't)
                File destTarget = new File(target, source.getName());
                if (source.isDirectory()) {
                    FileUtils.deleteDirectory(destTarget);
                    FileUtils.moveDirectoryToDirectory(source, target, false);
                } else {
                    destTarget.delete();
                    FileUtils.moveFileToDirectory(source, target, false);
                }
                resetTabs(fileList);
            }
        } catch (IOException e) {
            // Show the error.
            LOGGER.error("IOException while moving files.", e);
            AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Error while pasting files.", "",
                    "An error occurred while pasting files. Press OK to continue or Cancel to abort.\n" + e.getMessage(),
                    ButtonType.OK, ButtonType.CANCEL);

            error.showAndWait();
            final Optional<ButtonType> result = error.getResult();

            // If cancel was pressed, abort.
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return false;
            }
        }
        return true;
    }

    private boolean checkOpenTabsModified(final List<File> files) {
        for (File f : files) {
            FileTab tab = controller.findTab(toURL(f));
            if (tab != null) {
                if (tab.getEditorPane().getDocumentState().getValue() == EditorPane.DocumentState.CHANGED) {
                    AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING, "Modified document",
                            "The document " + tab.getName() + " is modified.",
                            "Do you want to save the changes?",
                            YES, NO, ButtonType.CANCEL);
                    dialog.showAndWait();
                    final Optional<ButtonType> result = dialog.getResult();
                    if (result.isPresent()) {
                        if (result.get() == ButtonType.CANCEL) {
                            return false;
                        }
                        if (result.get() == YES) {
                            tab.save();
                        }
                    }
                }
            }
        }
        return true;
    }

    private URL toURL(File string) {
        try {

            return string.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void resetTabs(final List<Pair<File, File>> files) {
        // Close all related open target tabs (if exist they will be overwritten by source files having different path and project so we need to close them)
        files.forEach(f -> {
            FileTab tab = controller.findTab(toURL(f.getKey())); // Target file
            if (tab != null) {
                FileTab openTab = (FileTab) controller.getSelectedTab();
                controller.closeTab(tab, true, true);
                controller.openRobot(toURL(f.getValue()));
                if(openTab != tab)
                    controller.showTab(openTab);
            }
        });

        // Reset project and document of all related open source tabs
        files.forEach(f -> {
            FileTab tab = controller.findTab(toURL(f.getKey())); // Source file
            if (tab != null) {
                final File file = f.getValue();
                try {
                    tab.resetSource(file.toURI().toURL()); // Target file
                } catch (MalformedURLException e) {
                    LOGGER.error("Could not reset sources for " + file, e);
                }
            }
        });
    }

    // Reload content of all existing tabs that matches the (target) files in the list
    private void reloadTabs(final List<File> files) {
        files.forEach(f -> {
            FileTab tab = controller.findTab(toURL(f));
            if (tab != null) {
                tab.reload();
            }
        });
    }

    /* End of bulk file functionality. */

    @FXML
    private void newProjectButtonPressed() {
        LoadOrCreateProjectDialog dlg = new LoadOrCreateProjectDialog(this);
        dlg.showAndWait();
    }

    @FXML
    private void newFolderButtonPressed() {
        new NewFolderDialog(this, getCurrentItem()).show();
    }

    @FXML
    private void uploadButtonPressed() {
        new UploadToServerDialog(this, getAllCurrentItems()).show();
    }

    private void renameButtonPressed() {
        TreeItem<Pair<File, String>> item = getCurrentItem();
        FileTab tab = controller.findTab(toURL(item.getValue().getKey()));

        // Check if a robot is still running, show a dialog to stop them.
        if (checkRobotsRunning(Collections.singletonList(item), false, false)) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING,
                    "Rename running robot",
                    "You are trying to rename a running robot or a folder containing running robots.",
                    "Do you want to stop the robot so you can rename it?",
                    YES, NO
            );

            dialog.showAndWait();
            final Optional<ButtonType> result = dialog.getResult();
            if (!result.isPresent() || result.get() != YES) {
                return;
            }
        }

        // Get the old name, show the rename dialog, get the new name.
        String oldName = item.getValue().getValue();
        new RenameDialog(item).showAndWait();
        String newName = item.getValue().getValue();

        // Check if the name changed.
        if (!oldName.equals(newName)) {
            // Stop any running robots, close tabs.
            checkRobotsRunning(Collections.singletonList(item), true, true);

            // If the tab was previously open, close and reopen it.
            if (tab != null) {
                boolean wasSelected = controller.getSelectedTab() == tab;
                controller.closeTab(tab);
                FileTab newTab = controller.openFile(toURL(item.getValue().getKey()));
                if (wasSelected) {
                    controller.showTab(newTab);
                }
            }
        }
    }

    private void deleteButtonPressed() {
        // Get selected items and reverse them so the right element will be selected
        ObservableList<TreeItem<Pair<File, String>>> selectedItems = FXCollections.observableArrayList(getAllCurrentItems());
        FXCollections.reverse(selectedItems);
        // Check if there are any robots running
        boolean running = checkRobotsRunning(selectedItems, false, false);

        // Check if projects or files/folders are being deleted
        if (selectedItems.stream().allMatch(file -> getProject(file) == file)) {
            int projectCount = selectedItems.size();

            // Set up dialog content
            CheckBox checkBox = new CheckBox("Delete project contents from disk");
            Label prjText = new Label(selectedItems.stream().map(file -> file.getValue().getKey().getPath())
                    .collect(Collectors.joining("\n")));
            prjText.setWrapText(true);

            GridPane gridPane = new GridPane();
            GridPane.setMargin(checkBox, new javafx.geometry.Insets(20, 0, 0, 0));
            gridPane.add(prjText, 0, 0);
            gridPane.add(checkBox, 0, 1);

            // Set up dialog
            ContentAlertDialog alert = new ContentAlertDialog(Alert.AlertType.CONFIRMATION,
                    "Delete",
                    "Are you sure you want to delete " + projectCount + " " + ((projectCount > 1) ? "projects" : "project") + "?" +
                            (running ? "\nOne or more robots are still running, deleting will terminate them." : ""),
                    "",
                    gridPane
            );

            alert.showAndWait();
            alert.getResult()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> deleteItems(selectedItems, checkBox.isSelected()));

        } else if (selectedItems.stream().allMatch(file -> getProject(file) != file)) {
            long folderCount = selectedItems.stream().filter(file -> file.getValue().getKey().isDirectory()).count();
            long fileCount = selectedItems.size() - folderCount;

            // Set up dialog
            AlertDialog alert = new AlertDialog(Alert.AlertType.CONFIRMATION, "Delete",
                    "Are you sure you want to delete " +
                            (folderCount > 0 ? folderCount + " folder" + (folderCount > 1 ? "s" : "") : "") +
                            (fileCount > 0 ? (folderCount > 0 && fileCount > 0 ? " and " : "") + fileCount + " file" + (fileCount > 1 ? "s" : "") : "")
                            + "?" +
                            (running ? "\nOne or more robots are still running, deleting will terminate them." : ""),
                    folderCount > 0 ? "Selected files, including all files in selected folders, will be deleted." : "");

            alert.showAndWait();
            if (alert.getResult().get() == ButtonType.OK) {
                deleteItems(selectedItems, true);
            }
        }
    }

    public void newBot(String templateFile) {
        // New file can only be created under a project.
        if (getCurrentProject() == null || getCurrentItem() == null) {
            return;
        }
        File projectFile = getCurrentProject().getValue().getKey();

        // Select initial directory.
        File initialFolder = getCurrentItem().getValue().getKey();
        if (initialFolder.isFile()) {
            initialFolder = initialFolder.getParentFile();
        }

        // Open a file chooser to save the robot file.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("New Robot");
        fileChooser.setInitialDirectory(initialFolder);
        String robotExtension = "*" + XillEnvironment.ROBOT_EXTENSION;
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Xill Robot (" + robotExtension + ")", robotExtension),
                new FileChooser.ExtensionFilter("All files (*.*)", "*.*")
        );

        File chosen = fileChooser.showSaveDialog(this.getScene().getWindow());

        // Check if no file was chosen.
        if (chosen == null) {
            return;
        }

        // Check if the new file is in the project.
        if (chosen.getParent().startsWith(projectFile.getAbsolutePath())) {
            // If the file has no extension, add the robot extension.
            if (FilenameUtils.getExtension(chosen.getName()).isEmpty()) {
                chosen = new File(chosen.getPath() + XillEnvironment.ROBOT_EXTENSION);
            }

            // Check whether the file is a robot.
            boolean isRobot = chosen.getName().endsWith(XillEnvironment.ROBOT_EXTENSION);

            try {
                Map<String, Object> model = Templater.getDefaultModel();
                model.put("fileName", chosen.getName());
                model.put("filePath", chosen.getCanonicalPath());
                model.put("projectName", projectFile.getName());
                model.put("projectPath", projectFile.getCanonicalPath());
                templater.render(templateFile, model, Paths.get(chosen.toURI()));
                controller.viewOrOpenRobot(toURL(chosen), Paths.get(projectFile.toURI()), isRobot);
            } catch (IOException e) {
                LOGGER.error("Failed to create file.", e);
                AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Error creating robot.", "", "Could not create '" + chosen.toString() + "'.");
                error.showAndWait();
            } catch (TemplateException e) {
                new AlertDialog(Alert.AlertType.ERROR, "Invalid template", "The template you want to use could not be processed!", e.getMessage()).show();
                controller.viewOrOpenRobot(toURL(chosen), Paths.get(projectFile.toURI()), isRobot);
            }
        } else {
            // Inform the user about the file being created outside of a project.
            new AlertDialog(Alert.AlertType.ERROR, "Project path error", "", "Files can only be created inside projects.").show();
        }
    }

    @Override
    public void handle(Event event) {
        // Request focus on mouse press.
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            this.requestFocus();
        }

        // Key presses.
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            KeyEvent keyEvent = (KeyEvent) event;

            // Hotkeys.
            handleHotkey(FXController.hotkeys.getHotkey(keyEvent));

            // Keypresses.
            if (keyEvent.getCode() == KeyCode.DELETE && !menuDelete.isDisable()) {
                deleteButtonPressed();
            }
        }
    }

    @SuppressWarnings("squid:SwitchLastCaseIsDefaultCheck")
    private void handleHotkey(HotkeysHandler.Hotkeys hk) {
        if (hk != null) {
            switch (hk) {
                case CUT:
                    if (!menuCut.isDisable()) {
                        cut();
                    }
                    break;
                case COPY:
                    if (!menuCopy.isDisable()) {
                        copy();
                    }
                    break;
                case PASTE:
                    if (!menuPaste.isDisable()) {
                        paste();
                    }
                    break;
                case RENAME:
                    if (!menuRename.isDisable()) {
                        renameButtonPressed();
                    }
                    break;
            }
        }
    }

    /**
     * Check if there are any robots running.
     *
     * @param items    The items to check.
     * @param stop     Whether to stop the running robots.
     * @param closeTab Whether to close open robot tabs.
     * @return True if any items or sub-items are running robots.
     */
    private boolean checkRobotsRunning(List<TreeItem<Pair<File, String>>> items, boolean stop, boolean closeTab) {
        boolean running = false;

        for (TreeItem<Pair<File, String>> item : items) {
            // Check if the robot tab is open and the robot is running.
            FileTab tab = controller.findTab(toURL(item.getValue().getKey()));

            // Check if the tab is a robot tab.
            if (tab != null && tab instanceof RobotTab) {
                RobotTab robotTab = (RobotTab) tab;
                running |= robotTab.getEditorPane().getControls().robotRunning();
                // Stop the robot.
                if (stop) {
                    robotTab.getEditorPane().getControls().stop();
                }
            }

            // Close the tab.
            if (closeTab) {
                controller.closeTab(tab);
            }

            // Recursively check all children of the item.
            running |= checkRobotsRunning(item.getChildren(), stop, closeTab);
        }

        return running;
    }

    /**
     * Delete multiple items from the tree view.
     *
     * @param toDelete           The items to delete.
     * @param hardDeleteProjects Whether to delete the projects from disk.
     */
    private void deleteItems(List<TreeItem<Pair<File, String>>> toDelete, boolean hardDeleteProjects) {
        // Copy the list (because javafx will select other items when some are deleted, screwing things over.
        List<TreeItem<Pair<File, String>>> items = new ArrayList<>(toDelete);

        // First stop all robots and close the tabs.
        checkRobotsRunning(items, true, true);

        items.forEach(item -> {
            File file = item.getValue().getKey();
            // Check if we have write access to file and we're not soft deleting a project
            if (!file.canWrite() && !(item == getProject(item) && !hardDeleteProjects)) {
                LOGGER.error("Cannot delete " + file.toString() + ": no write access.");
                AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Error while deleting files.", "",
                        "Could not delete: " + file.toString() + ", access was denied.\n\nPlease check if you have write permissions.",
                        ButtonType.OK);
                error.showAndWait();
            }

            // Check if the file exists and is writable.
            if (file.canWrite()) {
                try {
                    //If it is a file, remove it. If it is a folder only remove it if it is not a project or we should hard delete projects.
                    if (file.isFile() || file.isDirectory() && (item != getProject(item) || hardDeleteProjects)) {
                        FileUtils.forceDelete(file);
                    }
                } catch (IOException e) {
                    LOGGER.error("Could not delete " + file.toString(), e);
                    AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Error while deleting files.", "",
                            "An error occurred while deleting files.\n" + e.getMessage(), ButtonType.OK);
                    error.showAndWait();
                }
            }

            // If the item is a project, remove it.
            if (item == getProject(item)) {
                removeProject(item);
            }
        });
    }

    /* Projects */

    private void loadProjects() {
        Platform.runLater(() -> {
            List<ProjectSettings> projects = settings.project().getAll();

            // When this is the first time the IDE runs, add the samples project
            File defaultProjectPath = new File(DEFAULT_PROJECT_PATH);
            if (!settings.simple().getBoolean(Settings.INFO, Settings.HAS_RUN)) {
                if (defaultProjectPath.exists()) {
                    // Projects must have an absolute directory
                    newProject(DEFAULT_PROJECT_NAME, pathToURL(defaultProjectPath.toPath()), "");
                }
                // Mark that the IDE has run for the first time
                settings.simple().save(Settings.INFO, Settings.HAS_RUN, true);
                settings.commit();
            }

            if (projects.isEmpty()) {
                disableAllButtons(true);
                return;
            }

            projects.forEach(this::addProject);

            root.getChildren().forEach(node -> node.setExpanded(false));
            root.setExpanded(true);
        });
    }

    /**
     * Removes an item from the project list and keeps the files.
     *
     * @param item the item to remove
     */
    public void removeProject(final TreeItem<Pair<File, String>> item) {
        root.getChildren().remove(item);
        settings.project().delete(item.getValue().getValue());
        select(root);
    }

    /**
     * Creates a new project from source.
     *
     * @param name   the name of the new project
     * @param folder the folder representing the project
     * @return whether creating the project was successful
     */
    public boolean loadOrCreateProject(final String name, final String folder) {
        List<ProjectSettings> existingProjects = settings.project().getAll();

        boolean projectExist = existingProjects.stream().map(ProjectSettings::getFolder).anyMatch(n -> n.equalsIgnoreCase(folder));
        boolean nameTaken = existingProjects.stream().map(ProjectSettings::getName).anyMatch(n -> n.equalsIgnoreCase(name));

        if (projectExist) {
            return showDefaultErrorDialog("The selected folder is already a project. \nTo create a new project an empty folder must be selected.");
        }

        if (nameTaken) {
            return showDefaultErrorDialog("Make sure the name is not already in use.");
        }

        if ("".equals(folder)) {
            return showDefaultErrorDialog("Make sure the \"folder\" field is not empty.");
        }

        if ("".equals(name)) {
            return showDefaultErrorDialog("Make sure the name is not empty.");
        }

        // Check whether new project isn't parent or children of an existing project
        Optional<String> other = existingProjects.stream()
                .filter(setting -> folder.startsWith(setting.getFolder()) || setting.getFolder().startsWith(folder))
                .findAny()
                .map(ProjectSettings::getName);

        if (other.isPresent()) {
            return showDefaultErrorDialog("A project cannot be a parent or children of an already existing project." +
                    " The project you are trying to add is related to '" + other.get() + "'.");
        }

        ProjectSettings project = new ProjectSettings(name, folder, "");
        settings.project().save(project);
        File projectFolder = new File(folder);
        if (!projectFolder.exists()) {
            try {
                FileUtils.forceMkdir(new File(project.getFolder()));
            } catch (IOException e) {
                LOGGER.error("Failed to create project directory", e);
            }
        }
        addProject(project);
        return true;
    }

    /**
     * Creates a new project.
     *
     * @param name        the name of the new project
     * @param folder      the folder representing the project
     * @param description the description of the project
     * @return whether creating the project was successful
     */
    public boolean newProject(final String name, final URL folder, final String description) {
        // Check if the project is already opened
        boolean projectDoesntExist = root.getChildren().parallelStream().map(TreeItem::getValue).map(Pair::getValue).noneMatch(n -> n.equalsIgnoreCase(name))
                && findItemByPath(root, folder) == null;

        if (!projectDoesntExist || "".equals(name) || "".equals(folder)) {
            AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Error", "",
                    "Make sure the name and folder are not empty, and do not exist as a project yet.", ButtonType.OK);
            error.show();
            return false;
        }

        // Check if project folder already exists under different capitalization
        File projectFolder = new File(folder.toString());
        if (projectFolder.exists()) {
            try {
                String canonicalFileName = projectFolder.getCanonicalFile().getName();
                String fileName = projectFolder.getName();

                if (!canonicalFileName.equals(fileName)) {
                    AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Project already exists", "",
                            "The selected directory already exists with a different case. Please use \"" + canonicalFileName + "\" or rename your project.", ButtonType.OK);
                    error.show();
                    return false;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read directory", e);
            }
        }

        // Create the project.
        ProjectSettings project = new ProjectSettings(name, folder.toString(), description);
        settings.project().save(project);
        try {
            FileUtils.forceMkdir(new File(project.getFolder()));
        } catch (IOException e) {
            LOGGER.error("Failed to create project directory", e);
        }
        addProject(project);

        return true;
    }

    @SuppressWarnings("squid:S1166")
    // The error message thrown is minimalistic (as wanted) but has all the necessary information.
    private void addProject(final ProjectSettings project) {
        // Check if the project still exists
        if (project.getFolder() == null) {
            return;
        }

        TreeItem<Pair<File, String>> projectNode = new ProjectTreeItem(new File(project.getFolder()), project.getName());
        root.getChildren().add(projectNode);

        if (watcher != null) {
            try {
                watcher.addFolderListener(this, Paths.get(project.getFolder()));
            } catch (IOException e) {
                LOGGER.error("Could not register project: " + project.getFolder() + ". Did you remove or rename the project folder?");
            }
        }
        projectNode.setExpanded(false);
        select(projectNode);
    }

    /* End of projects */

    private boolean showAlertDialog(Alert.AlertType type, String title, String header, String content) {
        AlertDialog error = new AlertDialog(type, title, header, content, ButtonType.OK);
        error.show();
        return false;
    }

    private boolean showDefaultErrorDialog(String content) {
        return showAlertDialog(Alert.AlertType.ERROR, "Error", "", content);
    }

    /* Selection of TreeItems */

    /**
     * Selects an item in the treeview corresponding to given a path.
     *
     * @param path The path of the item to select.
     */
    public void select(final URL path) {
        select(path != null ? findItemByPath(root, path) : null);
    }

    private TreeItem<Pair<File, String>> findItemByPath(final TreeItem<Pair<File, String>> parent, final URL path) {
        for (TreeItem<Pair<File, String>> item : parent.getChildren()) {
            if (path.equals(pathToURL(item.getValue().getKey().toPath()))) {
                return item;
            } else {
                TreeItem<Pair<File, String>> child = findItemByPath(item, path);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    private URL pathToURL(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException("Cannot convert path '"+ path.toString() +"' to URL", e);
        }
    }

    /**
     * Select an item from the treeview.
     *
     * @param item The item to select.
     */
    public void select(final TreeItem<Pair<File, String>> item) {
        if (item != null) {
            trvProjects.getSelectionModel().clearSelection();
            trvProjects.getSelectionModel().select(item);
        }
    }

    /* End of selection. */

    /**
     * Create a directory if it does not exist.
     *
     * @param item The tree item for which to create the directory.
     */
    public void makeDirIfNotExists(TreeItem<Pair<File, String>> item) {
        File dir = item.getValue().getKey();

        if (!dir.exists()) {
            try {
                // Create the directory.
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                // Show an error.
                AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Could not create folder", "",
                        e.getMessage(), ButtonType.OK);
                error.show();
            }

            // Re-add the folder listener.
            try {

                watcher.addFolderListener(this, Paths.get(getProject(item).getValue().getKey().getPath()));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Called when the outside change to the robot file has been done
     * Check if the outside change to the robot file should lead to asking user about loading new content and if so then do it
     *
     * @param resource The url to the robot file
     */
    public void fileChanged(URL resource) {

        FileTab tab = controller.findTab(resource);
        if (tab == null) {
            return;
        }

        // Test of content change
        String newContent = "";
        try (InputStream stream = resource.openStream()) {
            newContent = IOUtils.toString(stream);
        } catch (IOException e) {
            LOGGER.warn("Failed to read changed robot content", e);
        }

        if (!tab.getEditorPane().checkChangedCode(newContent) || tab.getEditorPane().documentState.getValue().equals(EditorPane.DocumentState.NEW)) {
            return; // The code was not changed or it is a new document so there could not be any change
        }

        tab.requestFocus();

        // Create and show an alert dialog saying the content has been changed.
        AlertDialog alert = new AlertDialog(Alert.AlertType.WARNING, "File content change",
                "The file \"" + tab.getResourceUrl().getPath() + "\" has been modified outside the editor.", "Do you want reload the file?",
                YES, NO);

        alert.showAndWait();
        alert.getResult()
                .filter(result -> result == YES)
                .ifPresent(result -> tab.reload());
    }

    @Override
    public void folderChanged(final Path dir, final Path child, final WatchEvent<Path> event) {
        for (TreeItem<Pair<File, String>> item : root.getChildren()) {
            if (item instanceof ProjectTreeItem) {
                ProjectTreeItem project = (ProjectTreeItem) item;
                if (dir.startsWith(project.getValue().getKey().getAbsolutePath()) && event.kind() != ENTRY_MODIFY) {
                    // The files in project directory has changed (i.e. some file(s) has been removed / renamed / added).
                    Platform.runLater(() -> selectNewItem(dir, child, project));
                }
            }
        }
    }

    /**
     * refresh the project and select the right item. This is the newly created robot/folder or the parent of the deleted robot/folder
     *
     * @param parent  the parent of the item
     * @param child   the item
     * @param project the project to be refreshed
     */
    public void selectNewItem(Path parent, Path child, ProjectTreeItem project) {
        project.refresh();
        TreeItem<Pair<File, String>> item = findItemByPath(getRoot(), pathToURL(child));
        if (item == null) {
            item = findItemByPath(getRoot(), pathToURL(parent));
        }
        select(item);
    }

    /**
     * Returns the root of the tree.
     *
     * @return the root of the tree
     */

    public TreeItem<Pair<File, String>> getRoot() {
        return root;
    }

    /**
     * @return the currently selected node
     */
    public TreeItem<Pair<File, String>> getCurrentItem() {
        return trvProjects.getSelectionModel().getSelectedItem();
    }

    /**
     * @return All currently selected nodes.
     */
    public ObservableList<TreeItem<Pair<File, String>>> getAllCurrentItems() {
        return trvProjects.getSelectionModel().getSelectedItems();
    }

    public List<File> getAllCurrentFiles() {
        return getAllCurrentItems().stream().map(t -> t.getValue().getKey()).collect(Collectors.toList());
    }

    /**
     * Gets the first project node above the childItem.
     *
     * @param childItem The childItem to find the project for.
     * @return the project node
     */
    public TreeItem<Pair<File, String>> getProject(final TreeItem<Pair<File, String>> childItem) {
        TreeItem<Pair<File, String>> item = childItem;
        if (item == root) {
            return null;
        }

        while (item != null && item.getParent() != root) {
            item = item.getParent();
        }
        return item;
    }

    /**
     * Gets the first project node above the selected item.
     *
     * @return project node
     */
    public TreeItem<Pair<File, String>> getCurrentProject() {
        return getProject(getCurrentItem());
    }

    /**
     * Gets the project path of a node.
     *
     * @param url The file to get the project path for.
     * @return The project path if it exists
     */
    public Optional<String> getProjectPath(URL url) {
        Optional<String> projectPath = Optional.empty();

        // Find the tree item.
        TreeItem<Pair<File, String>> item = findItemByPath(root, url);

        // Check if the item is a part of the tree, only then should we try to get the project.
        if (item != null) {
            select(item);
            TreeItem<Pair<File, String>> project = getCurrentProject();

            if (project != null) {
                projectPath = Optional.of(project.getValue().getKey().getPath());
            }
        }

        return projectPath;
    }

    /**
     * Stops the folder watcher.
     */
    public static void stop() {
        if (watcher != null) {
            watcher.stop();
        }
    }

    protected void setGlobalController(final FXController controller) {
        this.controller = controller;
    }

    /**
     * store all files that are expanded so they can be expanded when the projecttree is rebuild.
     *
     * @param treeItem the root treeItem to check from
     */
    public void storeAllExpanded(final TreeItem<Pair<File, String>> treeItem) {
        if (treeItem.getValue().getKey().isDirectory()) {
            if (treeItem.isExpanded()) {
                expandedFiles.add(treeItem.getValue().getKey());
            }
            treeItem.getChildren().forEach(this::storeAllExpanded);
        }
    }

    /**
     * This method is called when the selection in the tree view is changed
     */
    @Override
    public void onChanged(Change<? extends TreeItem<Pair<File, String>>> newObject) {
        // Update all buttons to their default state.
        disableAllButtons(false);
        disableFileButtons(false);

        int selectedSize = getAllCurrentItems().size();
        if (selectedSize == 0) {
            // No item is selected.
            disableAllButtons(true);
            disableFileButtons(true);
        }

        // Check if more than 1 item is selected.
        if (selectedSize > 1) {
            menuRename.setDisable(true);
            menuOpenFolder.setDisable(true);
            disableFileButtons(true);
        }

        // If a project is selected disable the cut menu item.
        if (projectSelected()) {
            menuCut.setDisable(true);
            menuRename.setDisable(true);
        }

        // Disable deleting projects and folders/files at the same time
        if (getAllCurrentItems().stream().map(file -> getProject(file) == file).distinct().count() == 2) {
            menuDelete.setDisable(true);
        }
    }

    /**
     * Whether the current selection contains one or more projects.
     */
    private boolean projectSelected() {
        for (TreeItem<Pair<File, String>> item : getAllCurrentItems()) {
            if (item != null && item == getProject(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enable or disable all buttons.
     *
     * @param disable Whether to disable or enable the buttons.
     */
    private void disableAllButtons(boolean disable) {
        btnUpload.setDisable(disable);
        menuDelete.setDisable(disable);
        menuRename.setDisable(disable);
        menuOpenFolder.setDisable(disable);
        menuCut.setDisable(disable);
        menuNewFolder.setDisable(disable);
        menuNewBot.setDisable(disable);
    }

    /**
     * Update the New File and Open File buttons.
     *
     * @param disable Whether to disable or enable the buttons.
     */
    private void disableFileButtons(boolean disable) {
        controller.disableNewFileButton(disable);
        controller.disableOpenFileButton(disable);
        menuNewFolder.setDisable(disable);
        menuNewBotFromTemplate.setDisable(disable);
    }

    /**
     * Get the number of projects
     *
     * @return the number of projects present
     */
    public int getProjectsCount() {
        return settings.project().getAll().size();
    }

    @FXML
    private void toggleShowAllFiles() {
        // Refresh all projects.
        for (TreeItem<Pair<File, String>> item : root.getChildren()) {
            if (item instanceof ProjectTreeItem) {
                ProjectTreeItem project = (ProjectTreeItem) item;
                project.refresh();
            }
        }

        // Set the button label.
        String tooltipText = "Show " + (tbnShowAllFiles.isSelected() ? "robots only" : "all files");
        tbnShowAllFiles.getTooltip().setText(tooltipText);

        // Save the setting.
        settings.simple().save(Settings.FILE, Settings.SHOW_ALL_FILES, tbnShowAllFiles.isSelected());
    }

    /*========== Tree items and cells ==========*/

    /**
     * A project tree item.
     */
    private class ProjectTreeItem extends TreeItem<Pair<File, String>> {

        private boolean isLeaf;
        private boolean isFirstTimeChildren = true;
        private boolean isFirstTimeLeaf = true;

        public ProjectTreeItem(final File file, final String name) {
            super(new Pair<>(file, name));
        }

        @Override
        public ObservableList<TreeItem<Pair<File, String>>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                isLeaf = !getValue().getKey().isDirectory();
            }
            return isLeaf;
        }

        public void refresh() {
            // Update all children.
            expandedFiles.clear();
            storeAllExpanded(root);
            getChildren().setAll(buildChildren(this));
        }

        /**
         * Build the children of a tree item.
         *
         * @param treeItem The root item to build the children for.
         * @return A list of tree items that are the children of the given tree item.
         */
        private List<TreeItem<Pair<File, String>>> buildChildren(final TreeItem<Pair<File, String>> treeItem) {
            File f = treeItem.getValue().getKey();
            List<TreeItem<Pair<File, String>>> children = new ArrayList<>();

            if (f != null && f.isDirectory()) {
                // Get a list with all files in the directory.
                File[] files = f.listFiles(tbnShowAllFiles.isSelected() ? anyFileFilter : robotFileFilter);

                // Sort the list of files.
                Arrays.sort(files, this::compareFileOrder);

                // Create tree items from all files, add them to the list
                for (File file : files) {
                    ProjectTreeItem sub = new ProjectTreeItem(file, file.getName());
                    if (expandedFiles.contains(file)) {
                        sub.setExpanded(true);
                    }
                    children.add(sub);
                }
            }

            return children;
        }

        private int compareFileOrder(File o1, File o2) {
            // Put directories above files.
            if (o1.isDirectory() && o2.isFile()) {
                return -1;
            } else if (o1.isFile() && o2.isDirectory()) {
                return 1;
            } else {
                // Both are the same type, compare them normally.
                return o1.compareTo(o2);
            }
        }
    }

    /**
     * A custom tree cell which opens a robot tab on double-click and supports drag&drop.
     */
    private class CustomTreeCell extends TreeCell<Pair<File, String>> implements EventHandler<Event> {
        private static final String DRAG_OVER_CLASS = "drag-over";

        public CustomTreeCell() {
            // Subscribe to drag events.
            this.setOnDragDetected(this);
            this.setOnDragOver(this);
            this.setOnDragDropped(this);
            this.setOnDragEntered(this);
            this.setOnDragExited(this);
        }

        @Override
        protected void updateItem(final Pair<File, String> pair, final boolean empty) {
            super.updateItem(pair, empty);

            // Check if the pair or the string is null, set the text.
            if (pair == null || pair.getValue() == null) {
                setText("");
                return;
            }
            this.setText(pair.getValue());

            // Hook into the mouse double click event.
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() > 1
                        && pair.getKey() != null && pair.getKey().exists() && pair.getKey().isFile()) {
                    // Open new tab from file.
                    controller.openFile(toURL(pair.getKey()));
                }
            });
        }

        @Override
        public void handle(Event event) {
            if (event instanceof MouseEvent) {
                handleMouseEvent((MouseEvent) event);
            } else if (event instanceof DragEvent) {
                handleDragEvent((DragEvent) event);
            }
        }

        private void handleMouseEvent(MouseEvent mouseEvent) {
            if (mouseEvent.getEventType() == MouseEvent.DRAG_DETECTED && canDrag()) {
                // Start dragging.
                Dragboard board = this.startDragAndDrop(TransferMode.MOVE);

                // Set the clipboard content.
                ClipboardContent content = new ClipboardContent();
                content.putFiles(getAllCurrentFiles());
                board.setContent(content);

                mouseEvent.consume();
            }
        }

        private void handleDragEvent(DragEvent dragEvent) {
            if (!canDrop()) {
                return;
            }

            if (dragEvent.getEventType() == DragEvent.DRAG_OVER) {
                // Dragging over.
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            } else if (dragEvent.getEventType() == DragEvent.DRAG_ENTERED) {
                this.getStyleClass().add(DRAG_OVER_CLASS);
            } else if (dragEvent.getEventType() == DragEvent.DRAG_EXITED) {
                this.getStyleClass().remove(DRAG_OVER_CLASS);
            } else if (dragEvent.getEventType() == DragEvent.DRAG_DROPPED) {
                // Dropping.
                Dragboard board = dragEvent.getDragboard();
                if (board.hasFiles()) {
                    paste(this.getItem().getKey(), board.getFiles(), false);
                    dragEvent.setDropCompleted(true);
                }

                dragEvent.consume();
            }
        }

        private boolean canDrag() {
            // Projects cannot be moved.
            return !projectSelected();
        }

        private boolean canDrop() {
            return this.getTreeItem() != null;
        }
    }

    /*========== File filters ==========*/

    /**
     * A file filter filtering on the FXController.BOT_EXTENSION extension.
     */
    private class BotFileFilter extends FileFilter implements FilenameFilter {
        @Override
        public boolean accept(final File file) {
            return file.isDirectory() && !file.getName().startsWith(".") || file.getName().endsWith(XillEnvironment.ROBOT_EXTENSION);
        }

        @Override
        public String getDescription() {
            return String.format("Xillio bot script files (*%s)", XillEnvironment.ROBOT_EXTENSION);
        }

        @Override
        public boolean accept(final File directory, final String fileName) {
            return accept(new File(directory, fileName));
        }
    }

    /**
     * A file filter that accepts any file, except starting with a '.'.
     */
    private class AnyFileFilter extends FileFilter implements FilenameFilter {
        @Override
        public boolean accept(final File file) {
            return !file.getName().startsWith(".");
        }

        @Override
        public String getDescription() {
            return "Any file (*.*)";
        }

        @Override
        public boolean accept(final File directory, final String fileName) {
            return accept(new File(directory, fileName));
        }
    }
}
