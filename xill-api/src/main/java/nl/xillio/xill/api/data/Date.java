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
package nl.xillio.xill.api.data;


import java.time.ZonedDateTime;

/**
 * This interface represents a date object in the Xill language.
 *
 * @author Thomas Biesaart
 * @author Sander Visser
 * @author Geert Konijnendijk
 * @since 3.0.0
 */
public interface Date extends MetadataExpression {

	/**
	 * Returns a ZonedDateTime that represents the date stored in this object.
	 *
	 * @return the date
	 */
	ZonedDateTime getZoned();
}
