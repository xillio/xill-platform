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
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for creating an executor.
 *
 * @author Thomas Biesaart, Ernst van Rheenen
 */
@Singleton
public class ExecutorFactory {

    private static final String DEFAULT_CLIENT_ID = "DEFAULT_CLIENT_ID";
    private final Map<String, Executor> executors = new HashMap<>();

    /**
     * Creates a new Executor based on the provided client options
     * @param options Options to be used for setting up the http connection
     * @return An http client executor
     */
    public synchronized Executor buildExecutor(Options options) {
        String uuid = getSessionID(options);

        Executor executor;
        if(executors.containsKey(uuid)) {
            executor = executors.get(uuid);
        } else {
            executor = Executor.newInstance(buildClient(defaultBuilder(), options));
            executors.put(uuid, executor);
        }

        return executor;
    }


    /**
     * Returns a specific session UUID based on the provided options.
     *
     * The system will as much as possible try to keep sessions for domains. Because we cannot retroactively set the
     * insecure and redirect options, the session will be lost when one of these options is changed by the user.
     *
     * @param options Options to be set
     * @return The UUID for this session
     */
    public static String getSessionID(Options options) {
        String id;
        if(options.isBasicAuthEnabled()) {
            id = options.getBasicAuth().getUsername();
        } else if (options.isNTLMEnabled()) {
            id = options.getNTLMOptions().getUsername() + "@" + options.getNTLMOptions().getDomain();
        } else if(options.isProxyEnabled()) {
            id = options.getProxyOptions().getUsername() + "@" + options.getProxyOptions().getHttpHost().toHostString();
        } else {
            id = DEFAULT_CLIENT_ID;
        }
        return id + "[" + options.isInsecure() + "," + options.isEnableRedirect() + "]";
    }

    /**
     * Creates a new HttpClient using the specified options
     * @param options Options to be set
     * @return a new HttpClient
     */
    public static HttpClient buildClient(HttpClientBuilder builder, Options options) {

        if(options.isInsecure()){
            builder.setSSLHostnameVerifier(new NoopHostnameVerifier());
        }

        if(!options.isEnableRedirect()){
            builder.disableRedirectHandling();
        }

        return builder.build();
    }

    /**
     * Code borrowed from private implementation of org.apache.http.client.fluent.Executor
     * @return A preconfigured HttpClientBuilder
     */
    public static HttpClientBuilder defaultBuilder() {
        SSLConnectionSocketFactory ssl = null;

        try {
            ssl = SSLConnectionSocketFactory.getSystemSocketFactory();
        } catch (SSLInitializationException var7) {
            try {
                SSLContext sslcontext = SSLContext.getInstance("TLS");
                sslcontext.init((KeyManager[])null, (TrustManager[])null, (SecureRandom)null);
                ssl = new SSLConnectionSocketFactory(sslcontext);
            } catch (SecurityException var4) {
                ;
            } catch (KeyManagementException var5) {
                ;
            } catch (NoSuchAlgorithmException var6) {
                ;
            }
        }

        Registry sfr = RegistryBuilder.create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", ssl != null?ssl:SSLConnectionSocketFactory.getSocketFactory()).build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(sfr);
        connectionManager.setDefaultMaxPerRoute(100);
        connectionManager.setMaxTotal(200);
        connectionManager.setValidateAfterInactivity(1000);
        HttpClientBuilder builder = HttpClientBuilder.create().setConnectionManager(connectionManager);

        return builder;
    }
}
