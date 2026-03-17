package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class LoopDriver {

    private static final long LOOP_DELAY_MILLIS = 100L;
    private static final int HEARTBEAT_EVERY_N_LOOPS = 25;

    private final ClassA classA;

    public LoopDriver(ClassA classA) {
        this.classA = classA;
    }

    public void runForever() throws InterruptedException {
        long iteration = 0L;
        System.out.println("[sample] LoopDriver started. Running endless call chain...");
        while (true) {
            classA.methodA_1();
            iteration++;
            if (iteration % HEARTBEAT_EVERY_N_LOOPS == 0) {
                System.out.println("[sample] heartbeat - iterations=" + iteration);
            }
            Thread.sleep(LOOP_DELAY_MILLIS);
        }
    }
}

