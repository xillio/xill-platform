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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

/**
 * This class represents a {@link ProgressTracker} implementation that provides information about the progress of currently running robots.
 */
@Singleton
public class ProgressTrackerService implements ProgressTracker {

    private class ProgressInfo {
        private OnStopBehavior progressBarOnStopBehavior = OnStopBehavior.HIDE;
        private double currentProgress = -1; // Current progress (-1 if not set yet)
        private double startProgress = -1; // The first set progress (-1 if not set yet)
        private LocalDateTime startTime; // The datetime when the first progress was set (null if not set yet)
    }

    private final HashMap<UUID, ProgressInfo> progressData = new HashMap<>();

    @Override
    public void setOnStopBehavior(UUID compilerSerialId, OnStopBehavior onStopBehavior) {
        get(compilerSerialId, true).progressBarOnStopBehavior = onStopBehavior;
    }

    @Override
    public OnStopBehavior getOnStopBehavior(UUID compilerSerialId) {
        ProgressInfo progressInfo = get(compilerSerialId, false);
        if (progressInfo == null) {
            return null;
        } else {
            return progressInfo.progressBarOnStopBehavior;
        }
    }

    @Override
    public void setProgress(UUID compilerSerialId, double progress) {
        ProgressInfo progressInfo = get(compilerSerialId, true);
        progressInfo.currentProgress = progress;
        if (progressInfo.startProgress == -1) {
            progressInfo.startProgress = progress;
            progressInfo.startTime = LocalDateTime.now();
        }
    }

    @Override
    public Double getProgress(UUID compilerSerialId) {
        ProgressInfo progressInfo = get(compilerSerialId, false);
        if (progressInfo == null) {
            return null;
        } else {
            return progressInfo.currentProgress;
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

    @Override
    public Duration getRemainingTime(UUID compilerSerialId) {
        ProgressInfo progressInfo = get(compilerSerialId, false);
        if (progressInfo == null) {
            return null; // CSID not found
        }

        if (progressInfo.currentProgress == -1 || progressInfo.startProgress == -1 || progressInfo.currentProgress <= progressInfo.startProgress) {
            return null; // Cannot estimate remaining time
        }

        if (progressInfo.currentProgress >= 1) {
            return Duration.ZERO;
        }

        // Compute remaining time
        long elapsed = (long) ((progressInfo.currentProgress - progressInfo.startProgress) * 100); // Compute percent elapsed
        long remains = (long) ((1 - progressInfo.currentProgress)*100); // Compute how many percent remains
        return Duration.between(progressInfo.startTime, LocalDateTime.now()).dividedBy(elapsed).multipliedBy(remains);
    }

    private synchronized ProgressInfo get(UUID compilerSerialId, boolean createIfNotExist) {
        if (compilerSerialId == null) {
            return null;
        }
        if (!progressData.containsKey(compilerSerialId)) {
            if (createIfNotExist) {
                ProgressInfo progressInfo = new ProgressInfo();
                progressData.put(compilerSerialId, progressInfo);
                return progressInfo;
            } else {
                return null;
            }
        } else {
            return progressData.get(compilerSerialId);
        }
    }
}
