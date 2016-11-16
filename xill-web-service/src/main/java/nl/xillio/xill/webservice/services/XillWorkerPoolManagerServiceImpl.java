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
package nl.xillio.xill.webservice.services;

import com.google.inject.Singleton;
import nl.xillio.xill.webservice.model.XillWorkerPool;
import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;
import java.util.Optional;

/**
 * This class represents an implementation of the worker pools manager.
 */
@Singleton
public class XillWorkerPoolManagerServiceImpl implements XillWorkerPoolManagerService {
    @Override
    public XillWorkerPool getWorkerPool(final Path workDirectory) {
        throw new NotImplementedException("The 'getWorkerPool' method has not been implemented yet");
    }

    @Override
    public XillWorkerPool getDeafultWorkerPool() {
        throw new NotImplementedException("The 'getWorkerPool' method has not been implemented yet");
    }

    @Override
    public Optional<XillWorkerPool> findWorkerPool(final XWID projectId) {
        throw new NotImplementedException("The 'findWorkerPool' method has not been implemented yet");
    }
}
