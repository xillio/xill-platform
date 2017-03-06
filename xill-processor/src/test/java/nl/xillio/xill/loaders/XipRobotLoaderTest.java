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

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class XipRobotLoaderTest {
    private Path dir;

    private XipRobotLoader parentLoader;
    private XipRobotLoader childLoader;
    private URL parentFile;

    @BeforeClass
    private void beforeClass() throws IOException {
        dir = Files.createTempDirectory(getClass().getSimpleName());

        // Create the test files.
        Path childXip = dir.resolve("child.xip");
        ZipOutputStream childStream = new ZipOutputStream(new FileOutputStream(childXip.toFile()));
        ZipEntry childEntry = new ZipEntry("sub/robot.xill");
        childStream.putNextEntry(childEntry);
        childStream.closeEntry();
        childStream.close();
        Path parentXip = dir.resolve("parent.xip");
        ZipOutputStream parentStream = new ZipOutputStream(new FileOutputStream(parentXip.toFile()));
        ZipEntry parentEntry = new ZipEntry("sub/robot.xill");
        parentStream.putNextEntry(parentEntry);
        parentStream.write("Parent robot".getBytes());
        parentStream.closeEntry();
        parentStream.close();

        parentFile = new URL("jar:" + parentXip.toUri().toURL() + "!/sub/robot.xill");

        // Create the robot loaders.
        parentLoader = new XipRobotLoader(null, parentXip);
        childLoader = new XipRobotLoader(parentLoader, childXip);
    }

    @AfterClass
    private void afterClass() throws IOException {
        FileUtils.deleteDirectory(dir.toFile());
    }

    @Test
    public void testGetRobot() {
        assertEquals(parentLoader.getRobot("sub.robot"), parentFile);
    }

    @Test
    public void testGetRobotParentOverride() {
        assertEquals(childLoader.getRobot("sub.robot"), parentFile);
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
        assertEquals(new Scanner(childLoader.getRobotAsStream("sub.robot")).nextLine(), "Parent robot");
    }

    @Test
    public void testGetResource() {
        assertEquals(parentLoader.getResource("sub/robot.xill"), parentFile);
    }

    @Test
    public void testGetResourceParentOverride() {
        assertEquals(childLoader.getResource("sub/robot.xill"), parentFile);
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
        assertEquals(new Scanner(childLoader.getResourceAsStream("sub/robot.xill")).nextLine(), "Parent robot");
    }
}
