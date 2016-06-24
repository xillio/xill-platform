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

import java.math.BigDecimal;


/**
 * The Codec implementation to parse a {@link BigDecimal} to a BSON object.
 * This class transforms a {@link BigDecimal} to a double by checking if they're within the range of a double.
 * If so, we return the BSON DOUBLE type, else the BSON STRING.
 * 
 * WARNING!!!! THIS CLASS IS DUPLICATED IN THE UDM PACKAGE
 * note that ANY changes in this class should be changed in the copies there too.
 *
 * @author Ivor
 */
public class BigDecimalCodecImpl implements Codec<BigDecimal> {

    @Override
    public void encode(BsonWriter writer, BigDecimal value, EncoderContext encoderContext) {
        if (value.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) < 0
                && value.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) > 0) {
            writer.writeDouble(value.doubleValue());
        } else {
            writer.writeString(value.toString());
        }
    }


    @Override
    public Class<BigDecimal> getEncoderClass() {
        return BigDecimal.class;
    }

    @Override
    public BigDecimal decode(BsonReader reader, DecoderContext decoderContext) {
        return BigDecimal.valueOf(reader.readDouble());
    }
}
