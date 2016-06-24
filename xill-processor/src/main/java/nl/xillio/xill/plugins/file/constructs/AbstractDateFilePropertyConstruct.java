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
package nl.xillio.xill.plugins.file.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.data.DateFactory;
import nl.xillio.xill.api.errors.OperationFailedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * This class represents the base for all property constructs that extract {@link FileTime}.
 *
 * @author Thomas Biesaart
 */
abstract class AbstractDateFilePropertyConstruct extends AbstractFilePropertyConstruct<FileTime> {

    private DateFactory dateFactory;

    @Inject
    void setDateFactory(DateFactory dateFactory) {
        this.dateFactory = dateFactory;
    }

    @Override
    protected MetaExpression parse(FileTime time) {
        Date date = dateFactory.from(time.toInstant());

        MetaExpression result = fromValue(date.toString());
        result.storeMeta(date);
        return result;
    }

    protected BasicFileAttributes attributes(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new OperationFailedException("read attributes from " + path, e.getMessage(), e);
        }
    }
}
