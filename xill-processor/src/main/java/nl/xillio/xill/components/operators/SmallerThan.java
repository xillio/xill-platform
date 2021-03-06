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
package nl.xillio.xill.components.operators;

import nl.xillio.xill.api.components.Processable;

/**
 * This class represents the &lt; operator
 */
public final class SmallerThan extends BinaryNumberComparisonOperator {

    public SmallerThan(Processable left, Processable right) {
        super(left, right);
    }

    @Override
    protected boolean translate(int comparisonResult) {
        return comparisonResult < 0;
    }

}
