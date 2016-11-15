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

import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robot. This execution can be interrupted on a different thread.
 *
 * @author Thomas Biesaart
 */
public class XillWorker {
    private final String robot;

    public XillWorker(String robot) {
        this.robot = robot;
    }

    public String getRobot() {
        return robot;
    }

    /**
     * Return the id of the allocated worker.
     * The id must be unique across the XillWorkerPool.
     *
     * @return
     */
    public int getId() {
        throw new NotImplementedException("The 'getId' method has not been implemented yet");
    }

    public Object run(final Map<String, Object> arguments) {
        throw new NotImplementedException("The 'run' method has not been implemented yet");
    }

    public void abort() {
        throw new NotImplementedException("The 'abort' method has not been implemented yet");
    }
}
