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
package nl.xillio.xill.plugins.web.services.web;

import com.google.inject.ImplementedBy;

/**
 * Provides an interface for dealing with HTML and XHTML
 *
 * @author Geert Konijnendijk
 */
@ImplementedBy(HTMLServiceImpl.class)
public interface HTMLService {

    /**
     * Parse the input string as HMTL and tidy it.
     * This method guarantees a sensibly parsed document with at least a html and body tag.
     *
     * @param html        The HTML to parse and tidy
     * @param prettyPrint If true, the output HTML will be formatted nicely
     * @return Tidied HTML
     */
    String tidyHTML(String html, boolean prettyPrint);

    /**
     * Parse the input string as XHMTL and tidy it.
     * This method guarantees a sensibly parsed document with at least a html and body tag.
     *
     * @param html        The HTML to parse and tidy
     * @param prettyPrint If true, the output HTML will be formatted nicely
     * @return Tidied XHTML
     */
    String tidyXHTML(String html, boolean prettyPrint);

    /**
     * Parse the input string as a fragment from an HTML body.
     *
     * @param bodyFragment The HTML fragment to parse
     * @param prettyPrint  If true, the output HTML will be formatted nicely
     * @return The parsed and tidied fragment (without body tags) as HTML
     */
    String tidyHTMLBodyFragment(String bodyFragment, boolean prettyPrint);

    /**
     * Parse the input string as a fragment from an XHTML body.
     *
     * @param bodyFragment The HTML fragment to parse
     * @param prettyPrint  If true, the output HTML will be formatted nicely
     * @return The parsed and tidied fragment (without body tags) as XHTML
     */
    String tidyXHTMLBodyFragment(String bodyFragment, boolean prettyPrint);

}
