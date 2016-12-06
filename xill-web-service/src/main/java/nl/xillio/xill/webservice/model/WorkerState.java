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
package nl.xillio.xill.webservice.model;

/**
 * State model for a xill worker.
 */
public enum WorkerState {

    /**
     * The worker has been instantiated and the related robot compiled successfully.
     * Ready to accept a payload and run.
     * When the worker is started -> RUNNING
     */
    IDLE,

    /**
     * The worker is running and is not finished yet.
     * When the robot completes -> COMPLETED.
     */
    RUNNING,

    /**
     * The worker was running and received the abort signal.
     * When the runtime stops -> IDLE, or -> RUNTIME_ERROR if something went wrong.
     */
    ABORTING,

    /**
     * The Xill Runtime encountered an unrecoverable error and is unusable.
     * Terminal state.
     */
    RUNTIME_ERROR
}
