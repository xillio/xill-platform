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
package nl.xillio.xill.plugins.system.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.ResourceLoader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;

public class OpenResourceConstructTest extends TestUtils {
    private static final String path = "path/to/file";
    private static final String errorPath = "ThrowErrors";
    private ConstructContext context;
    private BufferedInputStream stream;

    @BeforeClass
    void beforeClass() throws Exception {
        context = mock(ConstructContext.class);
        ResourceLoader loader = mock(ResourceLoader.class);
        when(context.getResourceLoader()).thenReturn(loader);
        stream = mock(BufferedInputStream.class);
        when(loader.getResourceAsStream(path)).thenReturn(stream);
        when(loader.getResourceAsStream(errorPath)).thenThrow(new IOException(""));
    }

    @Test
    void testResource() throws Exception {

        MetaExpression result = OpenResourceConstruct.process(context, fromValue(path));

        assertSame(result.getBinaryValue().getInputStream(), stream);
    }

    @Test
    void testResourceNotExist() {

        MetaExpression result = OpenResourceConstruct.process(context, fromValue("wrongPath"));

        Assert.assertEquals(result, NULL);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    void testErrorHandling() {
        OpenResourceConstruct.process(context, fromValue(errorPath));
    }

}
