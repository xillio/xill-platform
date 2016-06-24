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
package nl.xillio.xill.api.construct;

import com.google.inject.Inject;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.services.files.FileResolver;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

/**
 * This interface contains the core functionality for all constructs.
 */
public abstract class Construct extends ExpressionBuilderHelper implements HelpComponent {
    private final String defaultName;

    @Inject
    private static FileResolver fileResolver;

    /**
     * Instantiates a new construct and picks a default name.
     */
    public Construct() {
        // Set the default name
        String name = getClass().getSimpleName();
        String superName = Construct.class.getSimpleName();
        if (name.endsWith(superName)) {
            name = name.substring(0, name.length() - superName.length());
        }
        defaultName = WordUtils.uncapitalize(name);
    }

    /**
     * <p>
     * Returns the name of the construct. This name is also the command by which this construct can be called inside scripts.
     * </p>
     * <p>
     * By default the name of a {@link XillPlugin} is the concrete implementation name acquired using {@link Class#getSimpleName()},
     * without the {@link XillPlugin} suffix. It is also uncapitalized using {@link WordUtils#uncapitalize(String)}.
     * </p>
     *
     * @return the name of the construct. This name is also the command by which
     * this construct can be called inside scripts
     */
    public String getName() {
        return defaultName;
    }

    /**
     * Builds a new process and return it ready for input.
     *
     * @param context the context for which to prepare the {@link Construct}
     * @return a prepared processor loaded with the {@link Construct} behaviour
     */
    public abstract ConstructProcessor prepareProcess(final ConstructContext context);

    /**
     * Checks whether a {@link MetaExpression} is <b>NOT</b> null, by calling {@link MetaExpression#isNull()}.
     *
     * @param expression   the expression to check
     * @param argumentName the name of the expression. This is used to generate understandable debug messages.
     * @throws RobotRuntimeException when the assertion fails
     */
    protected static void assertNotNull(final MetaExpression expression, final String argumentName) {
        if (expression.isNull()) {
            throw new RobotRuntimeException(argumentName + " cannot be null");
        }
    }

    /**
     * Checks whether a {@link MetaExpression} is null. This is done by calling {@link MetaExpression#isNull()}.
     *
     * @param expression   the expression to check
     * @param argumentName the name of the expression. This is used to generate understandable debug messages.
     * @throws RobotRuntimeException when the assertion fails
     */
    protected static void assertIsNull(final MetaExpression expression, final String argumentName) {
        if (!expression.isNull()) {
            throw new RobotRuntimeException(argumentName + " must be null");
        }
    }

    /**
     * Checks whether the {@link MetaExpression} contains an instance of meta
     * information and fetches it.
     *
     * @param <T>              the type of meta information to assert
     * @param expression       the expression to check
     * @param expressionName   the name of the expression. This would generally be a
     *                         parameter name in the construct. It is used to give the
     *                         developer an understandable message.
     * @param type             the {@link Class} of the meta object to fetch
     * @param friendlyTypeName the friendly name of the type. This will be used to generate
     *                         an understandable message
     * @return the requested meta object
     * @throws RobotRuntimeException when the assertion fails
     */
    protected static <T extends MetadataExpression> T assertMeta(final MetaExpression expression, final String expressionName, final Class<T> type, final String friendlyTypeName) {
        T value = expression.getMeta(type);
        if (value == null) {
            throw new RobotRuntimeException("Expected " + expressionName + " to be a " + friendlyTypeName);
        }

        return value;
    }

    @Override
    public URL getDocumentationResource() {
        String url = "/" + getClass().getName().replace('.', '/') + ".xml";
        return getClass().getResource(url);
    }

    /**
     * Uses the FileResolver service to get a File object from a path.
     *
     * @param context the construct context
     * @param path    the path
     * @return the file
     * @deprecated Use {@link #getPath(ConstructContext, MetaExpression)} instead
     */
    @Deprecated
    protected static File getFile(ConstructContext context, String path) {
        return fileResolver.buildFile(context, path);
    }

    /**
     * Uses the FileResolver service to get a File object from a path.
     *
     * @param context the construct context
     * @param path    the path
     * @return the path
     */
    protected static Path getPath(ConstructContext context, MetaExpression path) {
        return fileResolver.buildPath(context, path);
    }

    /**
     * Hides this construct from documentation.
     *
     * @return whether the construct's documentation should be hidden
     */
    public boolean hideDocumentation() {
        return false;
    }

    /**
     * Determines if this construct is deprecated.
     *
     * Looks for an {@link Deprecated} annotation by default.
     *
     * @return True if this construct is deprecated, false if not
     */
    public boolean isDeprecated(){
        return getClass().isAnnotationPresent(Deprecated.class);
    }
}
