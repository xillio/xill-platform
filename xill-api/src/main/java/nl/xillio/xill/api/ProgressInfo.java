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
package nl.xillio.xill.api;

/**
 * This interface represents the ProgressInfo object that contains information about the robot progress
 */
public interface ProgressInfo {
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
     * Getter for OnStopBehavior
     *
     * @return the action what will progress bar do after the robot is stopped
     */
    OnStopBehavior getOnStopBehavior();

    /**
     * Setter for progress value
     *
     * @param progress the current progress (0-1 or <0 for hiding the progress bar)
     */
    void setProgress(double progress);

    /**
     * Getter for progress value
     *
     * @return the current progress value
     */
    double getProgress();


}
