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

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.OptionsFactory;
import nl.xillio.xill.plugins.web.services.web.FileService;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.io.File;
import java.io.IOException;

/**
 * It loads web page from a provided string (the string represents HTML code of a web page)
 */
public class FromString extends PhantomJSConstruct {
    @Inject
    private FileService fileService;
    @Inject
    private OptionsFactory optionsFactory;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                content -> process(content, optionsFactory, fileService, getWebService()),
                new Argument("content", ATOMIC));
    }

    /**
     * @param contentVar     input string variable (HTML code of a web page)
     * @param optionsFactory The factory for creating options the {@link LoadPageConstruct} will be using.
     * @param fileService    The service for files we're using.
     * @param webService     The webservice the {@link LoadPageConstruct} will be using.
     * @throws OperationFailedException if the string could not not be written to a file
     * @return PAGE variable
     */
    public static MetaExpression process(final MetaExpression contentVar, final OptionsFactory optionsFactory, final FileService fileService, final WebService webService) {
        String content = contentVar.getStringValue();

        try {
            File htmlFile = fileService.createTempFile("ct_sel", ".html");
            fileService.writeStringToFile(htmlFile.toPath(), content);
            String uri = "file:///" + fileService.getAbsolutePath(htmlFile);
            return LoadPageConstruct.process(fromValue(uri), NULL, optionsFactory, webService);
        } catch (IOException e) {
            throw new OperationFailedException("write the string to a file.", "An IO error occurred.", e);
        }
    }

}
