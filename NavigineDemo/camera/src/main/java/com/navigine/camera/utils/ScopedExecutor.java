package com.navigine.camera.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScopedExecutor implements Executor {

    private Executor executor = null;
    private AtomicBoolean shutdown = new AtomicBoolean();

    public ScopedExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown.get()) return;

        executor.execute(() -> {
            if (shutdown.get()) return;
            command.run();
        });
    }

    public void shutdown() {
        shutdown.set(true);
    }
}
