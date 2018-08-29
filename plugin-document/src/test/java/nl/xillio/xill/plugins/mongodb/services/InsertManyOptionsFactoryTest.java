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

import com.google.inject.Guice;
import com.mongodb.client.model.InsertManyOptions;
import nl.xillio.xill.TestUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class InsertManyOptionsFactoryTest extends TestUtils {
    @Test
    public void testBuildOptions() {
        MongoConverter mongoConverter = Guice.createInjector().getInstance(MongoConverter.class);
        InsertManyOptionsFactory insertManyOptionsFactory = new InsertManyOptionsFactory(mongoConverter);

        InsertManyOptions options = insertManyOptionsFactory.build(TRUE);

        assertTrue(options.isOrdered());
        options = insertManyOptionsFactory.build(FALSE);
        assertFalse(options.isOrdered());
    }
}
