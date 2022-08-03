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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * Factory that builds connections with a {@link MongoClient}.
 *
 * @author Thomas Biesaart
 * @author Titus Nachbauer
 */
public class ConnectionFactory {

    /**
     * Build a connection.
     *
     * @param info the connection info
     * @return the connection
     */
    public Connection build(ConnectionInfo info) {
        return new Connection(createClient(info), info.getDatabase());
    }

    private MongoClient createClient(ConnectionInfo info) {
        ServerAddress address = new ServerAddress(info.getHost(), info.getPort());

        // Create the Codecs to be able to handle BigIntegers and BigDecimals
        Codec<BigInteger> bigIntegerCodec = new BigIntegerCodecImpl();
        Codec<BigDecimal> bigDecimalCodec = new BigDecimalCodecImpl();
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(bigIntegerCodec, bigDecimalCodec));

        MongoClientOptions mongoOptions = MongoClientOptions.builder() // Add options
                .serverSelectionTimeout(3000)
                .codecRegistry(codecRegistry)
                .sslEnabled(info.isSslEnabled())
                .build();
        if (info.getUsername() == null) {
            return new MongoClient(address, mongoOptions);
        } else {
            MongoCredential credential = MongoCredential.createCredential(
                    info.getUsername(),
                    info.getDatabase(),
                    info.getPassword().toCharArray());
            return new MongoClient(address, credential, mongoOptions);
        }
    }
}
