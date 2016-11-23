package nl.xillio.xill.webservice.xill;

import nl.xillio.util.XillioHomeFolder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

/**
 * Properties for locating anf loading the Xill environment.
 *
 * @author Thomas Biesaart
 * @author Geert Konijnendijk
 */
@Component
@ConfigurationProperties(prefix = "xillRuntime")
public class XillRuntimeProperties {
    private Path home = new File(XillioHomeFolder.forXillServer(), "xill").toPath();
    private Path pluginDir = null;
    private Path licenseDir = null;
    private Path workspacesDir = new File(XillioHomeFolder.forXillServer(), "workspaces" + File.separator + "projects").toPath();

    public Path getLicenseDir() {
        return licenseDir;
    }

    public void setLicenseDir(File licenseDir) {
        if (licenseDir.isFile()) {
            this.licenseDir = licenseDir.getParentFile().toPath();
        } else {
            this.licenseDir = licenseDir.toPath();
        }
    }

    public Path getHome() {
        return home;
    }

    public void setHome(File home) {
        this.home = home.toPath();
    }

    public Path getPluginDir() {
        return pluginDir;
    }

    public void setPluginDir(File pluginDir) {
        this.pluginDir = pluginDir.toPath();
    }

    /**
     * @return The base path for persistent workspaces
     */
    public Path getWorkspacesDir() {
        return workspacesDir;
    }

    /**
     * @param workspaceDir The base path for persistent workspaces
     */
    public void setWorkspacesDir(Path workspaceDir) {
        this.workspacesDir = workspaceDir;
    }
}
