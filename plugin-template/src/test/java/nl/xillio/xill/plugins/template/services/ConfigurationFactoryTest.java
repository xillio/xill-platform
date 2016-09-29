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
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.services.files.FileResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.HashMap;
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
        ConstructContext constructContext = mockConstructContext();

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
        ConstructContext constructContext = mockConstructContext();

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        MetaExpression encoding = fromValue("Something");
        MetaExpression softCache = fromValue(10);
        MetaExpression strongCache = fromValue(5);

        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("encoding", encoding);
        options.put("softCache", softCache);
        options.put("strongCache", strongCache);

        // Run
        Configuration configuration = configurationFactory.parseConfiguration(options, constructContext);

        // Assert
        Assert.assertEquals(configuration.getDefaultEncoding(), encoding.getStringValue());
        Assert.assertEquals(configuration.getTemplateExceptionHandler(), TemplateExceptionHandler.RETHROW_HANDLER);
        Assert.assertFalse(configuration.getLogTemplateExceptions());
        MruCacheStorage cache = (MruCacheStorage) configuration.getCacheStorage();
        Assert.assertEquals(cache.getSoftSizeLimit(), softCache.getNumberValue());
        Assert.assertEquals(cache.getStrongSizeLimit(), strongCache.getNumberValue());
    }

    @Test
    public void testNoCachingParseConfig() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mockConstructContext();
        LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<>();
        map.put("noCaching", fromValue(true));

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
        ConstructContext constructContext = mockConstructContext();

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

    @Test
    public void testSoftCacheChange() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mockConstructContext();

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        MetaExpression softCache = fromValue(10);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("softCache", softCache);

        // Run
        Configuration configuration = configurationFactory.parseConfiguration(options, constructContext);

        // Assert
        MruCacheStorage cache = (MruCacheStorage) configuration.getCacheStorage();
        Assert.assertEquals(cache.getSoftSizeLimit(), softCache.getNumberValue());
        Assert.assertEquals(cache.getStrongSizeLimit(), 0);
    }

    @Test
    public void testStrongCacheChange() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mockConstructContext();

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        MetaExpression strongCache = fromValue(5);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("strongCache", strongCache);

        // Run
        Configuration configuration = configurationFactory.parseConfiguration(options, constructContext);

        // Assert
        MruCacheStorage cache = (MruCacheStorage) configuration.getCacheStorage();
        Assert.assertEquals(cache.getSoftSizeLimit(), Integer.MAX_VALUE);
        Assert.assertEquals(cache.getStrongSizeLimit(), strongCache.getNumberValue());
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testInvalidDirectory() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mockConstructContext();
        when(fileResolver.buildPath(constructContext, fromValue("."))).thenReturn(Paths.get("/disaodjhsaiudhsaidsahid").toAbsolutePath());

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        MetaExpression templatesDirectory = fromValue(".");
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("templatesDirectory", templatesDirectory);

        // Run
        configurationFactory.parseConfiguration(options, constructContext);

        // Verify
        verify(fileResolver.buildPath(any(), any()), times(1));
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testInvalidSoftCache() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mockConstructContext();

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        MetaExpression softCache = fromValue(-1);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("softCache", softCache);

        // Run
        configurationFactory.parseConfiguration(options, constructContext);
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testInvalidStrongCache() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mockConstructContext();

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        MetaExpression strongCache = fromValue((long) Integer.MAX_VALUE + 1);
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("strongCache", strongCache);

        // Run
        configurationFactory.parseConfiguration(options, constructContext);
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testNotANumberCache() {
        // Mock
        FileResolver fileResolver = mock(FileResolver.class);
        ConstructContext constructContext = mockConstructContext();

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory(fileResolver);

        MetaExpression strongCache = fromValue("test");
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();
        options.put("strongCache", strongCache);

        // Run
        configurationFactory.parseConfiguration(options, constructContext);
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    void testTemplateException() throws TemplateException {
        Configuration myMock = mock(Configuration.class);
        doThrow(new TemplateException("Test", null)).when(myMock).setSetting(anyString(), anyString());

        ConfigurationFactory factory = spy(new ConfigurationFactory(null));
        doReturn(myMock).when(factory).buildDefaultConfiguration(any());

        factory.parseConfiguration(new HashMap<>(), context());
    }

    private ConstructContext mockConstructContext() {
        ConstructContext constructContext = mock(ConstructContext.class, RETURNS_DEEP_STUBS);
        when(constructContext.getRootRobot().getProjectPath().toPath()).thenReturn(Paths.get("/").toAbsolutePath());
        return constructContext;
    }
}