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
import com.mongodb.client.MongoDatabase;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.data.MetadataExpression;
import org.slf4j.Logger;

/**
 * Represents a connection with a {@link MongoClient}.
 *
 * @author Thomas Biesaart
 * @author Titus Nachbauer
 */
public class Connection implements MetadataExpression {
    private static final Logger LOGGER = Log.get();
    private final MongoClient client;
    private final String database;
    private boolean closed;

    /**
     * Create a connection.
     *
     * @param client   the client to wrap
     * @param database the database to connect to
     */
    public Connection(MongoClient client, String database) {
        this.client = client;
        this.database = database;
    }

    /**
     * Close the connection.
     */
    public void close() {
        if (closed) {
            return;
        }
        LOGGER.info("Closing connection");
        client.close();
        closed = true;
    }

    /**
     * Check whether connection is closed.
     *
     * @return true if and only if {@link Connection#close()} has been called
     */
    public boolean isClosed() {
        return closed;
    }

    public void requireValid() {
        client.getAddress();
    }

    public MongoDatabase getDatabase() {
        return client.getDatabase(database);
    }
}
