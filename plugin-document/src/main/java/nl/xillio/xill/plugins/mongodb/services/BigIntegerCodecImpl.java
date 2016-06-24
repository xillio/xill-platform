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
package nl.xillio.xill.plugins.mongodb.services;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.math.BigInteger;

/**
 * The Codec implementation to parse a {@link BigInteger} to BSON object.
 * This class transforms a {@link BigInteger} to a long by checking if they're within the range of a long.
 * If so, we return the BSON DOUBLE long, else the BSON STRING.
 *
 * WARNING!!!! THIS CLASS IS DUPLICATED IN THE UDM PACKAGE
 * note that ANY changes in this class should be changed in the copies there too.
 *
 * @author Ivor
 */
public class BigIntegerCodecImpl implements Codec<BigInteger> {

    @Override
    public void encode(BsonWriter writer, BigInteger value, EncoderContext encoderContext) {
        if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0
                && value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) > 0) {
            writer.writeInt32(value.intValue());
        } else if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0
                        && value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) > 0) {
            writer.writeInt64(value.longValue());
        }
        else {
            writer.writeString(value.toString()); 
        }
    }

    @Override
    public Class<BigInteger> getEncoderClass() {
        return BigInteger.class;
    }

    @Override
    public BigInteger decode(BsonReader reader, DecoderContext decoderContext) {
        return BigInteger.valueOf(reader.readInt64());
    }
}
