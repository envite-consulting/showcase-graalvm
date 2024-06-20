package de.envite.lecture.graalvm.utilizer;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class LoadTestStatus {
    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean isRunning() {
        return running.get();
    }

    public void setStatus(boolean value) {
        running.set(value);
    }
}
