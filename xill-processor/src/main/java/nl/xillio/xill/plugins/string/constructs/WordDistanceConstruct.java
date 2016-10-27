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
 *
 * Returns a number between 0 (no likeness) and 1 (identical), indicating how much the two strings are alike.
 *
 * If the option 'relative' is set to false, then the absolute editdistance will be returned rather than a relative distance.
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

    private MetaExpression process(final MetaExpression source, final MetaExpression target, final MetaExpression relative) {
        assertNotNull(source, "source");
        assertNotNull(target, "target");

        int edits = damerauLevenshteinDistance(source.getStringValue(), target.getStringValue());

        if (!relative.getBooleanValue()) {
            return fromValue(edits);
        }

        int maxLength = Math.max(source.getStringValue().length(), target.getStringValue().length());
        double similarity = 1 - (double) edits / maxLength;
        return fromValue(similarity);
    }

    /**
     * Returns a number between 0 (no likeness) and 1 (identical), indicating how much the two strings are alike.
     * @param source one of the two words
     * @param target the other word
     * @return likeness of the two strings (between 0 and 1)
     */
    private int damerauLevenshteinDistance(final String source, final String target) {
        int[] distanceLog = new int[(source.length() + 1) * (target.length() + 1)];

        int lenS1 = source.length() + 1;
        int lenT1 = target.length() + 1;

        if (lenT1 == 1) {
            return lenS1 - 1;
        }
        if (lenS1 == 1) {
            return lenT1 - 1;
        }

        int dlIndex = 0;
        int min;
        int cost;

        // start row with constant
        for (int tmp = 0; tmp < lenT1; tmp++) {
            distanceLog[dlIndex] = tmp;
            dlIndex += lenS1;
        }
        for (int sIndex = 0; sIndex < lenS1 - 1; sIndex++) {
            dlIndex = sIndex + 1;
            distanceLog[dlIndex] = dlIndex; // start column with constant
            for (int tIndex = 0; tIndex < lenT1 - 1; tIndex++) {
                dlIndex += lenS1;

                cost = (source.charAt(sIndex) == target.charAt(tIndex)) ? 0 : 1;

                // Insertion, Deletion and Substitution
                min = Math.min(distanceLog[dlIndex - 1] + 1, Math.min(distanceLog[dlIndex - lenS1] + 1, distanceLog[dlIndex - lenS1 - 1] + cost));

                // Transposition
                if (sIndex > 0 && tIndex > 0 && source.charAt(sIndex) == target.charAt(tIndex - 1) &&
                        source.charAt(sIndex - 1) == target.charAt(tIndex)) {
                    min = Math.min(distanceLog[dlIndex - 2 * lenS1 - 2] + cost, min);
                }

                distanceLog[dlIndex] = min;
            }
        }
        return distanceLog[dlIndex];
    }
}
