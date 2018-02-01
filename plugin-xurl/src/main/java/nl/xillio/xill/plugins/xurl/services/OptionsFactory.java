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
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.OptionsEnum;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xurl.data.Credentials;
import nl.xillio.xill.plugins.xurl.data.NTLMOptions;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.plugins.xurl.data.ProxyOptions;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;

import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;


/**
 * This class is responsible for applying options to a request.
 *
 * @author Thomas Biesaart
 * @author Andrea Parrilli
 */
@Singleton
public class OptionsFactory {
    private static final String NO_NULL_MESSAGE = "The %s option cannot be null";
    private Options defaultOptions = new Options();

    private static int getInt(MetaExpression value, String label) {
        assertValue(value, "The %s option must contain a number", label);

        if (Double.isNaN(value.getNumberValue().doubleValue())) {
            throw new RobotRuntimeException("The " + label + " option must contain a number");
        }

        return value.getNumberValue().intValue();
    }

    private static String getString(MetaExpression value, String label) {
        assertValue(value, "The %s option must contain a string", label);

        return value.getStringValue();
    }

    private static boolean getBoolean(MetaExpression value, String label) {
        assertValue(value, NO_NULL_MESSAGE, label);
        return value.getBooleanValue();
    }

    /**
     * Extract credentials from a MetaExpression.
     *
     * @param object the expression
     * @param option the name of the option. This is for error messages
     * @return the credentials
     * @throws RobotRuntimeException if the credentials were missing
     */
    private static Credentials getCredentials(MetaExpression object, String option) {
        Map<String, MetaExpression> map = toMap(object, option);
        MetaExpression username = map.get("username");
        assertValue(username, "The %s option must contain a 'username' field", option);
        MetaExpression password = map.get("password");
        assertValue(password, "The %s option must contain a 'password' field", option);

        return new Credentials(username.getStringValue(), password.getStringValue());
    }

    private static Map<String, MetaExpression> toMap(MetaExpression options, String parameterName) {
        if (options.getType() != ExpressionDataType.OBJECT) {
            throw new RobotRuntimeException(parameterName + " must be an OBJECT");
        }

        return options.getValue();
    }

    private static void assertValue(MetaExpression expression, String error, Object... input) {
        if (expression == null || expression.isNull()) {
            throw new RobotRuntimeException(String.format(error, input));
        }
    }

    public static ContentType parseContentType(String value) {
        try {
            return ContentType.parse(value);
        } catch (UnsupportedCharsetException e) {
            throw new RobotRuntimeException("Charset " + e.getCharsetName() + " is not supported", e);
        } catch (ParseException e) {
            throw new RobotRuntimeException("Could not parse content type " + value + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("squid:S2095") // Sonar expects MetaExpressions to be closed, which is done by our GC mechanism
    private static MetaExpression getWorkstation(String workstation) {
        return MetaExpression.parseObject("myworkstation");
    }

    /**
     * Build an {@link nl.xillio.xill.plugins.xurl.data.Options} object from a MetaExpression.
     *
     * @param options the expression
     * @return the options
     */
    public Options build(MetaExpression options) {
        Map<String, MetaExpression> optionsMap = toMap(options, "options");

        Options result = new Options(defaultOptions);
        for (Map.Entry<String, MetaExpression> entry : optionsMap.entrySet()) {
            String key = entry.getKey();
            MetaExpression value = entry.getValue();

            // Apply this option if it exists, otherwise throw error
            Option option = OptionsEnum.ofLabel(Option.class, key)
                    .orElseThrow(() -> new RobotRuntimeException("Provided option [" + key + "] was not recognized."));
            option.apply(result, value);
        }
        return result;
    }

    public Options getDefaultOptions() {
        return defaultOptions;
    }

    public void setDefaultOptions(Options options) {
        this.defaultOptions = options;
    }

    public enum Option implements OptionsEnum {
        BASIC_AUTH {
            @Override
            void apply(Options options, MetaExpression value) {
                options.setBasicAuth(getCredentials(value, label()));
            }
        },
        PROXY {
            @Override
            void apply(Options options, MetaExpression value) {

                Map<String, MetaExpression> proxyObject = toMap(value, label());

                // First we get the HttpHost object
                MetaExpression host = proxyObject.get("host");
                assertValue(host, "The %s option must contain a 'host' field", label());

                MetaExpression port = proxyObject.get("port");

                HttpHost httpHost = getHttpHost(host, port);

                // Then we see if we need authentication
                Credentials credentials = tryGetCredentials(value, label());

                options.setProxyOptions(new ProxyOptions(httpHost, credentials));
            }

            private Credentials tryGetCredentials(MetaExpression object, String option) {
                Map<String, MetaExpression> map = toMap(object, option);
                if (map.containsKey("username")) {
                    return getCredentials(object, option);
                }
                return null;
            }

            private HttpHost getHttpHost(MetaExpression host, MetaExpression port) {
                if (port == null || port.isNull()) {
                    return new HttpHost(host.getStringValue());
                }
                return new HttpHost(host.getStringValue(), port.getNumberValue().intValue());
            }
        },
        TIMEOUT {
            @Override
            void apply(Options options, MetaExpression value) {
                options.setTimeout(getInt(value, label()));
            }
        },
        ENABLE_REDIRECT {
            @Override
            void apply(Options options, MetaExpression value) {
                options.setEnableRedirect(getBoolean(value, label()));
            }
        },
        HEADERS {
            @Override
            void apply(Options options, MetaExpression value) {
                assertValue(value, NO_NULL_MESSAGE, label());

                Map<String, MetaExpression> map = toMap(value, label());
                Header[] headers = map.entrySet()
                        .stream()
                        .map(e -> new BasicHeader(e.getKey(), e.getValue().getStringValue()))
                        .toArray(Header[]::new);
                options.setHeaders(headers);
            }
        },
        INSECURE {
            @Override
            void apply(Options options, MetaExpression value) {
                options.setInsecure(getBoolean(value, label()));
            }
        },
        MULTIPART {
            @Override
            void apply(Options options, MetaExpression value) {
                options.setMultipart(getBoolean(value, label()));
            }
        },
        RESPONSE_CONTENT_TYPE {
            @Override
            void apply(Options options, MetaExpression value) {
                assertValue(value, NO_NULL_MESSAGE, label());

                options.setResponseContentType(parseContentType(value.getStringValue()));
            }
        },
        LOGGING {
            @Override
            void apply(Options options, MetaExpression value) {
                options.setLogging(value.isNull() ? null : value.getStringValue());
            }
        },
        NTLM {
            @Override
            void apply(Options options, MetaExpression value) {
                assertValue(value, NO_NULL_MESSAGE, label());

                Map<String, MetaExpression> proxyObject = toMap(value, label());

                Credentials credentials = getCredentials(value, label());

                MetaExpression workstation = proxyObject.get("workstation");
                if (workstation == null || workstation.isNull()) {
                    workstation = getWorkstation("myworkstation");
                }

                MetaExpression domain = proxyObject.get("domain");
                assertValue(domain, "The %s option must contain a 'domain' field", label());

                options.setNTLMOptions(new NTLMOptions(credentials, workstation.getStringValue(), domain.getStringValue()));
            }
        },
        IGNORE_CONNECTION_CACHE {
            @Override
            void apply(Options options, MetaExpression value) {
                options.setIgnoreConnectionCache(getBoolean(value, label()));
            }
        };

        abstract void apply(Options options, MetaExpression value);
    }
}
