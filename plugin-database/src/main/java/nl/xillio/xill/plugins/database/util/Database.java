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
package nl.xillio.xill.plugins.database.util;

import nl.xillio.xill.plugins.database.services.*;

import java.util.Arrays;
import java.util.Optional;

/**
 * The database enum, used for identifying database types.
 */
public enum Database {
    /**
     * The enum for the Oracle database.
     */
    ORACLE("oracle", "oracle.jdbc.OracleDriver", OracleDatabaseServiceImpl.class),
    /**
     * The enum for the MSsql database.
     */
    MSSQL("mssql", "net.sourceforge.jtds.jdbc.Driver", MssqlDatabaseServiceImpl.class),
    /**
     * The enum for the SQLite database.
     */
    SQLITE("sqlite", "org.sqlite.JDBC", SQLiteDatabaseServiceImpl.class),
    /**
     * The enum for the Mysql database.
     */
    MYSQL("mysql", "org.mariadb.jdbc.Driver", MysqlDatabaseServiceImpl.class);

    private String name;
    private String driverClass;
    private Class<? extends BaseDatabaseService> serviceClass;

    Database(String name, String driverClass, Class<? extends BaseDatabaseService> serviceClass) {
        this.name = name;
        this.driverClass = driverClass;
        this.serviceClass = serviceClass;
    }


    /**
     * @return Returns the name of the database.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the driver class of the database.
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * @return Returns the service class of the database.
     */
    public Class<? extends BaseDatabaseService> getServiceClass() {
        return serviceClass;
    }

    /**
     * @param name The name of the {@link Database}
     * @return The service belonging to the DBMS with the given name
     */
    public static Class<? extends BaseDatabaseService> findServiceClass(String name) {
        Optional<Database> db = Arrays.stream(values()).filter(d -> d.getName().equals(name)).findAny();
        if (!db.isPresent())
            throw new IllegalArgumentException("DBMS type is not supported");
        return db.get().getServiceClass();
    }
}
