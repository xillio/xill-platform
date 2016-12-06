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

import nl.xillio.xill.webservice.exceptions.XillBaseException;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.file.Path;

/**
 * A factory for constructing {@link XillWorker workers}.
 *
 * @author Geert Konijnendijk
 */
@Component
public class XillWorkerFactory {

    private ObjectPool<XillRuntime> runtimePool;

    @Inject
    public XillWorkerFactory(ObjectPool<XillRuntime> runtimePool) {
        this.runtimePool = runtimePool;
    }

    /**
     * Construct a {@link XillWorker}.
     *
     * @param workDirectory The working directory
     * @param robotFQN The robot's fully qualified name
     * @return A new {@link XillWorker}
     * @throws XillBaseException When an exception occurs in {@link XillWorker#XillWorker(Path, String, ObjectPool)}
     * @see XillWorker
     */
    public XillWorker constructWorker(Path workDirectory, String robotFQN) throws XillBaseException {
        return new XillWorker(workDirectory, robotFQN, runtimePool);
    }
}
