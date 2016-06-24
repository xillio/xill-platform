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
package nl.xillio.xill.plugins.system.services.info;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.services.XillService;

/**
 * This interface represents a service that provides system information
 */
@ImplementedBy(SystemInfoServiceImpl.class)
public interface SystemInfoService extends XillService {
    /**
     * Get properties related to the current filesystem and the available storage
     *
     * @return the {@link FileSystemInfo}
     */
    public FileSystemInfo getFileSystemInfo();

    /**
     * Get information related to the current {@link Runtime}
     *
     * @return the {@link RuntimeInfo}
     */
    public RuntimeInfo getRuntimeInfo();

    /**
     * Get information related to the current {@link RobotRuntimeInfo}
     *
     * @param context the context
     * @return the {@link RobotRuntimeInfo}
     */
    public RobotRuntimeInfo getRobotRuntimeInfo(ConstructContext context);
}
