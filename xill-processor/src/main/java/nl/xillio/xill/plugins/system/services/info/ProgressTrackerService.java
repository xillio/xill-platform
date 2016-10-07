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
package nl.xillio.xill.plugins.system.services.info;

import com.google.inject.Singleton;
import nl.xillio.xill.services.ProgressTracker;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class represents a {@link ProgressTracker} implementation that provides information about the progress of currently running robots.
 */
@Singleton
public class ProgressTrackerService implements ProgressTracker {

    private class Progress {
        private OnStopBehavior progressBarOnStopBehavior = OnStopBehavior.HIDE;
        private double robotProgress = -1;
    }

    private final HashMap<UUID, Progress> progressData = new HashMap<>();

    @Override
    public synchronized void setOnStopBehavior(UUID compilerSerialId, OnStopBehavior onStopBehavior) {
        get(compilerSerialId, true).progressBarOnStopBehavior = onStopBehavior;
    }

    @Override
    public synchronized OnStopBehavior getOnStopBehavior(UUID compilerSerialId) {
        Progress progress = get(compilerSerialId, false);
        if (progress == null) {
            return null;
        } else {
            return progress.progressBarOnStopBehavior;
        }
    }

    @Override
    public synchronized void setProgress(UUID compilerSerialId, double progress) {
        get(compilerSerialId, true).robotProgress = progress;
    }

    @Override
    public synchronized double getProgress(UUID compilerSerialId) {
        Progress progress = get(compilerSerialId, false);
        if (progress == null) {
            return -1;
        } else {
            return progress.robotProgress;
        }
    }

    @Override
    public synchronized boolean remove(UUID compilerSerialId) {
        if (progressData.containsKey(compilerSerialId)) {
            progressData.remove(compilerSerialId);
            return true;
        } else {
            return false;
        }
    }

    private Progress get(UUID compilerSerialId, boolean createIfNotExist) {
        if (compilerSerialId == null) {
            return null;
        }
        if (!progressData.containsKey(compilerSerialId)) {
            if (createIfNotExist) {
                Progress progress = new Progress();
                progressData.put(compilerSerialId, progress);
                return progress;
            } else {
                return null;
            }
        } else {
            return progressData.get(compilerSerialId);
        }
    }
}
