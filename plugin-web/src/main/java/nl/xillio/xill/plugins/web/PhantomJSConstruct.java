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
package nl.xillio.xill.plugins.web;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.web.data.NodeVariable;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.openqa.selenium.Cookie;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

/**
 * The base class for all the constructs which use PhantomJS.
 */
public abstract class PhantomJSConstruct extends Construct {
    @Inject
    protected WebService webService;

    /**
     * Creates new {@link NodeVariable}.
     *
     * @param driver     PhantomJS driver (page)
     * @param element    web element on the page (represented by driver)
     * @param webService the service that should be used
     * @return created variable
     */
    protected static MetaExpression createNode(final WebVariable driver, final WebVariable element, final WebService webService) {
        MetaExpression var = fromValue(webService.getAttribute(element, "outerHTML"));
        var.storeMeta(webService.createNodeVariable(driver, element));
        return var;
    }

    /**
     * Extracts web element from {@link NodeVariable}.
     *
     * @param var input variable (should be of a NODE type)
     * @return web element
     */
    protected static NodeVariable getNode(final MetaExpression var) {
        return var.getMeta(NodeVariable.class);
    }


    /**
     * Do the test if input {@link MetaExpression} if it's of NODE type.
     *
     * @param var MetaExpression (any variable)
     * @throws OperationFailedException if meta expression does not contain NODE variable
     */
    protected static void checkNodeType(final MetaExpression var) {
        if (var.getMeta(NodeVariable.class) == null) {
            throw new OperationFailedException("extract NODE variable from OBJECT.", "No NODE could be found in MetaData.");
        }
    }

    /**
     * Do the test if input {@link MetaExpression} if it's of PAGE type.
     *
     * @param var MetaExpression (any variable)
     * @throws OperationFailedException if meta expression does not contain PAGE variable
     */
    protected static void checkPageType(final MetaExpression var) {
        if (var.getMeta(PageVariable.class) == null) {
            throw new OperationFailedException("extract PAGE variable from OBJECT.", "No PAGE could be found in MetaData.");
        }
    }

    /**
     * Tests if input {@link MetaExpression} is a NODE and not a PAGE type.
     * Throws an error if input is none of both.
     * @param var the MetaExpression (any variable)
     * @throws OperationFailedException if input not of the NODE or PAGE type
     * @return if the input metadata contains a NODE type. We then expect that it is not a PAGE type aswell.
     */
    protected static boolean isNodeAndNotPage(final MetaExpression var) {
        if (var.getMeta(NodeVariable.class) == null && var.getMeta(PageVariable.class) == null) {
            throw new OperationFailedException("extract NODE or PAGE variable from OBJECT", "No NODE or PAGE variable could be found in MetaData.");
        } else {
            return var.getMeta(NodeVariable.class) != null;
        }
    }

    /**
     * Creates new {@link PageVariable}.
     *
     * @param item       from PhantomJS pool
     * @param webService the service that should be used
     * @return created PAGE variable
     */
    protected static MetaExpression createPage(final WebVariable item, final WebService webService) {
        MetaExpression var = fromValue(webService.getCurrentUrl(item));
        var.storeMeta(item);
        return var;
    }

    /**
     * Extracts a PageVariable from a MetaExpression.
     *
     * @param var input variable (should be of a PAGE type).
     * @return driver (page)
     */
    protected static PageVariable getPage(final MetaExpression var) {
        return var.getMeta(PageVariable.class);
    }

    /**
     * Creates an associated list variable that contains all information about one cookie.
     *
     * @param cookie     Selenium's cookie class
     * @param webService the service that should be used
     * @return created cookie variable
     */
    protected static MetaExpression makeCookie(final Cookie cookie, final WebService webService) {
        LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<String, MetaExpression>();
        map.put("name", ExpressionBuilderHelper.fromValue(webService.getName(cookie)));
        map.put("domain", ExpressionBuilderHelper.fromValue(webService.getDomain(cookie)));
        map.put("path", ExpressionBuilderHelper.fromValue(webService.getPath(cookie)));
        map.put("value", ExpressionBuilderHelper.fromValue(webService.getValue(cookie)));

        if (cookie.getExpiry() != null) {
            map.put("expires", ExpressionBuilderHelper.fromValue(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S").format(cookie.getExpiry())));
        }

        return ExpressionBuilderHelper.fromValue(map);
    }
}
