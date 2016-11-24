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
package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillLoader;
import nl.xillio.xill.webservice.exceptions.XillEnvironmentLoadException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This class is responsible for loading installations of Xill.
 *
 * Xill environments created by this class have all plugins loaded.
 *
 * @author Thomas Biesaart
 * @author Geert Konijnendijk
 */
@Component
public class XillEnvironmentFactory implements FactoryBean<XillEnvironment> {

    @Autowired
    private XillRuntimeProperties properties;

    /**
     * Load Xill from the default home location.
     *
     * @return a Xill runtime
     * @throws XillEnvironmentLoadException When loading the environment fails
     */
    public XillEnvironment load() {
        return load(properties.getHome(), properties.getPluginDir());
    }

    /**
     * Load Xill from a specific folder.
     *
     * @param xillHome      the xill home folder
     * @param pluginFolders the xill plugins folders
     * @return a Xill runtime
     * @throws XillEnvironmentLoadException When loading the environment fails
     */
    public XillEnvironment load(Path xillHome, Path... pluginFolders) {
        try {
            XillEnvironment environment = XillLoader.getEnv(xillHome);
            for (Path path : pluginFolders) {
                if (path != null) {
                    environment.addFolder(path);
                }
            }

            environment.addFolder(xillHome);
            environment.loadPlugins();

            return environment;
        } catch (IOException e) {
            throw new XillEnvironmentLoadException(
                    "The xill installation could not be loaded. Either the installation is missing or it was corrupt.\n" +
                            "Please make sure you have manually installed xill and pointed the xill.home property to the root folder.",
                    e
            );
        }
    }

    @Override
    public XillEnvironment getObject() {
        return load();
    }

    @Override
    public Class<?> getObjectType() {
        return XillEnvironment.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
