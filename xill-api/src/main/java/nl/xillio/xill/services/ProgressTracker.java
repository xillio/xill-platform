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
package nl.xillio.xill.services;

import java.time.Duration;
import java.util.UUID;

/**
 * This interface provides information about the progress of currently running robots.
 */
public interface ProgressTracker extends XillService {
    /**
     * Defines what will progress bar do after the robot is stopped (i.e. manually stopped or finished)
     */
    enum OnStopBehavior {
        /**
         * N/A - ignore this settings; indicate transferring just progress value without any stop behavior settings
         */
        NA,
        /**
         * Leave the progress bar as it is when the robot is stopped
         */
        LEAVE,
        /**
         * Set the progress bar to zero when the robot is stopped
         */
        ZERO,
        /**
         * Hide the progress bar when the robot is stopped
         */
        HIDE
    }

    /**
     * Setter for onStopBehaviour value.
     *
     * Does nothing when a non-existent {@code compilerSerialId} is given.
     *
     * @param compilerSerialId the running robot identificator
     * @param onStopBehavior the onStopBehaviour value
     */
    void setOnStopBehavior(UUID compilerSerialId, OnStopBehavior onStopBehavior);

    /**
     * Getter for onStopBehaviour value.
     *
     * @param compilerSerialId the running robot identificator
     * @return the onStopBehaviour value
     */
    OnStopBehavior getOnStopBehavior(UUID compilerSerialId);

    /**
     * Setter for progress value.
     *
     * Does nothing when a non-existent {@code compilerSerialId} is given.
     *
     * @param compilerSerialId the running robot identificator
     * @param progress the current progress (0-1 or <0 for hiding the progress bar)
     */
    void setProgress(UUID compilerSerialId, double progress);

    /**
     * Getter for progress value.
     *
     * @return the current progress value between 0-1 (including) or negative number or null if CSID is not found
     */
    Double getProgress(UUID compilerSerialId);

    /**
     * Removes progress info from the map.
     *
     * @param compilerSerialId the running robot identificator
     * @return true if the item was removed, false if the given item was not found in a map
     */
    boolean remove(UUID compilerSerialId);

    /**
     * Computes the estimation of remaining time for given CSID.
     *
     * @param compilerSerialId the running robot identificator
     * @return positive remaining time till the end of the robot run or null in case that the CSID is not found or there is not enough progress information for computing the estimation of the remaining time
     */
    Duration getRemainingTime(UUID compilerSerialId);
}
