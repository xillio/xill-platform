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
import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.model.XillWorkerPool;
import org.apache.commons.lang3.NotImplementedException;

/**
 * This class represents an implementation of the worker pools manager.
 */
@Singleton
public class XillWorkerPoolManagerService {

    /**
     * Create new or return existing XillWorkerPool for given projectId.
     *
     * @param projectId The project id. For the moment, it's not used as there is no support for projects yet.
     * @return The XillWorkerPool for given projectId that has been created or reused. For the moment, it always returns one XillWorkerPool as there is no support for projects yet.
     */
    public XillWorkerPool getWorkerPool(final String projectId) {
        throw new NotImplementedException("The 'getWorkerPool' method has not been implemented yet");
    }

    /**
     * Return existing XillWorkerPool for given projectId.
     *
     * @param projectId The project id. For the moment, it's not used as there is no support for projects yet.
     * @return The existing XillWorkerPool for given projectId.
     * @throws XillNotFoundException if XillWorkerPool for given projectId does not exist.
     */
    public XillWorkerPool findWorkerPool(final String projectId) throws XillNotFoundException {
        throw new NotImplementedException("The 'findWorkerPool' method has not been implemented yet");
    }
}
