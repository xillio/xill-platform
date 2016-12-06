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
package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.webservice.model.XillRuntime;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Factory for pooled {@link XillRuntime} instances.
 *
 * @author Geert Konijnendijk
 */
@Component
public class RuntimePooledObjectFactory extends BasePooledObjectFactory<XillRuntime> {

    private Provider<XillRuntime> xillRuntimeProvider;

    @Inject
    public RuntimePooledObjectFactory(Provider<XillRuntime> xillRuntimeProvider) {
        this.xillRuntimeProvider = xillRuntimeProvider;
    }

    @Override
    public XillRuntime create() throws Exception {
        return xillRuntimeProvider.get();
    }

    @Override
    public PooledObject<XillRuntime> wrap(XillRuntime obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<XillRuntime> p) throws Exception {
        p.getObject().close();
    }
}
