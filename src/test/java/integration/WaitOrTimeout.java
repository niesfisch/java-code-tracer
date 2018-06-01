package integration;

import java.util.Date;

public class WaitOrTimeout {

    private final Callback cb;
    private final int maxMillisWait;

    public void go() {
        final Date start = new Date();

        while (true) {
            if (cb.isTrue()) {
                return;
            }
            long elapsed = new Date().getTime() - start.getTime();
            if (elapsed >= maxMillisWait) {
                throw new RuntimeException("time out while waiting for condition");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public interface Callback {
        boolean isTrue();
    }

    public WaitOrTimeout(Callback cb, int maxMillisWait) {
        this.cb = cb;
        this.maxMillisWait = maxMillisWait;
    }
}
