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

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robot. This execution can be interrupted on a different thread.
 *
 * @author Thomas Biesaart
 */
public class Worker {
    private final String robot;

    public Worker(String robot) {
        this.robot = robot;
    }

    public String getRobot() {
        return robot;
    }
}
