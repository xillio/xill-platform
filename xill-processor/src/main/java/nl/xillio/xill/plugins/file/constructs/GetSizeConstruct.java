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
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.file.services.FileSizeCalculator;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This construct returns the size of a file or throws an error when the file was not found.
 *
 * @author Thomas biesaart
 */
@Singleton
public class GetSizeConstruct extends AbstractFilePropertyConstruct<Long> {
    private final FileSizeCalculator fileSizeCalculator;

    @Inject
    GetSizeConstruct(FileSizeCalculator fileSizeCalculator) {
        this.fileSizeCalculator = fileSizeCalculator;
    }

    @Override
    protected Long process(Path path) throws IOException {
        return fileSizeCalculator.getSize(path);
    }

    @Override
    protected MetaExpression parse(Long input) {
        return fromValue(input);
    }
}
