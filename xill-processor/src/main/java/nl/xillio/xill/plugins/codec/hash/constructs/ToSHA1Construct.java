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
package nl.xillio.xill.plugins.codec.hash.constructs;

import nl.xillio.xill.plugins.codec.constructs.AbstractDigestConstruct;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This construct will consume an input stream or string and get its SHA-1 hash.
 * It will optionally forward the input data to an output stream.
 *
 * @author Thomas Biesaart
 */
public class ToSHA1Construct extends AbstractDigestConstruct {

    @Override
    protected MessageDigest getDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-1");
    }
}
