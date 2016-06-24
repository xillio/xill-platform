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
package nl.xillio.xill.plugins.exiftool.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.biesaart.utils.Log;
import nl.xillio.exiftool.*;
import nl.xillio.exiftool.query.*;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.slf4j.Logger;

import java.util.Map;

/**
 * This class is responsible for converting MetaExpression to QueryOptions.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class OptionsFactory {
    private static final Logger LOGGER = Log.get();
    private final ProjectionFactory projectionFactory;

    @Inject
    public OptionsFactory(ProjectionFactory projectionFactory) {
        this.projectionFactory = projectionFactory;
    }

    @SuppressWarnings("unchecked")
    public FolderQueryOptions buildFolderOptions(MetaExpression options) {
        if (options.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("Options must be of type OBJECT");
        }

        FolderQueryOptions folderQueryOptions = new FolderQueryOptionsImpl();
        Map<String, MetaExpression> map = options.getValue();

        map.forEach((key, value) -> processFolder(folderQueryOptions, key.toLowerCase(), value));

        return folderQueryOptions;
    }

    @SuppressWarnings("unchecked")
    public FileQueryOptions buildFileOptions(MetaExpression options) {
        if (options.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("Options must be of type OBJECT");
        }

        FileQueryOptions folderQueryOptions = new FileQueryOptionsImpl();
        Map<String, MetaExpression> map = options.getValue();

        map.forEach((key, value) -> process(folderQueryOptions, key.toLowerCase(), value));

        return folderQueryOptions;
    }

    private void processFolder(FolderQueryOptions folderQueryOptions, String option, MetaExpression value) {
        switch (option) {
            case "recursive":
                folderQueryOptions.setRecursive(value.getBooleanValue());
                break;
            case "extensions":
                folderQueryOptions.setExtensionFilter(getProjection(value));
                break;
            default:
                process(folderQueryOptions, option, value);
        }
    }

    @SuppressWarnings("squid:S1301") // Implemented like this for future expansion
    private void process(QueryOptions queryOptions, String option, MetaExpression value) {
        switch (option) {
            case "nameconvention":
                queryOptions.setTagNameConvention(getConvention(value.getStringValue()));
                break;
            default:
                LOGGER.warn("Unknown option [" + option + "]");
        }
    }

    private TagNameConvention getConvention(String tagName) {
        switch (tagName) {
            case "capitalword":
            case "cw":
                return new CapitalWordNameConvention();
            case "uppercamelcase":
            case "ucc":
                return new UpperCamelCaseNameConvention();
            case "lowercamelcase":
            case "lcc":
            default:
                return new LowerCamelCaseNameConvention();
        }
    }

    private Projection getProjection(MetaExpression value) {
        try {
            return projectionFactory.build(value);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException("Invalid extension projection: " + e.getMessage(), e);
        }
    }
}
