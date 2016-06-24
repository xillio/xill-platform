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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.codec.decode.services.DecoderService;
import org.apache.commons.codec.DecoderException;

import java.nio.charset.UnsupportedCharsetException;

/**
 * Implementation of Decode.FromHex Construct. See {@link DecoderService#fromHex(String, String)}.
 *
 * @author Paul van der Zandt
 * @since 3.0
 */
public class FromHexConstruct extends Construct {

    private final DecoderService decoderService;

    @Inject
    public FromHexConstruct(DecoderService decoderService) {
        this.decoderService = decoderService;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(this::process,
                new Argument("hexString", ATOMIC),
                new Argument("charset", fromValue("UTF-8"), ATOMIC));
    }

    @SuppressWarnings("squid:UnusedPrivateMethod")
    private MetaExpression process(MetaExpression hexString, MetaExpression charsetName) {
        try {
            return hexString.isNull() ? NULL : fromValue(decoderService.fromHex(hexString.getStringValue(), charsetName.getStringValue()));
        } catch (DecoderException e) {
            throw new OperationFailedException("convert hex to normal", e.getMessage(), e);
        } catch (UnsupportedCharsetException e) {
            throw new InvalidUserInputException("Unknown character set.", charsetName.getStringValue(), "A valid character set.","use Decode;\n" +
                    "use System\n" +
                    "\n" +
                    "System.print(Decode.fromHex(\"C3A4C3ABC384\"));\n" +
                    "// prints äëÄ\n" +
                    "System.print(Decode.fromHex(\"e4ebc4\", \"ISO-8859-1\"));\n" +
                    "// also prints äëÄ" ,e);
        }
    }
}
