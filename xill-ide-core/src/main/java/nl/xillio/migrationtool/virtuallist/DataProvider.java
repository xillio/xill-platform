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
package nl.xillio.migrationtool.virtuallist;

import java.util.HashMap;
import java.util.List;


/**
 * Generic interface for a class that provides data to a {@link VirtualObservableList}. The DataProvider provides a basic set of entrypoints
 * to clients to disclose contents of the underlying datasource. A DataProvider provides an generic wrapper around actual
 * data sources such as for instance a database.
 * <p>
 * @author Ernst van Rheenen
 */
public interface DataProvider<S> {

    /**
     * Requests the DataProvider to find out what the actual size is of the data collection it is wrapping.
     *
     * @return the actual size of the actual data collection
     */
    int getSize();

    /**
     * Returns a counter for each filter that has been defined. This way one can display split counters in a UI to
     * show how a dataset is distributed over various {@link Filter filters}.
     *
     * @return the size of each subset grouped per filter
     */
    HashMap<?, Integer> getFilteredSizes();

    /**
     * Retrieves specified rows from the data source. The data type returned is the one that is used at instantiation
     * of the DataProvider.
     *
     * @param start requested start point of the data set
     * @param end   requested end point of the data set
     * @return a list of values from the data source
     */
    List<S> getBatch(int start, int end);

    /**
     * Set the {@link Filter filters} that are to be applied by the DataProvider on the data set before returning
     * information through <code>getSize()</code> or <code>getBatch()</code>.
     *
     * @param filters the filters to be applied to the data
     */
    void setSearchFilters(List<Filter> filters);

}
