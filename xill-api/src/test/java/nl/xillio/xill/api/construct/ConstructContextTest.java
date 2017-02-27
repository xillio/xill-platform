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

import nl.xillio.xill.api.*;
import nl.xillio.xill.api.components.RobotID;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;


public class ConstructContextTest {
    @Test
    public void testCreateChildProcessorReceivesOutputHandler() throws Exception {
        XillEnvironment environment = mock(XillEnvironment.class);
        XillProcessor processor = mock(XillProcessor.class);
        when(environment.buildProcessor(any(), any(), any())).thenReturn(processor);

        OutputHandler outputHandler = new DefaultOutputHandler();

        ConstructContext constructContext = new ConstructContext(
                Paths.get("."),
                RobotID.dummyRobot(),
                RobotID.dummyRobot(),
                null,
                new NullDebugger(),
                UUID.randomUUID(),
                outputHandler,
                null,
                null
        );

        XillProcessor child = constructContext.createChildProcessor(Paths.get("test.xill"), environment);

        // Check if the OutputHandler is set correctly
        ArgumentCaptor<OutputHandler> handlerArgumentCaptor = ArgumentCaptor.forClass(OutputHandler.class);
        verify(child).setOutputHandler(handlerArgumentCaptor.capture());
        assertSame(handlerArgumentCaptor.getValue(), outputHandler);
    }

}