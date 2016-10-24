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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.services.files.TextFileReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class GetTextConstructTest extends TestUtils {
    @BeforeClass
    private void preTest() {
        setFileResolverReturnValue(Paths.get(""));
    }

    /**
     * Test the construct under normal circumstances.
     */
    @Test
    public void testNormalUsageFromFile() throws IOException {
        String text = "foo bar";

        // Mock.
        TextFileReader textFileReader = mock(TextFileReader.class);
        GetTextConstruct construct = new GetTextConstruct(textFileReader);
        when(textFileReader.getText(any(), any())).thenReturn(text);

        // Run.
        MetaExpression result = this.process(construct, fromValue(""));

        // Assert.
        assertEquals(result.getStringValue(), text);
    }

    /**
     * Test the construct with a non-existent file.
     */
    @Test(expectedExceptions = OperationFailedException.class, expectedExceptionsMessageRegExp = ".*get text from the file.*")
    public void testFromFileNotExists() {
        // Mock.
        TextFileReader textFileReader = mock(TextFileReader.class);
        GetTextConstruct construct = new GetTextConstruct(textFileReader);
        when(textFileReader.getText(any(), any())).thenThrow(new OperationFailedException("get text from the file", "File does not exist."));

        // Run.
        this.process(construct, fromValue(""));
    }
}