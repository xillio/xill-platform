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
package nl.xillio.xill.plugins.web.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.services.web.HTMLService;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for {@link TidyConstruct}.
 *
 * @author Geert Konijnendijk
 */
public class TidyConstructTest extends TestUtils {

    public static final String INPUT_HTML = "<p>test", TIDIED_HTML = "<p>test</p>";

    public static final MetaExpression DEFAULT_HTML = mockExpression(ATOMIC, false, Double.NaN, INPUT_HTML),
            TRUE = mockExpression(ATOMIC, true, Double.NaN, "true"),
            SYNTAX_XHTML = mockExpression(ATOMIC, false, Double.NaN, "xhtml"),
            SYNTAX_HTML = mockExpression(ATOMIC, false, Double.NaN, "html");

    /**
     * Test {@link TidyConstruct to output a full HTML document
     */
    @Test
    public void testProcessFullHTML() {
        // mock
        TidyConstruct construct = new TidyConstruct();
        HTMLService service = mock(HTMLService.class);
        construct.setHtmlService(service);
        when(service.tidyHTML(any(), anyBoolean())).thenReturn(TIDIED_HTML);

        // run
        MetaExpression result = process(construct,DEFAULT_HTML, TRUE, SYNTAX_HTML, TRUE);

        // verify
        verify(service).tidyHTML(INPUT_HTML, true);

        // assert
        assertSame(result.getStringValue(), TIDIED_HTML);
    }

    /**
     * Test {@link TidyConstruct to output a full XHTML document
     */
    @Test
    public void testProcessFullXHTML() {
        // mock
        TidyConstruct construct = new TidyConstruct();
        HTMLService service = mock(HTMLService.class);
        construct.setHtmlService(service);
        when(service.tidyXHTML(any(), anyBoolean())).thenReturn(TIDIED_HTML);


        // run
        MetaExpression result = process(construct,DEFAULT_HTML, TRUE, SYNTAX_XHTML, TRUE);

        // verify
        verify(service).tidyXHTML(INPUT_HTML, true);

        // assert
        assertSame(result.getStringValue(), TIDIED_HTML);
    }

    /**
     * Test {@link TidyConstruct to output a fragment HTML document
     */
    @Test
    public void testProcessFragmentHTML() {
        // mock
        TidyConstruct construct = new TidyConstruct();
        HTMLService service = mock(HTMLService.class);
        construct.setHtmlService(service);
        when(service.tidyHTMLBodyFragment(any(), anyBoolean())).thenReturn(TIDIED_HTML);

        // run
        MetaExpression result = process(construct,DEFAULT_HTML, FALSE, SYNTAX_HTML, TRUE);

        // verify
        verify(service).tidyHTMLBodyFragment(INPUT_HTML, true);

        // assert
        assertSame(result.getStringValue(), TIDIED_HTML);
    }

    /**
     * Test {@link TidyConstruct to output a fragment XHTML document
     */
    @Test
    public void testProcessFragmentXHTML() {
        // mock
        TidyConstruct construct = new TidyConstruct();
        HTMLService service = mock(HTMLService.class);
        construct.setHtmlService(service);
        when(service.tidyXHTMLBodyFragment(any(), anyBoolean())).thenReturn(TIDIED_HTML);

        // run
        MetaExpression result = process(construct,DEFAULT_HTML, FALSE, SYNTAX_XHTML, TRUE);

        // verify
        verify(service).tidyXHTMLBodyFragment(INPUT_HTML, true);

        // assert
        assertSame(result.getStringValue(), TIDIED_HTML);
    }

    /**
     * Test {@link TidyConstruct with an incorrect syntax parameter.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessIncorrectSyntax() {
        // mock
        TidyConstruct construct = new TidyConstruct();
        HTMLService service = mock(HTMLService.class);
        construct.setHtmlService(service);
        MetaExpression syntax = mockExpression(ATOMIC, false, Double.NaN, "wrong");
        // run
        process(construct,DEFAULT_HTML, TRUE, syntax, TRUE);
    }
}
