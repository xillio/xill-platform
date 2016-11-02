package nl.xillio.xill.webservice.model;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robot. This execution can be interrupted on a different thread.
 *
 * @author Thomas Biesaart
 */
public class Worker {
    private final String robot;

    public Worker(String robot) {
        this.robot = robot;
    }

    public String getRobot() {
        return robot;
    }
}
