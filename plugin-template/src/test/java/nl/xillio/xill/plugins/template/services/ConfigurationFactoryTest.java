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
import freemarker.template.TemplateExceptionHandler;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.services.files.FileResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

/**
 * The unit tests for the {@link ConfigurationFactory}.
 *
 * @author Pieter Soels
 */
public class ConfigurationFactoryTest extends TestUtils {

    @Test
    public void testNormalUsageDefaultConfig() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mock(ConstructContext.class, RETURNS_DEEP_STUBS);
        Path templatesDir = Paths.get("/").toAbsolutePath();
        when(constructContext.getRootRobot().getProjectPath().toPath()).thenReturn(templatesDir);

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(constructContext);

        // Assert
        Assert.assertEquals(configuration.getDefaultEncoding(), "UTF-8");
        Assert.assertEquals(configuration.getTemplateExceptionHandler(), TemplateExceptionHandler.RETHROW_HANDLER);
        Assert.assertFalse(configuration.getLogTemplateExceptions());
    }

    @Test
    public void testNormalUsageParseConfig() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mock(ConstructContext.class, RETURNS_DEEP_STUBS);
        Path templatesDir = Paths.get("/").toAbsolutePath();
        when(constructContext.getRootRobot().getProjectPath().toPath()).thenReturn(templatesDir);
        MetaExpression encoding = fromValue("Something");
        MetaExpression softCache = fromValue(10);
        MetaExpression strongCache = fromValue(5);
        LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<>();
        map.put("encoding", encoding);
        map.put("softCache", softCache);
        map.put("strongCache", strongCache);

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        // Run
        Configuration configuration = configurationFactory.parseConfiguration(map, constructContext);

        // Assert
        Assert.assertEquals(configuration.getDefaultEncoding(), "Something");
        Assert.assertEquals(configuration.getTemplateExceptionHandler(), TemplateExceptionHandler.RETHROW_HANDLER);
        Assert.assertFalse(configuration.getLogTemplateExceptions());
        MruCacheStorage cache = (MruCacheStorage) configuration.getCacheStorage();
        Assert.assertEquals(cache.getSoftSizeLimit(), 10);
        Assert.assertEquals(cache.getStrongSizeLimit(), 5);
    }

    @Test
    public void testNoCachingParseConfig() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mock(ConstructContext.class, RETURNS_DEEP_STUBS);
        Path templatesDir = Paths.get("/").toAbsolutePath();
        when(constructContext.getRootRobot().getProjectPath().toPath()).thenReturn(templatesDir);
        MetaExpression noCaching = fromValue(true);
        LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<>();
        map.put("noCaching", noCaching);

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        // Run
        Configuration configuration = configurationFactory.parseConfiguration(map, constructContext);

        // Assert
        Assert.assertEquals(configuration.getDefaultEncoding(), "UTF-8");
        Assert.assertEquals(configuration.getTemplateExceptionHandler(), TemplateExceptionHandler.RETHROW_HANDLER);
        Assert.assertFalse(configuration.getLogTemplateExceptions());
        Assert.assertEquals(configuration.getCacheStorage().getClass(), NullCacheStorage.class);
    }

    @Test
    public void testParseConfigWithoutOptions() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mock(ConstructContext.class, RETURNS_DEEP_STUBS);
        Path templatesDir = Paths.get("/").toAbsolutePath();
        when(constructContext.getRootRobot().getProjectPath().toPath()).thenReturn(templatesDir);

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        // Run
        Configuration configuration1 = configurationFactory.parseConfiguration(null, constructContext);
        Configuration configuration2 = configurationFactory.buildDefaultConfiguration(constructContext);

        // Assert
        Assert.assertEquals(configuration1.getDefaultEncoding(), configuration2.getDefaultEncoding());
        Assert.assertEquals(configuration1.getTemplateExceptionHandler(), configuration2.getTemplateExceptionHandler());
        Assert.assertFalse(configuration1.getLogTemplateExceptions());
        Assert.assertFalse(configuration2.getLogTemplateExceptions());
        Assert.assertEquals(configuration1.getCacheStorage().getClass(), configuration2.getCacheStorage().getClass());
    }
}