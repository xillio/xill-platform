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
package nl.xillio.xill.integration;


import me.biesaart.utils.Log;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillLoader;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.integration.embeddedmongo.EmbeddedMongoHelper;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.testng.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RobotsIT {
    private static final Logger LOGGER = Log.get();
    private Path projectPath = Paths.get("target/integration-test", getClass().getName());
    private Path pluginPath = Paths.get("../xill-ide-launcher/plugins");
    private XillEnvironment xillEnvironment;

    @BeforeSuite
    public void loadXill() throws IOException {
        xillEnvironment = XillLoader.getEnv(pluginPath);
        xillEnvironment.addFolder(pluginPath);
        xillEnvironment.loadPlugins();
    }

    /**
     * Copy resources from the resources package to the same location in the working directory
     *
     * @throws IOException When copying fails
     */
    @BeforeClass
    public void copyResources() throws IOException {
        Reflections reflections = new Reflections(getResourcesPackage(), new ResourcesScanner());

        for (String r : reflections.getResources(a -> true)) {
            // Open the resource as an input stream from the class path
            try (InputStream stream = getClass().getResourceAsStream("/" + r)) {
                // Robot is run from the project path, copy resources to there
                Path resourcePath = projectPath.resolve(r);

                Files.createDirectories(resourcePath.getParent());
                Files.copy(stream, resourcePath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @BeforeClass
    public void startEmbeddedMongo() throws IOException {
        EmbeddedMongoHelper.start();
    }

    @AfterClass
    public void stopEmbeddedMongo() throws IOException {
        EmbeddedMongoHelper.stop();
    }

    protected String getPackage() {
        return "tests";
    }

    protected String getResourcesPackage() {
        return "testresources";
    }

    @DataProvider(name = "robots")
    public Object[][] getRobots() {
        // Scan for files in the tests package but exclude test resources
        Reflections reflections = new Reflections(getPackage(), new ResourcesScanner());
        return reflections
                .getResources(this::botnameFilter)
                .stream()
                .map(resource -> new Object[]{getClass().getResource("/" + resource), resource})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "robots")
    public void runRobot(URL robot, String name) throws IOException, XillParsingException {
        LOGGER.info("Testing {}", name);
        Path robotFile = projectPath.resolve(name);

        // cleanup mongo before each test (to make sure previous failures do not influence this test)
        EmbeddedMongoHelper.cleanupDB();

        if (Files.exists(robotFile)) {
            Files.delete(robotFile);
        }

        Files.createDirectories(robotFile.getParent());
        Files.createFile(robotFile);
        Files.copy(robot.openStream(), robotFile, StandardCopyOption.REPLACE_EXISTING);

        XillProcessor processor = xillEnvironment.buildProcessor(projectPath, robotFile);

        processor.compile();
        processor.getRobot().process(processor.getDebugger());
    }

    /**
     * Criteria to determine whether a bot will be used for integration testing.
     * This allows to include bots in the resource directory which are intended to be used as libraries only.
     *
     * @param resourceName file name of the robot
     * @return true if is an integration test bot
     */
    boolean botnameFilter(String resourceName) {
        return !resourceName.startsWith("NoTest");
    }
}
