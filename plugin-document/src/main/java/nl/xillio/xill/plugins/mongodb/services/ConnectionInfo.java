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

import nl.xillio.xill.plugins.document.util.Require;

import java.util.Objects;

/**
 * This class represents the information required to create a MongoDB connection.
 *
 * @author Thomas Biesaart
 * @author Titus Nachbauer
 */
public class ConnectionInfo {
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 27017;
    public static final String UDM_DATABASE_NAME_ROOT = "udm_";
    public static final String UDM_DEFAULT_IDENTITY = "default";

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String identity;


    public ConnectionInfo() {
        this(UDM_DEFAULT_IDENTITY);
    }

    /**
     * Configures the {@link ConnectionManager} to use the UDM identity to create a connection to Mongo,
     * as opposed to specifying {@code host}, {@code port}, {@code database} and credentials.
     * @param identity the identity identifier of the target database on the same machine and without authentication. Corresponds to the udm_identity database
     */
    public ConnectionInfo(String identity) {
        Require.notEmpty(identity);

        this.host = DEFAULT_HOST;
        this.port = getDefaultPort();
        this.database = composeDbNameFromIdentity(identity);
        this.username = null;
        this.password = null;
        this.identity = identity;
    }

    public static int getDefaultPort() {
        String sysPort = System.getProperty("udm.port");

        if (sysPort == null) {
            return DEFAULT_PORT;
        }

        return Integer.parseInt(sysPort);
    }

    public ConnectionInfo(String host, int port, String database) {
        this(host, port, database, null, null);
    }

    public ConnectionInfo(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.identity = "";
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getDatabase() {
        return database;
    }

    public String getPassword() {
        return password;
    }

    public String getIdentity() {
        return identity;
    }

    private static String composeDbNameFromIdentity(String identity) {
        return UDM_DATABASE_NAME_ROOT + identity;
    }

    @Override
    public String toString() {
        if (username == null) {
            return String.format("Mongo[%s:%d/%s]", host, port, database);
        }
        return String.format("Mongo[%s@%s:%d/%s]", username, host, port, database);
    }

    @Override
    @SuppressWarnings("squid:S1067") // cannot efficiently reduce number of conditional operators
    public boolean equals(Object obj) {
        if (!(obj instanceof ConnectionInfo)) {
            return false;
        }

        ConnectionInfo other = (ConnectionInfo) obj;
        return (identity == null && other.identity == null || other.identity != null && other.identity.equals(identity)) &&
                other.host.equals(host) &&
                other.port == port &&
                other.database.equals(database) &&
                (username == null && other.username == null || other.username.equals(username)) &&
                (password == null && other.password == null || other.password.equals(password));
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, database, username, password, identity);
    }
}
