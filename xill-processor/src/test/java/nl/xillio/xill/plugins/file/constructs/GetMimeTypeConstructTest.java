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
package nl.xillio.xill.plugins.file.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.services.resourceLibraries.ContentTypeLibrary;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class GetMimeTypeConstructTest extends TestUtils {

    @Test
    public void testExistingMimeType() throws Exception {
        GetMimeTypeConstruct getMimeTypeConstruct = new GetMimeTypeConstruct(new ContentTypeLibrary());
        MetaExpression actual = process(getMimeTypeConstruct, fromValue("test.txt"));
        assertEquals(actual.getStringValue(), "text/plain");
    }

    @Test
    public void testExistingMimeTypeWithCompletePath() throws Exception {
        GetMimeTypeConstruct getMimeTypeConstruct = new GetMimeTypeConstruct(new ContentTypeLibrary());
        MetaExpression actual = process(getMimeTypeConstruct, fromValue("/opt/var/log/test.txt"));
        assertEquals(actual.getStringValue(), "text/plain");
    }

    @Test
    public void testNonExistingMimeType() throws Exception {
        GetMimeTypeConstruct getMimeTypeConstruct = new GetMimeTypeConstruct(new ContentTypeLibrary());
        MetaExpression actual = process(getMimeTypeConstruct, fromValue("test.doesnotexist"));
        assertEquals(actual, NULL);
    }

    @Test
    public void testNoExtension() throws Exception {
        GetMimeTypeConstruct getMimeTypeConstruct = new GetMimeTypeConstruct(new ContentTypeLibrary());
        MetaExpression actual = process(getMimeTypeConstruct, fromValue("test"));
        assertEquals(actual, NULL);
    }

    @Test
    public void testNoExtensionWithDotInPath() throws Exception {
        GetMimeTypeConstruct getMimeTypeConstruct = new GetMimeTypeConstruct(new ContentTypeLibrary());
        MetaExpression actual = process(getMimeTypeConstruct, fromValue("test.test/txt"));
        assertEquals(actual, NULL);
    }

    @Test
    public void testNull() throws Exception {
        GetMimeTypeConstruct getMimeTypeConstruct = new GetMimeTypeConstruct(new ContentTypeLibrary());
        MetaExpression actual = process(getMimeTypeConstruct, NULL);
        assertEquals(actual, NULL);
    }

}