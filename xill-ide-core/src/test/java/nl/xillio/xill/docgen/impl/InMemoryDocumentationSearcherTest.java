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
package nl.xillio.xill.docgen.impl;

import nl.xillio.xill.docgen.DocumentationEntity;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Test the {@link InMemoryDocumentationSearcher}
 */
public class InMemoryDocumentationSearcherTest {
    @Test
    public void testSearch() throws Exception {
        InMemoryDocumentationSearcher searcher = spySearcher();

        // searchByName
        doReturn(new String[]{"Result A"}).when(searcher).searchByName(anyString());
        // searchByTag
        doReturn(new String[]{"Result B", "Result C"}).when(searcher).searchByTags(anyString());

        // Call the method
        String[] result = searcher.search("This is my query");

        // Verify calls
        verify(searcher).searchByName(anyString());
        verify(searcher).searchByTags(anyString());

        // Assertions
        assertEquals(result, new String[]{"Result A", "Result B", "Result C"});
    }

    @Test
    public void testSearchByTags() throws Exception {
        InMemoryDocumentationSearcher searcher = spySearcher();

        // Insert document
        String packet = "UnitTest";
        DocumentationEntity entity = mock(DocumentationEntity.class);
        when(entity.getTags()).thenReturn(Collections.singletonList("myTag"));
        when(entity.getIdentity()).thenReturn("construct");
        searcher.index(packet, entity);

        // Run the method
        String[] result = searcher.searchByTags("myTag");

        assertEquals(result, new String[]{"UnitTest.construct"});

    }

    @Test
    public void testSearchByName() throws Exception {
        InMemoryDocumentationSearcher searcher = spySearcher();

        // Insert document
        String packet = "UnitTest";
        DocumentationEntity entity = mock(DocumentationEntity.class);
        when(entity.getTags()).thenReturn(Collections.singletonList("myTag"));
        when(entity.getIdentity()).thenReturn("construct");
        searcher.index(packet, entity);

        // Run the method
        String[] result = searcher.searchByName("c");

        // Check result
        assertEquals(result, new String[]{"UnitTest.construct"});

        // Now run again without results
        // Run the method
        String[] result2 = searcher.searchByName("crt");

        // Check result
        assertEquals(result2, new String[]{});


    }

    private InMemoryDocumentationSearcher spySearcher() {
        return spy(new InMemoryDocumentationSearcher());
    }
}