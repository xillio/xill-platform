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
package nl.xillio.xill.services.files;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;

import java.io.File;
import java.nio.file.Path;

/**
 * <p>
 * This interface describes a service that will resolve files from RobotIDs and paths.
 * </p>
 *
 * @author Thomas Biesaart
 * @since 5-8-2015
 */
@ImplementedBy(FileResolverImpl.class)
public interface FileResolver {
    /**
     * Resolves a file using the general file system rules.
     *
     * @param context the robot to resolve the file for
     * @param path    the path expression
     * @return the file
     */
    Path buildPath(ConstructContext context, MetaExpression path);

    @Deprecated
    File buildFile(ConstructContext context, String path);
}
