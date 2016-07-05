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

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>This virtual list behaves more or less like a normal {@link ObservableList}, but instead of having all data in memory,
 * data is obtained from an external data provider. The VirtualObservableList will maintain a small cache and will smartly
 * prefetch data before it is required on-screen.
 * </p><p>
 * The virtual list is immutable and will not allow any changes to be made to it, nor persist changes to the {@link DataProvider}.
 * </p><p>
 * Usage:<br>
 * The update() or update(pos) function must be invoked in order to resync with the DataProvider, prior to fetching new data
 * using get().
 * </p><p>
 * This class is not a full implementation of the {@link ObservableList} interface. Methods that mutate data and methods or request
 * subsets of data are not supported for obvious reasons. Whenever an unsupported operation is invoked, an
 * UnsupportedOperationException is raised.
 * </p><p>
 * On top of the standard interface, this class adds some additional functionality in the form of applying filters to the
 * data shown in the list, and by providing an <code>update()</code> method which can be invoked to force the class to fetch the latest
 * data from the underlying DataProvider.
 * </p>
 * @author Ernst van Rheenen
 */
public class VirtualObservableList<S> implements ObservableList<S> {
    private static final Logger LOGGER = Log.get();

    // Data provider
    private DataProvider<S> provider; // Link with the actual datasource

    // External change listeners
    private Set<ListChangeListener<S>> changeListeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Filters
    private Map<?, Integer> filterCounts;  // Counters per filter

    // Cache and related variables
    private List<S> cache = new LinkedList<>();  // Internally data is cached for performance
    private int windowSize;      // Amount of items we want to cache
    private int windowPosition;  // Relative position of the cache to the actual dataprovider
    private int virtualSize;     // Size of actual dataset as provided by the dataprovider
    private int windowHorizon;   // windowSize / 4. When the cursor goes outside this horizon, we will start fetching new data.
    private int windowMid;       // windowSize / 2. Center of the window

    /**
     * Constructor taking a {@link DataProvider}, custom cache size and set of {@link Filter filters}.
     *
     * @param provider  the DataProvider that provides items from the actual datasource
     * @param cacheSize size of the cache in number of items. It is advisable to have a few hundred items in cache.
     * @param filters   filters indicating what data should be included in the result set
     */
    public VirtualObservableList(DataProvider<S> provider, int cacheSize, List<Filter> filters) {
        this.provider = provider;
        this.provider.setSearchFilters(filters);

        windowSize = cacheSize;
        virtualSize = provider.getSize();
        windowHorizon = Math.round(cacheSize / 4);
        windowMid = Math.round(cacheSize / 2);
        shiftWindow(0);
    }

    /**
     * Updates the {@link Filter filters} used for this virtual list. Filters indicate which data should be included in the result sets.
     *
     * @param filters the filters to be used
     */
    public void setFilters(List<Filter> filters) {
        provider.setSearchFilters(filters);
        //update();
    }

    /**
     * Forces an update of the data. The window will be shifted to the bottom of the data collection.
     * All registered listeners will automatically be notified.
     */
    public void update() {
        update(-1);
    }

    /**
     * Forces an update of the data for the specified row.
     */
    public void update(int position) {
        virtualSize = provider.getSize();

        if(position == -1) {
            position = virtualSize - 1;
        }

        shiftWindow(position);
        updateFilterCounts();
    }

    /**
     * Fetches a single row from the list.
     *
     * @param virtualIndex the virtual index of the row
     * @return a single row of type S
     */
    @Override
    public S get(int virtualIndex) {
        int index = virtualToIndex(virtualIndex);

        if (index < 0) {
            // Shift window to left
            shiftWindow(virtualIndex);
        } else if (index < windowSize) {
            // Consider moving window
            adjustWindow(virtualIndex);
        } else {
            // Shift window to right
            shiftWindow(virtualIndex);
        }

        if(index < 0 || index >= cache.size())
            return null;

        return getItemFromCache(index);
    }

    /**
     * Clears the list until the next time update() is invoked.
     */
    @Override
    public void clear() {
        virtualSize = 0;
        windowPosition = 0;
        cache.clear();
        notifyListeners();
    }

    /**
     * Fetches counters for the currently active data set, grouped per {@link Filter}.
     *
     * @return counters for the currently active data set
     */
    public Map<?, Integer> getFilterCounts() {
        return filterCounts;
    }

    /*
    Implementation of some standard functions
    */

    @Override
    public int size() {
        return virtualSize;
    }

    @Override
    public int indexOf(Object o) {
        return indexToVirtual(cache.indexOf(o));
    }

    @Override
    public boolean isEmpty() {
        return virtualSize <= 0;
    }

    @Override
    public boolean contains(Object o) {
        return cache.contains(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return cache.containsAll(c);
    }


    /*
    Listener registration & notification
     */

    @Override
    public void addListener(ListChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public void removeListener(ListChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void notifyListeners() {
        for (ListChangeListener listener : changeListeners) {
            listener.onChanged(new Change<S>(this) {

                @Override
                public boolean next() {
                    return false;
                }

                @Override
                public void reset() {

                }

                @Override
                public int getFrom() {
                    return 0;
                }

                @Override
                public int getTo() {
                    return virtualSize;
                }

                @Override
                public List<S> getRemoved() {
                    return null;
                }

                @Override
                protected int[] getPermutation() {
                    return new int[0];
                }
            });
        }
    }

    /**
     * Request to shift the window to the indicated index.
     *
     * @param virtualIndex the virtual index to which to move the window
     */
    private void shiftWindow(int virtualIndex) {
        if (virtualIndex > virtualSize) {
            virtualIndex = virtualSize;
        }

        final int start = virtualIndex - windowMid > 0 ? virtualIndex - windowMid : 0;
        final int end = start + windowSize > virtualSize ? virtualSize : start + windowSize;

        cache = start == end ? new LinkedList<>() : provider.getBatch(start, end);
        windowPosition = start;
        notifyListeners();
    }

    /**
     * Checks if the window needs to be shifted. By default we will prefetch new data when index &lt; 25% or
     * index &gt; 75% of the cached data.
     *
     * @param virtualIndex the virtual index to which to move the window
     */
    private void adjustWindow(int virtualIndex) {
        int mid = windowPosition + windowMid;
        if (virtualIndex > mid && virtualIndex < virtualSize - windowHorizon && virtualIndex - mid > windowHorizon) {
            shiftWindow(virtualIndex);
        } else if (virtualIndex < mid && virtualIndex > windowHorizon && mid - virtualIndex > windowHorizon) {
            shiftWindow(virtualIndex);
        }
    }

    /**
     * Updates the per-filter counters.
     */
    private void updateFilterCounts() {
        if (virtualSize > 0) {
            filterCounts = provider.getFilteredSizes();
        }
    }

    /**
     * Fetches a single item at provided cache index.
     *
     * @param index index at which the item sits in the cache
     * @return the requested item
     */
    private S getItemFromCache(int index) {
        return cache.isEmpty() ? null : cache.get(index);
    }

    /**
     * Converts a virtual index to a cache index. The returned value can be outside the cache boundaries and as such
     * does not represent a directly usable index.
     *
     * @param virtual the virtual index to be converted
     * @return the associated cache index
     */
    private int virtualToIndex(int virtual) {
        return virtual - windowPosition;
    }

    /**
     * Converts a cache index to a virtual index.
     *
     * @param index
     * @return
     */
    private int indexToVirtual(int index) {
        return windowPosition + index;
    }


    /*
    FUNCTIONS BELOW ARE ONLY STUBS
     */

    @Override
    public boolean add(Object o) {
        return false;
    }

    @Override
    public void addListener(InvalidationListener listener) {
    }

    @Override
    public void removeListener(InvalidationListener listener) {
    }


    /*
    FUNCTIONS BELOW HAVE NOT BEEN IMPLEMENTED AND WILL THROW AN ERROR WHEN INVOKED!
     */

    @Override
    public Iterator<S> iterator() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ListIterator<S> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<S> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<S> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public S set(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public S remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Object[] elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setAll(Object[] elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setAll(Collection col) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Object[] elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Object[] elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(int from, int to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public S[] toArray(Object[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public S[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

}
