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
package nl.xillio.xill.plugins.codec.encode.services;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of encoding methods.
 *
 * @author Paul van der Zandt
 * @author Pieter Dirk Soels
 * @since 3.0
 */
public class EncoderServiceImpl implements EncoderService {

    @Override
    public String toHex(String inputString, boolean toLowerCase, String charsetName) {
        final Charset charset = charsetName == null ? StandardCharsets.UTF_8 : Charset.forName(charsetName);
        return new String(Hex.encodeHex(inputString.getBytes(charset), toLowerCase));
    }

    @Override
    public String urlEncode(final String text, final boolean xWwwForm) throws UnsupportedEncodingException {
        String encText = URLEncoder.encode(text, "UTF-8");
        return xWwwForm ? encText : encText.replace("+", "%20");
    }

    @Override
    public String escapeXML(final String text) {
        return StringEscapeUtils.escapeXml11(text);
    }

}
