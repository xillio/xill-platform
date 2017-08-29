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
package nl.xillio.xill.api.construct;

import nl.xillio.events.EventHost;
import nl.xillio.xill.api.*;
import nl.xillio.xill.api.components.EventEx;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.events.RobotStartedAction;
import nl.xillio.xill.api.events.RobotStoppedAction;
import nl.xillio.xill.api.io.ResourceLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * This class represents a context in which a construct can be processed.
 */
public class ConstructContext {

    private final Path workingDirectory;
    private final RobotID robotID;
    private final RobotID rootRobot;
    private final UUID compilerSerialId;
    private final OutputHandler outputHandler;
    private final ResourceLoader resourceLoader;
    /**
     * Events for notifying constructs that robots have started or stopped.
     * Example uses are initialization and cleanup.
     */
    private final EventHost<RobotStartedAction> robotStartedEvent;
    private final EventHost<RobotStoppedAction> robotStoppedEvent;
    /**
     * This event is used to forward deprecated method calls to the correct methods.
     *
     * @deprecated Used to support the deprecated {@link ConstructContext#getOnRobotInterrupt()}
     */
    @Deprecated
    private final EventEx<Object> mockInterruptEvent = new MockInterruptEvent();
    private Logger robotLogger;
    private Logger rootLogger;
    private Debugger debugger;

    /**
     * Creates a new {@link ConstructContext} for a specific robot.
     *
     * @param workingDirectory  the workingDirectory of the current robot
     * @param robot             the robotID of the current robot
     * @param rootRobot         the robotID of the root robot
     * @param construct         the construct that will be using this context
     * @param resourceLoader
     * @param debugger          the debugger that is being used
     * @param compilerSerialId  the serial id of the compiler instance
     * @param outputHandler     the event handler for all robot output
     * @param robotStartedEvent the event host for started robots
     * @param robotStoppedEvent the event host for stopped robots
     */
    public ConstructContext(final Path workingDirectory, final RobotID robot, final RobotID rootRobot, final Construct construct, ResourceLoader resourceLoader, final Debugger debugger, UUID compilerSerialId, OutputHandler outputHandler, final EventHost<RobotStartedAction> robotStartedEvent,
                            final EventHost<RobotStoppedAction> robotStoppedEvent) {
        this.workingDirectory = workingDirectory;
        robotID = robot;
        this.rootRobot = rootRobot;
        this.resourceLoader = resourceLoader;
        this.compilerSerialId = compilerSerialId;
        this.outputHandler = outputHandler;
        this.robotStartedEvent = robotStartedEvent;
        this.robotStoppedEvent = robotStoppedEvent;
        this.debugger = debugger;
    }

    /**
     * Creates a new {@link ConstructContext} for a specific robot.
     *
     * @param workingDirectory  the workingDirectory of the current robot
     * @param robot             the robotID of the current robot
     * @param rootRobot         the robotID of the root robot
     * @param construct         the construct that will be using this context
     * @param debugger          the debugger that is being used
     * @param compilerSerialId  the serial id of the compiler instance
     * @param robotStartedEvent the event host for started robots
     * @param robotStoppedEvent the event host for stopped robots
     * @param resourceLoader
     */
    public ConstructContext(final Path workingDirectory, final RobotID robot, final RobotID rootRobot, final Construct construct, final Debugger debugger, UUID compilerSerialId, final EventHost<RobotStartedAction> robotStartedEvent,
                            final EventHost<RobotStoppedAction> robotStoppedEvent, ResourceLoader resourceLoader) {
        this(workingDirectory, robot, rootRobot, construct, resourceLoader, debugger, compilerSerialId, new DefaultOutputHandler(), robotStartedEvent, robotStoppedEvent);
    }

    /**
     * @return the workingDirectory of the current robot
     */
    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @return the robotID
     */
    public RobotID getRobotID() {
        return robotID;
    }

    /**
     * @return the robotID for the Root Robot
     */
    public RobotID getRootRobot() {
        return rootRobot;
    }

    /**
     * @return the robotLogger
     */
    public Logger getLogger() {

        // Make sure the logger is set
        if (robotLogger == null) {
            robotLogger = LogUtil.getLogger(robotID, outputHandler);
        }

        return robotLogger;
    }

    /**
     * @return the root RobotAppender
     */
    public Logger getRootLogger() {

        // Make sure the logger is set
        if (rootLogger == null) {
            rootLogger = LogUtil.getLogger(rootRobot, outputHandler);
        }

        return rootLogger;
    }

    /**
     * Gets a {@link ResourceLoader} object.
     *
     * @return ResourceLoader object
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * Adds a listener that will be called when a robot is started.
     *
     * @param listener the listener to add
     */
    public void addRobotStartedListener(Consumer<RobotStartedAction> listener) {
        if (robotStartedEvent != null) {
            robotStartedEvent.getEvent().addListener(listener);
        }
    }

    /**
     * Adds a listener that will be called when a robot is stopped.
     *
     * @param listener the listener to add
     */
    public void addRobotStoppedListener(Consumer<RobotStoppedAction> listener) {
        if (robotStoppedEvent != null) {
            robotStoppedEvent.getEvent().addListener(listener);
        }
    }

    /**
     * This method is deprecated. Use {@link ConstructContext#addRobotInterruptListener(Consumer)}.
     *
     * @return a mocked {@link EventEx} that will forward the calls to the non-deprecated api
     * @deprecated Replaced be addRobotInterruptListener and removeRobotInterruptListener
     */
    @Deprecated
    public EventEx<Object> getOnRobotInterrupt() {
        return mockInterruptEvent;
    }

    /**
     * Adds a listener that will be called when a robot is interrupted.
     *
     * @param listener the listener to add
     * @return true if listener has been added
     */
    public boolean addRobotInterruptListener(Consumer<Object> listener) {
        if (debugger == null || debugger.getOnRobotInterrupt() == null) {
            return false;
        } else {
            debugger.getOnRobotInterrupt().addListener(listener);
            return true;
        }
    }

    /**
     * Removes a listener that was previously added by addRobotInterruptListener method
     *
     * @param listener the listener to remove
     * @return true if listener has been removed
     */
    public boolean removeRobotInterruptListener(Consumer<Object> listener) {
        if (debugger == null || debugger.getOnRobotInterrupt() == null) {
            return false;
        } else {
            debugger.getOnRobotInterrupt().removeListener(listener);
            return true;
        }
    }

    /**
     * Gets the serial number of the compiler used to compile the script of this context.
     *
     * @return the serial number
     */
    public UUID getCompilerSerialId() {
        return compilerSerialId;
    }

    /**
     * Create a processor using the current debugger as the parent.
     *
     * @param robotPath       the robot that should be compiled
     * @param xillEnvironment the xill environment
     * @return the processor
     * @throws IOException if an IO error occurs
     */
    public XillProcessor createChildProcessor(String robotPath, XillEnvironment xillEnvironment) throws IOException {
        URL robotURL = resourceLoader.getResource(robotPath);

        Path[] basePaths = resourceLoader.getBasePaths().toArray(new Path[0]);
        XillProcessor processor = xillEnvironment.buildProcessor(
                workingDirectory,
                new RobotID(robotURL, robotPath),
                debugger.createChild(),
                basePaths
        );

        processor.setOutputHandler(outputHandler);
        return processor;
    }

    /**
     * This class forwards deprecated calls to the correct implementation
     *
     * @deprecated we no longer use the getter. Use the delegation methods instead.
     */
    @Deprecated
    private class MockInterruptEvent extends EventEx<Object> {
        @Override
        public synchronized void addListener(Consumer<Object> listener) {
            addRobotInterruptListener(listener);
        }

        @Override
        public synchronized void removeListener(Consumer<Object> listener) {
            removeRobotInterruptListener(listener);
        }
    }
}
