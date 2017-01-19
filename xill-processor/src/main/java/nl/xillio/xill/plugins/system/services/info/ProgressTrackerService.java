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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a {@link ProgressTracker} implementation that provides information about the progress of
 * currently running robots.
 */
@Singleton
public class ProgressTrackerService implements ProgressTracker {

    private final Map<UUID, ProgressInfo> progressData = new ConcurrentHashMap<>();
    private static final ProgressInfo EMPTY_PROGRESS_INFO = new ProgressInfo() {
        @Override
        OnStopBehavior getProgressBarOnStopBehavior() {
            return OnStopBehavior.NA;
        }

        @Override
        void setProgressBarOnStopBehavior(OnStopBehavior progressBarOnStopBehavior) {}

        @Override
        Double getCurrentProgress() {
            return null;
        }

        @Override
        void update(Double progress) {}

        @Override
        Duration getRemainingTime() {
            return null;
        }

    };

    @Override
    public void setOnStopBehavior(UUID compilerSerialId, OnStopBehavior onStopBehavior) {
        getOrCreate(compilerSerialId).setProgressBarOnStopBehavior(onStopBehavior);
    }

    @Override
    public OnStopBehavior getOnStopBehavior(UUID compilerSerialId) {
        return get(compilerSerialId).getProgressBarOnStopBehavior();
    }

    @Override
    public void setProgress(UUID compilerSerialId, double progress) {
        getOrCreate(compilerSerialId).update(progress);
    }

    @Override
    public Double getProgress(UUID compilerSerialId) {
        return get(compilerSerialId).getCurrentProgress();
    }

    @Override
    public boolean remove(UUID compilerSerialId) {
        // Concurrent hashmap never contains null key values, so remove returning a null value means that the entry is not found.
        return progressData.remove(compilerSerialId) != null;
    }

    @Override
    public Duration getRemainingTime(UUID compilerSerialId) {
        return get(compilerSerialId).getRemainingTime();
    }

    private ProgressInfo get(UUID compilerSerialId) {
        return compilerSerialId != null && progressData.containsKey(compilerSerialId)
                ? progressData.get(compilerSerialId) : EMPTY_PROGRESS_INFO;
    }

    private ProgressInfo getOrCreate(UUID compilerSerialId) {
        if (compilerSerialId == null) {
            return EMPTY_PROGRESS_INFO;
        }
        add(compilerSerialId);
        return get(compilerSerialId);
    }

    private synchronized void add(UUID compilerSerialId) {
        if (!progressData.containsKey(compilerSerialId)) {
            ProgressInfo progressInfo = new ProgressInfo();
            progressData.put(compilerSerialId, progressInfo);
        }
    }

    private static class ProgressInfo {
        private OnStopBehavior progressBarOnStopBehavior = OnStopBehavior.HIDE;
        private double currentProgress = -1.0; // Current progress (<0 if not set yet)
        private double startProgress = -1.0; // The first set progress (<0 if not set yet)
        private LocalDateTime startTime; // The datetime when the first progress was set (null if not set yet)

        OnStopBehavior getProgressBarOnStopBehavior() {
            return progressBarOnStopBehavior;
        }

        void setProgressBarOnStopBehavior(OnStopBehavior progressBarOnStopBehavior) {
            this.progressBarOnStopBehavior = progressBarOnStopBehavior;
        }

        Double getCurrentProgress() {
            return currentProgress;
        }

        void update(Double progress) {
            if (Double.compare(startProgress, 0.0) < 0) {
                startProgress = progress;
                startTime = LocalDateTime.now();
            }
            currentProgress = progress;
        }
        private boolean isFinished() {
            return Double.compare(currentProgress, 1.0) >= 0;
        }

        private boolean isStarted() {
            return Double.compare(startProgress, 0.0) >= 0;
        }

        Duration getRemainingTime() {
            if (isFinished()) {
                return Duration.ZERO;
            }
            if (!isStarted()) {
                return null;
            }
            long passedTime = Duration.between(startTime, LocalDateTime.now()).toNanos();
            double remaining = (passedTime / currentProgress) - passedTime;
            return Duration.ofNanos(Math.round(remaining));
        }
    }

}
