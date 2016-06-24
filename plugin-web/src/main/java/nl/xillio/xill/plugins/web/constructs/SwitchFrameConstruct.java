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
package nl.xillio.xill.plugins.web.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.NodeVariable;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

/**
 * Switch current page context to a provided frame
 */
public class SwitchFrameConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (page, frame) -> process(page, frame, webService),
                new Argument("page", ATOMIC),
                new Argument("frame", ATOMIC));
    }

    /**
     * @param pageVar    input variable (should be of a PAGE type)
     * @param frameVar   input variable - frame specification - string or number or web element (NODE variable)
     * @param webService The service we're using to access the web.
     * @throws OperationFailedException if the frame parameter could not be resolved
     * @return null variable
     */
    public static MetaExpression process(final MetaExpression pageVar, final MetaExpression frameVar, final WebService webService) {

        checkPageType(pageVar);
        WebVariable driver = getPage(pageVar);

        if(frameVar.getMeta(NodeVariable.class) != null){
            WebVariable element = getNode(frameVar);
            webService.switchToFrame(driver,element);
        } else{
            Object frame = MetaExpression.extractValue(frameVar);
            if(frame instanceof Integer){
                webService.switchToFrame(driver, (Integer) frame);
            } else if(frame instanceof  String){
                webService.switchToFrame(driver,(String) frame);
            } else{
                throw new OperationFailedException("prepare switching the frame", "Could not resolve \'frame\' parameter.");
            }
        }

        return NULL;
    }

}
