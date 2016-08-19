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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.FileService;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static nl.xillio.xill.plugins.web.services.web.WebService.MIN_VIEWPORT_HEIGHT;
import static nl.xillio.xill.plugins.web.services.web.WebService.MIN_VIEWPORT_WIDTH;

/**
 * Capture screenshot of currently loaded page and save it to a .png file
 */
public class ScreenshotConstruct extends PhantomJSConstruct {

    private static final String EXAMPLE = "use System, Web;\nvar page = Web.loadPage(\"http://www.xillio.com\");\nWeb.screenshot(page, \"c:/tmp/test.png\",{\"resolution\" : [1024,768]});";
    private static final String EXPECTED_INPUT = String.format("A resolution of the screenshot - the list of width and height in pixels.%nMinimum resolution is %1$dx%2$d pixels.", MIN_VIEWPORT_WIDTH, MIN_VIEWPORT_HEIGHT);

    @Inject
    private FileService fileService;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (page, fileName, options) -> process(page, fileName, options, fileService, webService, context),
                new Argument("page", ATOMIC),
                new Argument("filename", ATOMIC),
                new Argument("options", NULL, OBJECT));
    }

    /**
     * @param pageVar     input variable (should be of a PAGE type)
     * @param fileService The service we're using for files.
     * @param webService  The service we're using for accessing the web.
     * @param fileName    input string variable - output .png filepath
     * @param context     the context of this construct
     * @param optionsVar  the construct options
     * @throws OperationFailedException if an io error occurred
     * @return null variable
     */
    public static MetaExpression process(final MetaExpression pageVar, final MetaExpression fileName, final MetaExpression optionsVar, final FileService fileService, final WebService webService, final ConstructContext context) {

        if (pageVar.isNull()) {
            return NULL;
        }
        if (fileName.getStringValue().isEmpty()) {
            throw new RobotRuntimeException("Invalid variable value. Filename is empty!");
        }

        int width = 0;
        int height = 0;
        if (!optionsVar.isNull()) {
            final MetaExpression value = ((Map<String, MetaExpression>)optionsVar.getValue()).get("resolution");
            if (value == null || value.getType() != LIST) {
                throw new InvalidUserInputException("Invalid options.", optionsVar.getStringValue(), EXPECTED_INPUT, EXAMPLE);
            }

            final List<MetaExpression> list = value.getValue();
            width = list.get(0).getNumberValue().intValue();
            height = list.get(1).getNumberValue().intValue();
            if (width < MIN_VIEWPORT_WIDTH || height < MIN_VIEWPORT_HEIGHT) {
                throw new InvalidUserInputException("Invalid resolution value.", optionsVar.getStringValue(), EXPECTED_INPUT, EXAMPLE);
            }
        }

        checkPageType(pageVar);

        WebVariable driver = getPage(pageVar);

        try {
            Path srcFile = webService.getScreenshotAsFilePath(driver, width, height);
            Path desFile = getPath(context, fileName);
            fileService.copyFile(srcFile, desFile);
        } catch (IOException e) {
            throw new OperationFailedException("copy to: " + fileName.getStringValue(), "An IOException occurred", e);
        }

        return NULL;
    }

}
