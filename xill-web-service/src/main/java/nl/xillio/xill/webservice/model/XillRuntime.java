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
package nl.xillio.xill.webservice.model;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.XillEnvironment;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents an instance of the Xill environment able to run a single robot with low latency.
 *
 * This implementation allows for {@link XillRuntime runtimes} to be kept in a pool, preventing
 * unnecessary recreation of environments.
 *
 * @author Geert Konijnendijk
 */
public class XillRuntime implements AutoCloseable {

    public void compile(Path workingDirectory, Path robotPath)  {

    }

    public Object runRobot(Map<String, Object> parameters, OutputHandler outputHandler) {
        return null;
    }

    public void abortRobot() {

    }

    @Override
    public void close() {

    }

    @Inject
    public void setXillEnvironmentProvider(Provider<XillEnvironment> xillEnvironmentProvider) {

    }
}
