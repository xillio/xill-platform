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
package nl.xillio.xill.plugins.xml.constructs;

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.plugins.file.services.files.SimpleTextFileReader;
import nl.xillio.xill.plugins.xml.services.NodeService;
import nl.xillio.xill.plugins.xml.services.NodeServiceImpl;
import nl.xillio.xill.services.files.TextFileReader;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the {@link FromFileConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class FromFileConstructTest extends TestUtils {
    private NodeService nodeService = spy(new NodeServiceImpl());
    private TextFileReader textFileReader = spy(new SimpleTextFileReader());
    private FromFileConstruct construct = new FromFileConstruct(nodeService, textFileReader);

    /**
     * Test the construct under normal circumstances.
     */
    @Test
    public void testProcess() throws IOException {
        // Create test file.
        Path file = Files.createTempFile(getClass().getSimpleName(), ".xml");
        String source = "<parent><child>inner</child></parent>";
        Files.copy(IOUtils.toInputStream(source), file, StandardCopyOption.REPLACE_EXISTING);
        setFileResolverReturnValue(file);

        // Run.
        MetaExpression result = this.process(construct, fromValue(file.toAbsolutePath().toString()));

        // Verify.
        verify(nodeService).fromString(source);

        // Assert.
        assertTrue(result.hasMeta(XmlNode.class));

        // Delete test file.
        Files.delete(file);
    }
}