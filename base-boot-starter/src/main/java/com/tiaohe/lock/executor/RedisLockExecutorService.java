package com.tiaohe.lock.executor;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class RedisLockExecutorService extends ThreadPoolExecutor {
    public RedisLockExecutorService(int corePoolSize, int maxPoolSize, long keepAliveTime, int queueSize,
                                    ThreadFactory threadFactory, RejectedExecutionHandler rejectedHandler) {
        super(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                threadFactory != null ? threadFactory : Executors.defaultThreadFactory(),
                rejectedHandler != null ? rejectedHandler : new ThreadPoolExecutor.AbortPolicy());
    }
    @Override
    public void execute(Runnable command) {
        super.execute(command); // 调用父类的 execute 方法
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (t == null && r instanceof Future<?>) {
            Future<?> future = (Future<?>) r;
            try {
                future.get(); // 触发异常
            } catch (ExecutionException e) {
                logError("线程池任务执行异常", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logError("线程池任务被中断", e);
            } catch (Exception e) {
                logError("线程池任务执行异常", e);
            }
        } else if (t != null) {
            logError("线程池任务执行异常", t);
        }
    }

    private void logError(String message, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        log.error("{}: {}", message, stringWriter);
    }
}
