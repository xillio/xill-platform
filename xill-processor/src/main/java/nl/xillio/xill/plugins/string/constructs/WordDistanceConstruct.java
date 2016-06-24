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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;

/**
 * <p>
 * Returns a number between 0 (no likeness) and 1 (identical), indicating how much the two strings are alike.
 * </p>
 * <p>
 * If the option 'relative' is set to false, then the absolute editdistance will be returned rather than a relative distance.
 * </p>
 *
 * @author Sander
 */
public class WordDistanceConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {

        return new ConstructProcessor(
                (source, target, relative) -> process(source, target, relative),
                new Argument("source", ATOMIC),
                new Argument("target", ATOMIC),
                new Argument("relative", TRUE));
    }

    static MetaExpression process(final MetaExpression source, final MetaExpression target, final MetaExpression relative) {
        assertNotNull(source, "source");
        assertNotNull(target, "target");

        int edits = damlev(source.getStringValue(), target.getStringValue());

        if (!relative.getBooleanValue()) {
            return fromValue(edits);
        }

        int maxlength = Math.max(source.getStringValue().length(), target.getStringValue().length());
        double similarity = 1 - (double) edits / maxlength;
        return fromValue(similarity);
    }

    private static int damlev(final String source, final String target) {
        int[] workspace = new int[1024];
        int lenS = source.length();
        int lenT = target.length();

        if (lenS * lenT > workspace.length) {
            workspace = new int[(source.length() + 1) * (target.length() + 1)];
        }

        int lenS1 = lenS + 1;
        int lenT1 = lenT + 1;

        if (lenT1 == 1) {
            return lenS1 - 1;
        }
        if (lenS1 == 1) {
            return lenT1 - 1;
        }

        int[] dl = workspace;
        int dlIndex = 0;
        int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0, min = 0, cost = 0, tmp = 0;
        int tri = lenS1 + 2;

        // start row with constant
        dlIndex = 0;
        for (tmp = 0; tmp < lenT1; tmp++) {
            dl[dlIndex] = tmp;
            dlIndex += lenS1;
        }
        for (int sIndex = 0; sIndex < lenS; sIndex++) {
            dlIndex = sIndex + 1;
            dl[dlIndex] = dlIndex; // start column with constant
            for (int tIndex = 0; tIndex < lenT; tIndex++) {
                rowBefore = dlIndex;
                dlIndex += lenS1;
                // deletion
                min = dl[rowBefore] + 1;
                // insertion
                tmp = dl[dlIndex - 1] + 1;
                if (tmp < min) {
                    min = tmp;
                }
                cost = 1;
                if (source.charAt(sIndex) == target.charAt(tIndex)) {
                    cost = 0;
                }
                if (sIndex > 0 && tIndex > 0) {
                    if (source.charAt(sIndex) == target.charAt(tPrevIndex) && source.charAt(sPrevIndex) == target.charAt(tIndex)) {
                        tmp = dl[rowBefore - tri] + cost;
                        // transposition
                        if (tmp < min) {
                            min = tmp;
                        }
                    }
                }
                // substitution
                tmp = dl[rowBefore - 1] + cost;
                if (tmp < min) {
                    min = tmp;
                }
                dl[dlIndex] = min;
                tPrevIndex = tIndex;
            }
            sPrevIndex = sIndex;
        }
        return dl[dlIndex];
    }
}
