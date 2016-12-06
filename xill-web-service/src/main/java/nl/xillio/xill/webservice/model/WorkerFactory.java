/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.exceptions.BaseException;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.file.Path;

/**
 * A factory for constructing {@link Worker workers}.
 *
 * @author Geert Konijnendijk
 */
@Component
public class WorkerFactory {

    private ObjectPool<Runtime> runtimePool;

    @Inject
    public WorkerFactory(ObjectPool<Runtime> runtimePool) {
        this.runtimePool = runtimePool;
    }

    /**
     * Constructs a Worker.
     *
     * @param workDirectory the working directory
     * @param robotFQN the robot's fully qualified name
     * @return a new Worker
     * @throws BaseException when an exception occurs in {@link Worker#Worker(Path, String, ObjectPool)}
     */
    public Worker constructWorker(Path workDirectory, String robotFQN) throws BaseException {
        return new Worker(workDirectory, robotFQN, runtimePool);
    }
}
