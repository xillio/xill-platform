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
package nl.xillio.xill.plugins.database.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.database.BaseDatabaseConstruct;
import nl.xillio.xill.plugins.database.services.DatabaseServiceFactory;
import nl.xillio.xill.plugins.database.util.Database;

/**
 * The connect construct for the SQLite database.
 */
public class SqliteConnectConstruct extends BaseDatabaseConstruct {

    @Override
    public ConstructProcessor doPrepareProcess(ConstructContext context) {
        // Default is in-memory database
        return new ConstructProcessor(file -> process(file, factory, context, context.getRootRobot()), new Argument("file", fromValue(":memory:"), ATOMIC));
    }

    static MetaExpression process(MetaExpression file, DatabaseServiceFactory factory, ConstructContext context, RobotID robotID) {
        MetaExpression[] newArgs = {file, fromValue(Database.SQLITE.getName()), NULL, NULL, emptyObject()};
        return ConnectConstruct.process(newArgs, factory, context, robotID);
    }
}
