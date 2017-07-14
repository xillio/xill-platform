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
package nl.xillio.xill.plugins.date.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.date.BaseDateConstruct;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * constructs a date from the provided values.
 *
 * @author Sander Visser
 */
public class OfConstruct extends BaseDateConstruct {

    @Override
    @SuppressWarnings("squid:S2095")
    // Suppress "Resources should be closed": Arguments do not need to be closed here, because ConstructProcessor closes them
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        Argument args[] = {
                new Argument("year"), new Argument("month"),
                new Argument("day"), new Argument("hour"),
                new Argument("minute"), new Argument("second"),
                new Argument("nano", fromValue(0)),
                new Argument("zone", fromValue(ZoneId.systemDefault().getId()))
        };

        return new ConstructProcessor(this::process, args);
    }

    @SuppressWarnings("squid:S1166")
    private MetaExpression process(final MetaExpression[] input) {
        Date date;
        ZoneId zone;

        for (MetaExpression m : input) {
            assertNotNull(m, "input");
        }

        int year = input[0].getNumberValue().intValue();
        int month = input[1].getNumberValue().intValue();
        int day = input[2].getNumberValue().intValue();
        int hour = input[3].getNumberValue().intValue();
        int minute = input[4].getNumberValue().intValue();
        int second = input[5].getNumberValue().intValue();
        int nano = input[6].getNumberValue().intValue();

        try {
            zone = ZoneId.of(input[7].getStringValue());
        } catch (DateTimeException e) {
            throw new InvalidUserInputException("Invalid zone ID", input[7].getStringValue(), "A valid timezone.", "use System;\n" +
                    "use Date;\n" +
                    "var date = Date.of(2015, 12, 31, 10, 5, 0);\n" +
                    "System.print(date);", e);
        }
        try {
            date = dateService.constructDate(year, month, day, hour, minute, second, nano, zone);
        } catch (DateTimeException e) {
            throw new OperationFailedException("create a date variable", e.getLocalizedMessage(), e);
        }

        return fromValue(date);

    }
}
