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

import com.google.inject.Singleton;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.XillParsingException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * This class is responsible for running xill robots.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class XillExecutionService {
    private static final Logger LOGGER = Log.get();
    private XillEnvironment environment;

    public void init(XillEnvironment environment) {
        this.environment = environment;
    }

    public boolean isInitialized() {
        return environment != null;
    }

    public List<Issue> run(Path robot, Path project) throws IOException {
        requireInitialized();

        XillProcessor processor = environment.buildProcessor(project, robot);

        List<Issue> issueList = compile(processor);

        InstructionFlow<MetaExpression> result = processor.getRobot().process(processor.getDebugger());

        if (result.hasValue()) {
            LOGGER.info("Robot finished with return value: {}", result.get());
        } else {
            LOGGER.info("Robot finished with no return value");
        }

        return issueList;
    }

    private List<Issue> compile(XillProcessor processor) throws IOException {
        try {
            return processor.compile();
        } catch (XillParsingException e) {
            throw new CompileException(e);
        }
    }

    private void requireInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException("The execution service has not been initialized");
        }
    }
}
