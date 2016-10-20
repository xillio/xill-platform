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
package nl.xillio.migrationtool;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.gui.FXController;
import nl.xillio.plugins.ContenttoolsPlugin;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.docgen.impl.XillDocGen;
import nl.xillio.xill.util.settings.SettingsHandler;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Launcher class, is used to launch processors in their own threads, facilitates a simple Log, and provides commandline running.
 */
public class Loader implements ContenttoolsPlugin {
    /**
     * The GUI's current official version.
     */
    public static final String SHORT_VERSION;
    /**
     * The release date of the official version.
     */
    public static final String VERSION_DATE;
    /**
     * Just the version number + date.
     */
    public static final String LONG_VERSION;
    public static final String APP_TITLE;
    private static final Manifest MANIFEST;
    private static final Logger LOGGER;
    private static XillEnvironment xill;
    private static XillInitializer initializer;

    static {
        Logger logger = Log.get();
        try {
            String path = Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            // Do not try to read the manifest when we are not running from a jar, i.e. this is a development version
            if (path.endsWith(".jar")) {
                MANIFEST = new JarFile(path).getManifest();
            } else {
                MANIFEST = null;
            }
        } catch (URISyntaxException | IOException e) {
            throw new XillioRuntimeException("Failed to find running jar file", e);
        }

        String date;
        if (MANIFEST != null) {
            date = MANIFEST.getMainAttributes().getValue("Created-On");
            try {
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(date);
                date = DateFormat.getDateInstance().format(parsedDate);
            } catch (ParseException e) {
                logger.error("Failed to parse date from manifest", e);
            }
        } else {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(new Date());
        }
        SHORT_VERSION = Loader.class.getPackage().getImplementationVersion() == null ? "dev" : Loader.class.getPackage().getImplementationVersion();
        VERSION_DATE = date;
        LOGGER = logger;
        LONG_VERSION = SHORT_VERSION + ", " + date;
        String appTitle = Loader.class.getPackage().getImplementationTitle();
        APP_TITLE = appTitle == null ? "Xill IDE Development" : appTitle;
    }

    /**
     * @return the currently used initializer
     */
    public static XillInitializer getInitializer() {
        return initializer;
    }

    /**
     * Shortcut to load FXML
     *
     * @param resource
     * @param controller
     * @return The node represented in the FXML resource
     * @throws IOException
     */
    public static Node load(final URL resource, final Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setClassLoader(controller.getClass().getClassLoader());
        loader.setController(controller);
        return loader.load();
    }

    /**
     * @return The xill implementation this was initialized with
     */
    public static XillEnvironment getXill() {
        return xill;
    }

    @Override
    @SuppressWarnings("squid:S1147") // Exit methods should not be called.
    public void start(final Stage primaryStage, final XillEnvironment xill) {
        try {
            checkJRE();
        } catch (IOException e) {
            LOGGER.error("JRE Check Failed", e);
            alert("Something went wrong while starting the application: " + e.getLocalizedMessage());
        }
        Loader.xill = xill;
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        try {
            // Initial load of settings
            SettingsHandler.loadSettings();
        } catch (IOException e) {
            LOGGER.error("Could not read settings file", e);
            recoverSettings(primaryStage);
            return;
        }

        // Continue starting normally if the settings can be loaded
        startIDE(primaryStage);
    }

    @Override
    public Consumer<String> getSingleInstanceHandler() {
        return FXController.OPEN_ROBOT_EVENT::invoke;
    }

    /**
     * Show a dialog asking the user to recover settings from a backup.
     * When this fails, the settings file is deleted and the default settings are written. When this too fails, exit.
     * <p>
     * When one of these steps succeeds, the IDE continues loading.
     * </p>
     *
     * @param primaryStage The stage to add all elements to
     */
    private void recoverSettings(Stage primaryStage) {
        // Try to recover the settings from a backup
        AlertDialog dialog = new AlertDialog(AlertType.ERROR, "Recover settings",
                "The settings file could not be read", "Do you want to recover the last working settings file?",
                ButtonType.YES, ButtonType.NO);

        dialog.showAndWait();

        if (dialog.getResult().get() == ButtonType.YES) {
            try {
                // Recover from backup settings
                SettingsHandler.recoverSettings();
                // Reload the settings after recovery
                SettingsHandler.loadSettings();
            } catch (IOException e) {
                LOGGER.error("Could not recover settings, writing defaults", e);
                try {
                    // Try to write the default settings when the backup settings cannot be loaded
                    SettingsHandler.forceDefaultSettings();
                } catch (IOException e1) {
                    LOGGER.error("Could not write default settings, exiting", e);
                    // System.exit is appropriate here since there is a fatal error.
                    System.exit(1);
                }
            }
        } else {
            // Simply exit when the user chooses not to recover the settings
            Platform.exit();
        }

        // When recovery succeeds, continue starting the IDE
        startIDE(primaryStage);
    }

    /**
     * Starts the Xill IDE.
     *
     * @param primaryStage The stage to add all elements to
     */
    @SuppressWarnings("squid:S1147") // Exit methods should not be called.
    private void startIDE(Stage primaryStage) {
        // Start loading plugins
        initializer = new XillInitializer(new XillDocGen());
        initializer.start();

        try (InputStream image = getClass().getResourceAsStream("/icon.png")) {
            if (image != null) {
                primaryStage.getIcons().add(new Image(image));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        Parent root;
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/Glober SemiBold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Glober xBold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/UbuntuMono Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/UbuntuMono Bold.ttf"), 10);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/contenttools.fxml"));
            root = loader.load();

            Scene scene = new Scene(root, 1024, 768);

            primaryStage.setScene(scene);

            // Maximize the window.
            primaryStage.setMaximized(true);
        } catch (IOException e) {
            LOGGER.error("Loader.initGUI(): Fatal error occurred during launch: " + e.getMessage(), e);
            // System.exit is appropriate here since there is a fatal error.
            System.exit(1);
        }

        primaryStage.setTitle(APP_TITLE + " - " + LONG_VERSION);
        primaryStage.getScene().lookup("#apnRoot").setVisible(false);
        primaryStage.show();

        Platform.runLater(() -> primaryStage.getScene().lookup("#apnRoot").setVisible(true));
    }

    private void checkJRE() throws IOException {
        // If we are not running from a jar, i.e. this is a development version, do not run the check
        if (MANIFEST == null)
            return;

        String expectedVersion = MANIFEST.getMainAttributes().getValue("Build-Jdk");
        if (expectedVersion == null) {
            throw new IOException("No java version was found. This is not a build.");
        }
        String actualVersion = System.getProperty("java.version");

        if (!actualVersion.equals(expectedVersion)) {
            throw new IOException("The application was built with java version " + expectedVersion + " but is running with " + actualVersion + ".");
        }
    }

    private void alert(String message) {
        new AlertDialog(AlertType.ERROR, "Warning", "", message).showAndWait();
    }
}
