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
package nl.xillio.xill.cli.commands;

import me.biesaart.wield.error.IllegalUserInputException;
import me.biesaart.wield.input.Arguments;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.cli.services.XillEnvironmentFactory;
import nl.xillio.xill.cli.services.XillExecutionService;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;


public class InitCommandTest {

    @Test
    public void testRun() throws IOException {
        XillExecutionService executionService = new XillExecutionService();
        XillEnvironment environment = mock(XillEnvironment.class);
        XillEnvironmentFactory factory = mock(XillEnvironmentFactory.class);
        when(factory.buildFor(any())).thenReturn(environment);

        InitCommand command = new InitCommand(factory, executionService);

        command.run(args());

        assertTrue(executionService.isInitialized());
    }

    @Test(expectedExceptions = IllegalUserInputException.class)
    public void testInvalidPath() {
        Arguments args = new Arguments();
        args.add("?\0?");
        new InitCommand(null, null).run(args);
    }

    // Disabled this test as it is influenced by an external environment variable XILL_HOME.
    // If it is present and points to a valid xill installation, this test really fails with a NPE
//    @Test(expectedExceptions = IllegalUserInputException.class)
//    public void testNoHome() {
//        new InitCommand(null, null).run(new Arguments());
//    }


    private Arguments args() {
        Arguments args = new Arguments();
        args.add(".");
        return args;
    }

}
