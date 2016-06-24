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
package nl.xillio.xill.plugins.database.services;

import com.google.inject.Singleton;
import nl.xillio.xill.plugins.database.util.Database;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates services for specific DBMSs.
 *
 * @author Geert Konijnendijk
 */
@Singleton
public class DatabaseServiceFactory {

    // Map from name to service
    private Map<String, BaseDatabaseService> services = new HashMap<>();

    /**
     * Get a service given a DBMS name from {@link Database}
     *
     * @param name The name from a {@link Database} value
     * @return A {@link DatabaseService} suited for the given DBMS
     * @throws ReflectiveOperationException When the driver for the service can not be loaded
     */
    public DatabaseService getService(String name) throws ReflectiveOperationException, IllegalArgumentException {
        if (!services.containsKey(name)) {
            BaseDatabaseService service = Database.findServiceClass(name).newInstance();
            services.put(name, service);
            service.loadDriver();
        }
        return services.get(name);
    }
}
