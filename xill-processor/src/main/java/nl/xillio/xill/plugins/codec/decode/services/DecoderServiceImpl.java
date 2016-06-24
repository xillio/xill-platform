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
package nl.xillio.xill.plugins.codec.decode.services;

import nl.xillio.xill.api.errors.NotImplementedException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of Decoding methods.
 *
 * @author Paul van der Zandt
 * @author Pieter Dirk Soels
 * @since 3.0
 */
public class DecoderServiceImpl implements DecoderService {

    @Override
    public String fromHex(String hexString, String charsetName) throws DecoderException {
        final Charset charset = charsetName == null ? StandardCharsets.UTF_8 : Charset.forName(charsetName);
        final Hex hex = new Hex(charset);
        return new String(hex.decode(hexString.getBytes()), charset);
    }

    @Override
    public String urlDecode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NotImplementedException("The UTF-8 charset is not supported. " +
                    "If this happens please contact the development team." + e.getMessage(), e);
        }
    }

    @Override
    public String unescapeXML(String text, final int passes) {
        String result = text;
        for (int i = 0; i < passes; i++) {
            result = StringEscapeUtils.unescapeXml(result);
        }
        return result;
    }
}
