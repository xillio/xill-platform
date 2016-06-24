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

import com.google.inject.ImplementedBy;

import java.io.UnsupportedEncodingException;

/**
 * Interface defining behavior of encoding constructs.
 *
 * @author Paul van der Zandt
 * @author Pieter Dirk Soels
 * @since 3.0
 */
@ImplementedBy(EncoderServiceImpl.class)
public interface EncoderService {

    /**
     * Converts a string into a string of characters representing the hexadecimal values of each byte in order.
     * The returned string will be double the length of the passed string, as it takes two characters to represent any
     * given byte.
     *
     * @param inputString a String to convert to Hex characters
     * @param toLowerCase <code>true</code> converts to lowercase, <code>false</code> to uppercase
     * @param charsetName the charset name. Default is UTF-8
     * @return A String containing hexadecimal characters
     */
    String toHex(String inputString, boolean toLowerCase, String charsetName);

    /**
     * Do URL encode of provided text
     *
     * @param text            input string
     * @param usePlusEncoding the output format (true means that spaces will be + otherwise %20)
     * @return urlencoded string
     * @throws UnsupportedEncodingException If something goes wrong during the encoding
     */
    String urlEncode(String text, boolean usePlusEncoding) throws UnsupportedEncodingException;

    /**
     * Escapes the XML.
     *
     * @param text The text that requires escaping
     * @return An escaped text
     */
    String escapeXML(String text);
}
