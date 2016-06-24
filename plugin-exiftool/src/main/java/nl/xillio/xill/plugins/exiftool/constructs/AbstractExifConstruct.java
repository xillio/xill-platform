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
package nl.xillio.xill.plugins.exiftool.constructs;

import com.google.inject.Inject;
import nl.xillio.exiftool.query.Projection;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.exiftool.services.ProjectionFactory;

/**
 * This class provides a base implementation for all constructs in this package.
 *
 * @author Thomas Biesaart
 */
public abstract class AbstractExifConstruct extends Construct {
    private ProjectionFactory projectionFactory;

    @Inject
    public void setProjectionFactory(ProjectionFactory projectionFactory) {
        this.projectionFactory = projectionFactory;
    }

    protected Projection getProjection(MetaExpression expression) {
        try {
            return projectionFactory.build(expression);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(expression + " is not a valid projection: " + e.getMessage(), e);
        }
    }
}
