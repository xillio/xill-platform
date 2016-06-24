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
package nl.xillio.xill.cli.services;

import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.errors.XillParsingException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class XillExecutionServiceTest {
    @Test
    public void testRun() throws IOException, XillParsingException {
        Robot robot = mock(Robot.class);
        when(robot.process(any())).thenReturn(InstructionFlow.doReturn());

        XillProcessor processor = mock(XillProcessor.class);
        when(processor.getRobot()).thenReturn(robot);

        XillEnvironment environment = mock(XillEnvironment.class);
        when(environment.buildProcessor(any(), any())).thenReturn(processor);

        XillExecutionService service = new XillExecutionService();
        service.init(environment);

        service.run(Paths.get("."), Paths.get("."));

        verify(environment).buildProcessor(Paths.get("."), Paths.get("."));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNotInitialized() throws IOException {
        XillExecutionService service = new XillExecutionService();
        service.run(Paths.get("."), Paths.get("."));
    }
}
