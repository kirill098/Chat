package org.study.kirill.concurrentutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.kirill.Exeptions.*;
import java.io.IOException;
/*
import net.jcip.annotations.GuardedBy;
*/

/**
 * рабочие потоки
 * извлекает задачи из очереди и выполняет их. Если очередь пуста, то ожидает,
 * пока в очереди не появится новая задача.
 */
public class WorkerThread implements Stoppable {
    private static Logger log = LoggerFactory.getLogger("workerThread");
    private final Object lock = new Object();

    //@GuardedBy (lock)
    private final Thread workerThread;
    // @GuardedBy (lock)
    private final ThreadPool threadPool;
    // @GuardedBy (lock)
    private Runnable currentTask;
    private boolean status;

    WorkerThread(ThreadPool pool) {
        currentTask = null;
        status = true;
        threadPool = pool;
        workerThread = new Thread(this);
        workerThread.start();
    }

    /**
     * Если есть задачи на выполнение - выполняем, инача ждем
     * </code> currentTask.run()</code> - выполняется, когда пришла задача
     * В конце
     */
    @Override
    public void run() {
        synchronized (lock) {
            while (status) {
                while (currentTask == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        log.error("ошибка ожидания задач на выполнение");
                    }
                }
                try {
                    currentTask.run();
                } catch (Exception e) {
                    log.error(" currentTask.run() {}", e);
                } finally {
                    currentTask = null;
                    threadPool.onTaskCompleted(this);
                }
            }
        }
    }


    /**
     * set </code> task </> to </code> currentTask </>,
     * notify waiting threads.
     *
     */
    void execute(Stoppable task) throws WorkerThreadExсeption {
        synchronized (lock) {
            if (currentTask != null) throw new WorkerThreadExсeption("Method execute: currentTask != null");
            currentTask = task;
            lock.notifyAll();

        }
    }


    @Override
    public void stop() throws IllegalAccessException, SessionException, IOException {

        if (status) {
            status = false;
            workerThread.interrupt();
            currentTask = null;
            log.info("WorkerThread is stopped");
        }
    }
}




