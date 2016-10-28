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
                this::process,
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
        int sourceLengthPlus1 = source.length() + 1;
        int targetLengthPlus1 = target.length() + 1;
        int[] distanceLog = new int[sourceLengthPlus1 * targetLengthPlus1];

        if (targetLengthPlus1 == 1) {
            return sourceLengthPlus1 - 1;
        }
        if (sourceLengthPlus1 == 1) {
            return targetLengthPlus1 - 1;
        }

        int distanceLogIndex = 0;

        // Start row with constant
        for (int targetIndex = 0; targetIndex < targetLengthPlus1; targetIndex++) {
            distanceLog[distanceLogIndex] = targetIndex;
            distanceLogIndex += sourceLengthPlus1;
        }

        for (int sourceIndex = 0; sourceIndex < sourceLengthPlus1 - 1; sourceIndex++) {
            distanceLogIndex = sourceIndex + 1;
            distanceLog[distanceLogIndex] = distanceLogIndex; // Start column with constant

            for (int targetIndex = 0; targetIndex < targetLengthPlus1 - 1; targetIndex++) {
                distanceLogIndex += sourceLengthPlus1;
                distanceLog[distanceLogIndex] = calculateDistance(source, target, sourceIndex, targetIndex, distanceLog, distanceLogIndex, sourceLengthPlus1);
            }
        }
        return distanceLog[distanceLogIndex];
    }

    private int calculateDistance(final String source, final String target, final int sourceIndex, final int targetIndex,
                                  final int[] distanceLog, final int distanceLogIndex, final int sourceLengthPlus1) {

        int cost = (source.charAt(sourceIndex) == target.charAt(targetIndex)) ? 0 : 1;

        // Insertion, Deletion and Substitution
        int min = Math.min(distanceLog[distanceLogIndex - 1] + 1, Math.min(distanceLog[distanceLogIndex - sourceLengthPlus1] + 1,
                distanceLog[distanceLogIndex - sourceLengthPlus1 - 1] + cost));

        // Transposition
        if (sourceIndex > 0 && targetIndex > 0 && source.charAt(sourceIndex) == target.charAt(targetIndex - 1) &&
                source.charAt(sourceIndex - 1) == target.charAt(targetIndex)) {
            min = Math.min(distanceLog[distanceLogIndex - 2 * sourceLengthPlus1 - 2] + cost, min);
        }

        return min;
    }
}
