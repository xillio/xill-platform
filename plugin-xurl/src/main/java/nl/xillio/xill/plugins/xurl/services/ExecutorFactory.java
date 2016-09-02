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
package nl.xillio.xill.plugins.xurl.services;

import com.google.inject.Singleton;
import nl.xillio.xill.plugins.xurl.data.Options;
import org.apache.http.client.fluent.Executor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * This class is responsible for creating an executor.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class ExecutorFactory {

    public Executor buildExecutor(Options options) {

        HttpClientBuilder builder = HttpClients.custom();

        if (options.isInsecure()) {
            builder.setSSLHostnameVerifier(new NoopHostnameVerifier());
        }

        if (!options.isEnableRedirect()) {
            builder.disableRedirectHandling();
        }

        return Executor.newInstance(builder.build());
    }
}
