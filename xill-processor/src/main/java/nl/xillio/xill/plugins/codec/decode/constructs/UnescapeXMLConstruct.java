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
package nl.xillio.xill.plugins.codec.decode.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.codec.decode.services.DecoderService;

/**
 * <p>
 * Decodes all ampersand-encoded characters in the provided text.
 * </p>
 *
 * @author Sander Visser
 */
public class UnescapeXMLConstruct extends Construct {

    private DecoderService decoderService;

    @Inject
    public UnescapeXMLConstruct(DecoderService decoderService) {
        this.decoderService = decoderService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("string", ATOMIC),
                new Argument("passes", fromValue(1), ATOMIC));
    }

    MetaExpression process(final MetaExpression stringVar, final MetaExpression passesVar) {
        assertNotNull(stringVar, "string");
        assertNotNull(passesVar, "passes");

        String text = stringVar.getStringValue();

        int passes = passesVar.getNumberValue().intValue();

        return fromValue(decoderService.unescapeXML(text, passes));
    }
}
