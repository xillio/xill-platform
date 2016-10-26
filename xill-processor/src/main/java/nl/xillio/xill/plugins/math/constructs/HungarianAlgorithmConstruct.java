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
package nl.xillio.xill.plugins.math.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This construct processes a matrix using the hungarian algorithm
 * <p>
 * Hungarian Algorithm implementation by Konstantinos A. Nedas
 * Credits and code below
 * Created on Apr 25, 2005
 * <p>
 * Munkres-Kuhn (Hungarian) Algorithm Clean Version: 0.11
 * <p>
 * Konstantinos A. Nedas Department of Spatial Information Science &amp;
 * Engineering University of Maine, Orono, ME 04469-5711, USA
 * kostas@spatial.maine.edu http://www.spatial.maine.edu/~kostas
 * <p>
 * This Java class implements the Hungarian algorithm
 * <p>
 * It takes 2 arguments: a. A 2-D array (could be rectangular or square). b.
 * A string ("min" or "max") specifying whether you want the min or max
 * assignment. [It returns an assignment matrix[array.length][2] that
 * contains the row and col of the elements (in the original inputted array)
 * that make up the optimum assignment.]
 * <p>
 * Any comments, corrections, or additions would be much appreciated. Credit
 * due to professor Bob Pilgrim for providing an online copy of the
 * pseudo code for this algorithm
 * (http://216.249.163.93/bob.pilgrim/445/munkres.html)
 * <p>
 * Feel free to redistribute this source code, as long as this header--with
 * the exception of sections in brackets--remains as part of the file.
 * <p>
 * Requirements: JDK 1.5.0_01 or better.
 *
 * @author Ivor van der Hoog
 * @author Ernst van Rheenen
 * @author Konstantinos A. Nedas
 */
public class HungarianAlgorithmConstruct extends Construct {
    // The example used in the InvalidUserInputException.
    private static final String EXAMPLE = "use Math;\nvar matrix = [[0,1,3], [2,2,3], [5,4,1]];\nMath.hungarianAlgorithm(matrix, true);";

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(this::process, new Argument("matrix", LIST), new Argument("max", TRUE, ATOMIC));
    }

    private MetaExpression process(final MetaExpression matrixVar, final MetaExpression maxVar) {
        assertNotNull(matrixVar, "matrix");

        if (matrixVar.getType() != LIST) {
            throw new InvalidUserInputException("No matrix given.", matrixVar.getStringValue(), "Two-dimensional list containing numbers.", EXAMPLE);
        }

        List<MetaExpression> matrix = matrixVar.getValue();

        String method = "max";
        if (!maxVar.getBooleanValue()) {
            method = "min";
        }

        // Prepare array
        int rows = matrix.size();
        if (rows < 1) {
            throw new InvalidUserInputException("Not enough data.", matrixVar.getStringValue(), "At least 1 row with data.", EXAMPLE);
        }

        MetaExpression var = matrix.get(0);

        if (var.getType() != LIST) {
            return NULL;
        }

        List<MetaExpression> varList = var.getValue();

        int columns = varList.size();

        if (columns < 1) {
            throw new InvalidUserInputException("Not enough data.", matrixVar.getStringValue(), "At least 1 column with data.", EXAMPLE);
        }

        if (rows == 0 || columns == 0) {
            return NULL;
        }

        double[][] array = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                array[i][j] = getMatrixValue(matrix, i, j);
                if (Double.isNaN(array[i][j])) {
                    throw new InvalidUserInputException("Invalid value at [" + i + "," + j + "].", ((List<MetaExpression>) matrix.get(i).getValue()).get(j).getStringValue(), "A numerical value.", EXAMPLE);
                }
            }
        }

        // Transpose if required
        boolean transposed = false;
        if (array.length > array[0].length) {
            array = transpose(array);
            transposed = true;
        }

        // Perform the actual calculation
        int[][] assignment = hgAlgorithm(array, method);

        // Calculate the final score
        double sum = 0;
        for (int[] element : assignment) {
            sum = sum + array[element[0]][element[1]];
        }

        // Transpose results back if required
        if (transposed) {
            for (int i = 0; i < assignment.length; i++) {
                int row = assignment[i][0];
                int col = assignment[i][1];
                assignment[i][0] = col;
                assignment[i][1] = row;
            }
        }

        // Prepare results
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put("sum", fromValue(sum));

        List<MetaExpression> cells = new ArrayList<>();
        for (int[] element : assignment) {
            LinkedHashMap<String, MetaExpression> item = new LinkedHashMap<>();
            item.put("row", fromValue(element[0]));
            item.put("col", fromValue(element[1]));
            cells.add(fromValue(item));
        }
        result.put("cells", fromValue(cells));

        return fromValue(result);
    }

    private static double getMatrixValue(final List<MetaExpression> matrix, final int row, final int col) {
        MetaExpression rowVariable = matrix.get(row);
        if (rowVariable.getValue() == NULL || rowVariable.getType() != LIST) {
            return Double.NaN;
        }

        @SuppressWarnings("unchecked")
        MetaExpression cell = ((List<MetaExpression>) rowVariable.getValue()).get(col);
        if (cell.getValue() == NULL || cell.getType() != ATOMIC) {
            return Double.NaN;
        }

        return cell.getNumberValue().doubleValue();

    }

    /**
     * Check if a double value equals zero by byte comparison.
     *
     * @param value The double value to check.
     * @return True if the value equals zero.
     */
    private static boolean doubleEqualsZero(final double value) {
        return Double.doubleToRawLongBits(value) == 0;
    }

    /**
     * Finds the largest element in a positive array. Works for arrays where all
     * values are >= 0.
     *
     * @param array a positive array
     * @return the largest element
     */
    private static double findLargest(final double[][] array) {
        double largest = 0;
        for (double[] element : array) {
            for (double element2 : element) {
                if (element2 > largest) {
                    largest = element2;
                }
            }
        }
        return largest;
    }

    /**
     * Transposes a double[][] array.
     *
     * @param array an array
     * @return the transposed array
     */
    private static double[][] transpose(final double[][] array) {
        double[][] transposedArray = new double[array[0].length][array.length];
        for (int i = 0; i < transposedArray.length; i++) {
            for (int j = 0; j < transposedArray[i].length; j++) {
                transposedArray[i][j] = array[j][i];
            }
        }
        return transposedArray;
    }

    /**
     * Copies all elements of an array to a new array.
     *
     * @param original an array
     * @return a copy of the array
     */
    private static double[][] copyOf(final double[][] original) {
        double[][] copy = new double[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            // Need to do it this way, otherwise it copies only memory location
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    // **********************************//
    // METHODS OF THE HUNGARIAN ALGORITHM//
    // **********************************//

    private static int[][] hgAlgorithm(final double[][] array, final String sumType) {
        double[][] cost = copyOf(array); // Create the cost matrix
        if ("max".equalsIgnoreCase(sumType)) { // Then array is weight array.
            // Must change to cost.
            double maxWeight = findLargest(cost);
            for (int i = 0; i < cost.length; i++) { // Generate cost by
                // subtracting.
                for (int j = 0; j < cost[i].length; j++) {
                    cost[i][j] = maxWeight - cost[i][j];
                }
            }
        }
        double maxCost = findLargest(cost); // Find largest cost matrix element
        // (needed for step 6).

        int[][] mask = new int[cost.length][cost[0].length]; // The mask array.
        int[] rowCover = new int[cost.length]; // The row covering vector.
        int[] colCover = new int[cost[0].length]; // The column covering vector.
        int[] zeroRC = new int[2]; // Position of last zero from Step 4.
        int step = 1;
        boolean done = false;
        while (!done) {
            switch (step) {
                case 1:
                    step = hgStep1(cost);
                    break;
                case 2:
                    step = hgStep2(cost, mask, rowCover, colCover);
                    break;
                case 3:
                    step = hgStep3(mask, colCover);
                    break;
                case 4:
                    step = hgStep4(cost, mask, rowCover, colCover, zeroRC);
                    break;
                case 5:
                    step = hgStep5(mask, rowCover, colCover, zeroRC);
                    break;
                case 6:
                    step = hgStep6(cost, rowCover, colCover, maxCost);
                    break;
                case 7:
                    done = true;
                    break;
                default:
                    break;
            }
        }

        int[][] assignment = new int[array.length][2]; // Create the returned
        // array.
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[i].length; j++) {
                if (mask[i][j] == 1) {
                    assignment[i][0] = i;
                    assignment[i][1] = j;
                }
            }
        }

        return assignment;
    }

    private static int hgStep1(final double[][] cost) {
        // What STEP 1 does:
        // For each row of the cost matrix, find the smallest element
        // and subtract it from from every other element in its row.
        double minval;
        for (int i = 0; i < cost.length; i++) {
            minval = cost[i][0];
            for (int j = 0; j < cost[i].length; j++) { // 1st inner loop finds
                // min val in row.
                if (minval > cost[i][j]) {
                    minval = cost[i][j];
                }
            }
            for (int j = 0; j < cost[i].length; j++) { // 2nd inner loop
                // subtracts it.
                cost[i][j] = cost[i][j] - minval;
            }
        }

        return 2;
    }

    private static int hgStep2(final double[][] cost, final int[][] mask, final int[] rowCover, final int[] colCover) {
        // What STEP 2 does:
        // Marks uncovered zeros as starred and covers their row and column.

        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[i].length; j++) {
                if (doubleEqualsZero(cost[i][j]) && colCover[j] == 0 && rowCover[i] == 0) {
                    mask[i][j] = 1;
                    colCover[j] = 1;
                    rowCover[i] = 1;
                }
            }
        }

        clearCovers(rowCover, colCover); // Reset cover vectors.

        return 3;
    }

    private static int hgStep3(final int[][] mask, final int[] colCover) {
        // What STEP 3 does:
        // Cover columns of starred zeros. Check if all columns are covered.

        for (int[] element : mask) {
            for (int j = 0; j < element.length; j++) {
                if (element[j] == 1) {
                    colCover[j] = 1;
                }
            }
        }

        int count = 0;
        for (int element : colCover) {
            count = count + element;
        }

        // Should be cost.length but ok, because mask has same dimensions.
        return count >= mask.length ? 7 : 4;
    }

    private static int hgStep4(final double[][] cost, final int[][] mask, final int[] rowCover, final int[] colCover, final int[] zeroRC) {
        // What STEP 4 does:
        // Find an uncovered zero in cost and prime it (if none go to step 6).
        // Check for star in same row:
        // if yes, cover the row and uncover the star's column. Repeat until no
        // uncovered zeros are left
        // and go to step 6. If not, save location of primed zero and go to step
        // 5.

        int step = 4; // Will always be changed, because the while loop will do at least one iteration.

        int[] rowCol = new int[2]; // Holds row and col of uncovered zero.
        boolean done = false;
        while (!done) {
            rowCol = findUncoveredZero(rowCol, cost, rowCover, colCover);
            if (rowCol[0] == -1) {
                done = true;
                step = 6;
            } else {
                mask[rowCol[0]][rowCol[1]] = 2; // Prime the found uncovered
                // zero.

                boolean starInRow = false;
                for (int j = 0; j < mask[rowCol[0]].length; j++) {
                    if (mask[rowCol[0]][j] == 1) { // If there is a star in the
                        // same row...
                        starInRow = true;
                        rowCol[1] = j; // remember its column.
                    }
                }

                if (starInRow) {
                    rowCover[rowCol[0]] = 1; // Cover the star's row.
                    colCover[rowCol[1]] = 0; // Uncover its column.
                } else {
                    zeroRC[0] = rowCol[0]; // Save row of primed zero.
                    zeroRC[1] = rowCol[1]; // Save column of primed zero.
                    done = true;
                    step = 5;
                }
            }
        }
        return step;
    }

    // Aux 1 for hgStep4.
    private static int[] findUncoveredZero(final int[] rowCol, final double[][] cost, final int[] rowCover, final int[] colCover) {
        rowCol[0] = -1; // Just a check value. Not a real index.
        rowCol[1] = 0;

        int i = 0;
        boolean done = false;
        while (!done) {
            int j = 0;
            while (j < cost[i].length) {
                if (doubleEqualsZero(cost[i][j]) && rowCover[i] == 0 && colCover[j] == 0) {
                    rowCol[0] = i;
                    rowCol[1] = j;
                    done = true;
                }
                j = j + 1;
            }
            i = i + 1;
            if (i >= cost.length) {
                done = true;
            }
        }
        return rowCol;
    }

    private static int hgStep5(final int[][] mask, final int[] rowCover, final int[] colCover, final int[] zeroRC) {
        // What STEP 5 does:
        // Construct series of alternating primes and stars. Start with prime
        // from step 4.
        // Take star in the same column. Next take prime in the same row as the
        // star. Finish
        // at a prime with no star in its column. Unstar all stars and star the
        // primes of the
        // series. Erasy any other primes. Reset covers. Go to step 3.

        int count = 0; // Counts rows of the path matrix.
        int[][] path = new int[mask[0].length * mask.length][2]; // Path matrix
        // (stores row and col).
        path[count][0] = zeroRC[0]; // Row of last prime.
        path[count][1] = zeroRC[1]; // Column of last prime.

        boolean done = false;
        while (!done) {
            int r = findStarInCol(mask, path[count][1]);
            if (r >= 0) {
                count = count + 1;
                path[count][0] = r; // Row of starred zero.
                path[count][1] = path[count - 1][1]; // Column of starred zero.
            } else {
                done = true;
            }

            if (!done) {
                int c = findPrimeInRow(mask, path[count][0]);
                count = count + 1;
                path[count][0] = path[count - 1][0]; // Row of primed zero.
                path[count][1] = c; // Col of primed zero.
            }
        }

        convertPath(mask, path, count);
        clearCovers(rowCover, colCover);
        erasePrimes(mask);

        return 3;
    }

    // Aux 1 for hgStep5.
    private static int findStarInCol(final int[][] mask, final int col) {
        int r = -1; // Again this is a check value.
        for (int i = 0; i < mask.length; i++) {
            if (mask[i][col] == 1) {
                r = i;
            }
        }
        return r;
    }

    // Aux 2 for hgStep5.
    private static int findPrimeInRow(final int[][] mask, final int row) {
        int c = -1;
        for (int j = 0; j < mask[row].length; j++) {
            if (mask[row][j] == 2) {
                c = j;
            }
        }
        return c;
    }

    // Aux 3 for hgStep5.
    private static void convertPath(final int[][] mask, final int[][] path, final int count) {
        for (int i = 0; i <= count; i++) {
            if (mask[path[i][0]][path[i][1]] == 1) {
                mask[path[i][0]][path[i][1]] = 0;
            } else {
                mask[path[i][0]][path[i][1]] = 1;
            }
        }
    }

    // Aux 4 for hgStep5.
    private static void erasePrimes(final int[][] mask) {
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[i].length; j++) {
                if (mask[i][j] == 2) {
                    mask[i][j] = 0;
                }
            }
        }
    }

    // Aux 5 for hgStep5 (and not only).
    private static void clearCovers(final int[] rowCover, final int[] colCover) {
        for (int i = 0; i < rowCover.length; i++) {
            rowCover[i] = 0;
        }
        for (int j = 0; j < colCover.length; j++) {
            colCover[j] = 0;
        }
    }

    private static int hgStep6(final double[][] cost, final int[] rowCover, final int[] colCover, final double maxCost) {
        // What STEP 6 does:
        // Find smallest uncovered value in cost: a. Add it to every element of
        // covered rows
        // b. Subtract it from every element of uncovered columns. Go to step 4.

        double minval = findSmallest(cost, rowCover, colCover, maxCost);
        for (int i = 0; i < rowCover.length; i++) {
            for (int j = 0; j < colCover.length; j++) {
                if (rowCover[i] == 1) {
                    cost[i][j] = cost[i][j] + minval;
                }
                if (colCover[j] == 0) {
                    cost[i][j] = cost[i][j] - minval;
                }
            }
        }

        return 4;
    }

    // Aux 1 for hgStep6.
    private static double findSmallest(final double[][] cost, final int[] rowCover, final int[] colCover, final double maxCost) {
        double minval = maxCost; // There cannot be a larger cost than this.
        for (int i = 0; i < cost.length; i++) { // Now find the smallest
            // uncovered value.
            for (int j = 0; j < cost[i].length; j++) {
                if (rowCover[i] == 0 && colCover[j] == 0 && minval > cost[i][j]) {
                    minval = cost[i][j];
                }
            }
        }
        return minval;
    }
}
