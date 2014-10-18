package de.hska.ld.core.util;

import org.springframework.scheduling.annotation.Async;

public class SpringAsyncExecutor implements AsyncExecutor {

    @Async
    @Override
    public void run(Runnable r) {
        r.run();
    }
}