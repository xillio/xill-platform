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

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The unit tests for the {@link ConfigurationFactory}.
 *
 * @author Pieter Soels
 */
public class ConfigurationFactoryTest extends TestUtils {

    @Test
    public void testNormalUsageDefaultConfig() {
        // Instantiate
        ConstructContext constructContext = context();
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        // Run
        Configuration configuration = configurationFactory.buildDefaultConfiguration(constructContext);

        // Assert
        Assert.assertEquals(configuration.getDefaultEncoding(), "UTF-8");
        Assert.assertEquals(configuration.getTemplateExceptionHandler(), TemplateExceptionHandler.RETHROW_HANDLER);
        Assert.assertFalse(configuration.getLogTemplateExceptions());
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testInvalidDirectory() {
        // Mock
        Path path = Paths.get("/disaodjhsaiudhsaidsahid").toAbsolutePath();

        // Instantiate
        ConfigurationFactory configurationFactory = new ConfigurationFactory();

        // Run
        configurationFactory.buildDefaultConfiguration(path);
    }

}