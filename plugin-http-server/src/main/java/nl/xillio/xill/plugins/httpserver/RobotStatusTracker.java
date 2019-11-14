package nl.xillio.xill.plugins.httpserver;

import java.util.function.Consumer;

public class RobotStatusTracker implements Consumer<Object> {
    private boolean stopped = false;

    @Override
    public void accept(Object robotStoppedAction) {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }
}
