package nl.xillio.xill.webservice.model;

/**
 * State model for a xill worker.
 */
public enum XillWorkerState {

    /**
     * The worker has been created, but the robot hasn't been compiled yet.
     */
    NEW,

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
     * When the runtime stops -> IDLE, or previous state if the robot
     * was not running.
     */
    ABORTING,

    /**
     * The robot related to this worker is being compiled.
     * When compilation ends -> IDLE or COMPILATION_ERROR
     * depending on the outcome of the compilation
     */
    COMPILING,

    /**
     * The robot does not compile and cannot run.
     * Terminal state.
     */
    COMPILATION_ERROR,

    /**
     * The Xill Runtime encountered an unrecoverable error and is unusable.
     * Terminal state.
     */
    RUNTIME_ERROR,

    /**
     * The robot has completed its execution and the result is ready to be queried.
     * When the result is retrieved -> IDLE
     */
    COMPLETED
}
