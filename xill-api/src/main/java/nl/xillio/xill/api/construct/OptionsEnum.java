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

import java.util.Optional;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

/**
 * This interface represents a way for constructs and option parsers to back their options using an enumeration.
 *
 * You can define your enumeration by implementing this interface.
 * <code>
 *     public enum Options implements OptionsEnum {
 *          NAME,
 *          AGE,
 *          DATE;
 *     }
 * </code>
 *
 * Then you can use the label() and ofLabel methods to fetch enumerations using lowerCamelCase conventions.
 * <code>
 *     Option option = OptionsEnum.ofLabel(Option.class, "optionA");
 *
 *     // Or
 *
 *     String optionLabel = Option.OPTION_A.label();
 * </code>
 *
 * @author Thomas Biesaart
 * @author Titus Nachbauer
 */
public interface OptionsEnum {

    /**
     * Returns the enum constant of the specified enum type with the
     * specified label.  The label is the lowerCamelCase variant of the
     * UPPER_CAMEL_CASE
     *
     * @param <T> The enum type whose constant is to be returned
     * @param enumType the {@code Class} object of the enum type from which
     *      to return a constant
     * @param label the lowerCamelCase label of an enumeration
     * @return the enum constant of the specified enum type with the
     *      specified label
     * @throws NullPointerException if {@code enumType} or {@code name}
     *         is null
     */
    @SuppressWarnings("squid:S1166") // This exception is handled by returning a non-result
    static <T extends Enum<T>> Optional<T> ofLabel(Class<T> enumType, String label) {
        String name = LOWER_CAMEL.to(UPPER_UNDERSCORE, label);

        try {
            return Optional.of(Enum.valueOf(enumType, name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the label of this enum constant, which is the lowerCamelCase
     * variant of the declared (UPPER_UNDERSCORE) name.
     *
     * @return the label of this enum constant
     */
    default String label() {
        return UPPER_UNDERSCORE.to(LOWER_CAMEL, name());
    }

    String name();
}
