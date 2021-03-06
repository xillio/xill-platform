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
package nl.xillio.xill.api.preview;

import nl.xillio.xill.api.data.MetadataExpression;

/**
 * This interface represents an object that can provide a plain text preview.
 *
 * @author Thomas Biesaart
 */
public interface TextPreview extends MetadataExpression {

    /**
     * Maximum string size of the preview
     */
    int MAX_TEXT_PREVIEW_SIZE = 1000000;

    /**
     * The string placed at the end of the text preview that is used to indicate that the text preview is not complete in comparison with the full text
     */
    String NOT_COMPLETE_MARK = "\n...";

    /**
     * Renders a plain text preview of this object.
     *
     * @return the preview
     */
    String getTextPreview();
}
