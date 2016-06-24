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
package nl.xillio.xill.plugins.string.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.string.services.string.UrlUtilityService;

/**
 * <p>
 * Converts a relative URL string to an absolute URL using a string, pageUrl, as base URL.
 * </p>
 */
public class AbsoluteURLConstruct extends Construct {

    @Inject
    private UrlUtilityService urlUtilityService;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (pageUrl, relativeUrl) -> process(pageUrl, relativeUrl, urlUtilityService),
                new Argument("pageUrl", ATOMIC),
                new Argument("relativeUrl", ATOMIC));
    }

    static MetaExpression process(final MetaExpression pageUrlVar, final MetaExpression relativeUrlVar, final UrlUtilityService urlUtilityService) {
        String pageUrl = pageUrlVar.getStringValue().trim();
        String relativeUrl = relativeUrlVar.getStringValue().trim();

        if (!pageUrl.startsWith("http://") && !pageUrl.startsWith("https://")) {
            pageUrl = "http://" + pageUrl;
        }

        if (pageUrl.endsWith("/") && relativeUrl.isEmpty()) {
            pageUrl = pageUrl.substring(0, pageUrl.length() - 1);
        }

        if (relativeUrl.isEmpty()) {
            return fromValue(urlUtilityService.cleanupUrl(pageUrl));
        }
        try {
            String processed = urlUtilityService.tryConvert(pageUrl, relativeUrl);

            if (processed != null) {
                return fromValue(processed);
            } else {
                throw new OperationFailedException("convert relative URL to absolute URL", "The page url parameter is invalid.", "Pass correct page url.");
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidUserInputException("Illegal argument was handed to the matcher when trying to convert the URL.", pageUrl, "Correct page url.",
                    "use String;\nString.absoluteURL(\"http://www.xillio.nl/calendar/\", \"movies\");", e);
        }

    }
}
