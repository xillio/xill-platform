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
package nl.xillio.xill.plugins.template.services;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

/**
 * The unit tests for the {@link ConfigurationParser}.
 *
 * @author Pieter Soels
 */
public class ConfigurationParserTest extends TestUtils {

    @Test
    public void testNormalUsageParseConfig() {
        // Instantiate
        ConfigurationParser configurationParser = new ConfigurationParser();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        MetaExpression encoding = fromValue("Something");
        MetaExpression softCache = fromValue(10);
        MetaExpression strongCache = fromValue(5);

        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("encoding", encoding);
        options.put("softCache", softCache);
        options.put("strongCache", strongCache);

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(context());
        Configuration result = configurationParser.parseConfiguration(configuration, options);

        // Assert
        Assert.assertEquals(result.getDefaultEncoding(), encoding.getStringValue());
        Assert.assertEquals(result.getTemplateExceptionHandler(), TemplateExceptionHandler.RETHROW_HANDLER);
        Assert.assertFalse(result.getLogTemplateExceptions());
        MruCacheStorage cache = (MruCacheStorage) result.getCacheStorage();
        Assert.assertEquals(cache.getSoftSizeLimit(), softCache.getNumberValue());
        Assert.assertEquals(cache.getStrongSizeLimit(), strongCache.getNumberValue());
    }

    @Test
    public void testNoCachingParseConfig() {
        // Instantiate
        ConfigurationParser configurationParser = new ConfigurationParser();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("noCaching", fromValue(true));

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(context());
        Configuration result = configurationParser.parseConfiguration(configuration, options);

        // Assert
        Assert.assertEquals(result.getDefaultEncoding(), "UTF-8");
        Assert.assertEquals(result.getTemplateExceptionHandler(), TemplateExceptionHandler.RETHROW_HANDLER);
        Assert.assertFalse(result.getLogTemplateExceptions());
        Assert.assertEquals(result.getCacheStorage().getClass(), NullCacheStorage.class);
    }

    @Test
    public void testSoftCacheChange() {
        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        ConfigurationParser configurationParser = new ConfigurationParser();

        MetaExpression softCache = fromValue(10);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("softCache", softCache);

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(context());
        Configuration result = configurationParser.parseConfiguration(configuration, options);

        // Assert
        MruCacheStorage cache = (MruCacheStorage) result.getCacheStorage();
        Assert.assertEquals(cache.getSoftSizeLimit(), softCache.getNumberValue());
        Assert.assertEquals(cache.getStrongSizeLimit(), 0);
    }

    @Test
    public void testStrongCacheChange() {
        // Instantiate
        ConfigurationParser configurationParser = new ConfigurationParser();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        MetaExpression strongCache = fromValue(5);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("strongCache", strongCache);

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(context());
        Configuration result = configurationParser.parseConfiguration(configuration, options);

        // Assert
        MruCacheStorage cache = (MruCacheStorage) result.getCacheStorage();
        Assert.assertEquals(cache.getSoftSizeLimit(), Integer.MAX_VALUE);
        Assert.assertEquals(cache.getStrongSizeLimit(), strongCache.getNumberValue());
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testInvalidSoftCache() {
        // Instantiate
        ConfigurationParser configurationParser = new ConfigurationParser();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        MetaExpression softCache = fromValue(-1);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("softCache", softCache);

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(context());
        configurationParser.parseConfiguration(configuration, options);
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testInvalidStrongCache() {
        // Instantiate
        ConfigurationParser configurationParser = new ConfigurationParser();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        MetaExpression strongCache = fromValue((long) Integer.MAX_VALUE + 1);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("strongCache", strongCache);

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(context());
        configurationParser.parseConfiguration(configuration, options);
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testNotANumberCache() {
        // Instantiate
        ConfigurationParser configurationParser = new ConfigurationParser();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        MetaExpression strongCache = fromValue("test");
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("strongCache", strongCache);

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(context());
        configurationParser.parseConfiguration(configuration, options);
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    void testTemplateException() throws TemplateException {
        Configuration configuration = mock(Configuration.class);
        doThrow(new TemplateException("Test", null)).when(configuration).setSetting(anyString(), anyString());

        ConfigurationParser configurationParser = new ConfigurationParser();
        configurationParser.parseConfiguration(configuration, new HashMap<>());
    }
}