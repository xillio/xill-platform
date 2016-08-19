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
package nl.xillio.xill.debugging;

/**
 * This class represents a wrapper for robot progress information.
 * This class is used for both data transferring and storing.
 */
public class ProgressInfo implements nl.xillio.xill.api.ProgressInfo {

    private double progress = -1; // Value -1 means that it's not used (i.e. progress bar is hidden) - as default state
    private OnStopBehavior onStopBehavior = OnStopBehavior.HIDE;

    public ProgressInfo() {
    }

    public ProgressInfo(double progress) {
        this.progress = progress;
        onStopBehavior = OnStopBehavior.NA;
    }

    /**
     * Setter for onStopBehavior
     *
     * @param onStopBehavior the specification what to do after robot is stopped
     */
    public void setOnStopBehaviour(OnStopBehavior onStopBehavior) {
        this.onStopBehavior = onStopBehavior;
    }

    @Override
    public void setProgress(double progress) {
        this.progress = progress;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public OnStopBehavior getOnStopBehavior() {
        return onStopBehavior;
    }
}
