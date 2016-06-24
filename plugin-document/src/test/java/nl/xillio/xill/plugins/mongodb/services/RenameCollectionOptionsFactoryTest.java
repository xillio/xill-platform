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

import com.mongodb.client.model.RenameCollectionOptions;
import nl.xillio.xill.TestUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RenameCollectionOptionsFactoryTest extends TestUtils {
    @Test
    public void testBuildOptions() {
        MongoConverter mongoConverter = new MongoConverter(null);
        RenameCollectionOptionsFactory RenameCollectionOptionsFactory = new RenameCollectionOptionsFactory(mongoConverter);

        RenameCollectionOptions options = RenameCollectionOptionsFactory.build(FALSE);
        assertFalse(options.isDropTarget());

        options = RenameCollectionOptionsFactory.build(TRUE);
        assertTrue(options.isDropTarget());
    }
}
