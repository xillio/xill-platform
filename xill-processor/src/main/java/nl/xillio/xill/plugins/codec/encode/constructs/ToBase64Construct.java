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
package nl.xillio.xill.plugins.codec.encode.constructs;

import nl.xillio.xill.plugins.codec.constructs.AbstractEncodingConstruct;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This construct will encode streams or strings to base64.
 *
 * @author Thomas Biesaart
 * @see AbstractEncodingConstruct
 */
public class ToBase64Construct extends AbstractEncodingConstruct {
    @Override
    protected InputStream encode(BufferedInputStream inputStream) {
        return new Base64InputStream(inputStream, true);
    }

    @Override
    protected OutputStream encode(OutputStream outputStream) {
        return new Base64OutputStream(outputStream, true);
    }
}
