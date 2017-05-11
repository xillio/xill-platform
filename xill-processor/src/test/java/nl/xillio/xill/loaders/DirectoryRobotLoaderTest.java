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
package nl.xillio.xill.loaders;

import me.biesaart.utils.FileUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class DirectoryRobotLoaderTest {
    private Path dir;

    private DirectoryRobotLoader parentLoader;
    private DirectoryRobotLoader childLoader;
    private URL parentUrl;

    @BeforeClass
    private void beforeClass() throws IOException {
        dir = Files.createTempDirectory(getClass().getSimpleName());

        // Create the test files.
        Path parentLoaderDir = Files.createDirectory(dir.resolve("parent"));
        Path parentSub = Files.createDirectory(parentLoaderDir.resolve("sub"));
        Path parentFile = Files.createFile(parentSub.resolve("robot.xill"));
        Files.write(parentFile, Collections.singletonList("Parent robot"));
        parentUrl = parentFile.toUri().toURL();
        Path childLoaderDir = Files.createDirectory(dir.resolve("child"));
        Path childSub = Files.createDirectory(childLoaderDir.resolve("sub"));
        Files.createFile(childSub.resolve("robot.xill"));

        // Create the robot loaders.
        parentLoader = new DirectoryRobotLoader(null, parentLoaderDir);
        childLoader = new DirectoryRobotLoader(parentLoader, childLoaderDir);
    }

    @AfterClass
    private void afterClass() throws IOException {
        FileUtils.deleteDirectory(dir.toFile());
    }

    @Test
    public void testGetRobot() {
        assertEquals(parentLoader.getRobot("sub.robot"), parentUrl);
    }

    @Test
    public void testGetRobotParentOverride() {
        assertEquals(childLoader.getRobot("sub.robot"), parentUrl);
    }

    @Test
    public void testGetRobotNotExists() {
        assertNull(childLoader.getRobot("non.existent"));
    }

    @Test
    public void testGetRobotAsStreamNotExists() throws IOException {
        assertNull(childLoader.getRobotAsStream("non.existent"));
    }

    @Test
    public void testGetRobotAsStream() throws IOException {
        try (InputStream stream = childLoader.getRobotAsStream("sub.robot")) {
            assertEquals(new Scanner(stream).nextLine(), "Parent robot");
        }
    }

    @Test
    public void testGetResource() {
        assertEquals(parentLoader.getResource("sub/robot.xill"), parentUrl);
    }

    @Test
    public void testGetResourceParentOverride() {
        assertEquals(childLoader.getResource("sub/robot.xill"), parentUrl);
    }

    @Test
    public void testGetResourceNotExists() {
        assertNull(childLoader.getResource("non/existent"));
    }

    @Test
    public void testGetResourceAsStreamNotExists() throws IOException {
        assertNull(childLoader.getResourceAsStream("non/existent"));
    }

    @Test
    public void testGetResourceAsStream() throws IOException {
        try (InputStream stream = childLoader.getResourceAsStream("sub/robot.xill")) {
            assertEquals(new Scanner(stream).nextLine(), "Parent robot");
        }
    }

    @Test
    public void testGetResourceAbsolutePath() {
        assertNull(childLoader.getResource("/"));
    }

    @Test
    public void testGetResourceUp() {
        assertNull(parentLoader.getResource("../child/sub/robot.xill"));
    }

    @Test
    public void testGetResourceCaseSensitive() {
        assertNull(parentLoader.getResource("sub/ROBOT.xill"));
    }

    @Test
    public void testBackslashes() {
        String os = System.getProperty("os.name");
        if (!os.contains("indows")) {
            throw new SkipException("Skipping Windows test on " + os);
        }

        assertEquals(parentLoader.getResource("sub\\robot.xill"), parentUrl);
    }

    @Test
    public void testNormalize() {
        assertEquals(parentLoader.getResource("sub/../sub/robot.xill"), parentUrl);
    }
}
