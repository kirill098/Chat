package org.study.kirill.concurrentutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.kirill.Exeptions.*;

import java.io.IOException;
import java.util.LinkedList;

/*
тут складируются все запущенные потоки
 При многократном использовании потоков для решения многочисленных задач,
    издержки создания потока распространяются на многие задачи.
В качестве бонуса, поскольку поток уже существует, когда прибывает запрос, задержка,
    произошедшая из-за создания потока, устраняется.
Таким образом, запрос может быть обработан немедленно,
    что делает приложение более быстрореагирующим.
 */

/**
 * freeWorkers -  свободные рабочие
 * allWorkers - база работников
 * maxSize - макс. число свободных работников
 */

public class ThreadPool {
    private static Logger log = LoggerFactory.getLogger("threadPool");
    private final Object lock = new Object();
    private final LinkedList<Stoppable> allWorkers;
    private final Channel<Stoppable> freeWorkers;
    private final int maxSize;

    /**
     */
    public ThreadPool(int maxSize) {
        this.maxSize = maxSize;
        this.freeWorkers = new Channel<>(maxSize);
        this.allWorkers = new LinkedList<>();
        WorkerThread worker = new WorkerThread(this); //рабочие потоки
        this.allWorkers.addLast(worker);
        this.freeWorkers.put(worker);
    }

    /**
     *
     *
     * @param task for execution for free workerTread
     *             if there is no freeWorkers, but allWorkers.size != max, create new WorkerThread,
     *             put it in allWorkers, freeWorkers
     */
    void execute(Stoppable task) {
        synchronized (lock) {
            if (freeWorkers.getSize() == 0) {
                if (allWorkers.size() < maxSize) {
                    WorkerThread worker = new WorkerThread(this);
                    allWorkers.addLast(worker);
                    freeWorkers.put(worker);
                }
            }
            try {
                ((WorkerThread) freeWorkers.get()).execute(task);
            } catch (WorkerThreadExсeption e) {
                log.error(" Oops!:{}", e);
            }
        }
    }

    /**
     * put @param workerThread to freeWorkers (кладем свободного worker в очередь)
     */
    void onTaskCompleted(WorkerThread workerThread) {
        freeWorkers.put(workerThread);
    }

    public void stop() throws TreadPoolException {
        while (allWorkers.size() > 0) {
            try {
                allWorkers.removeFirst().stop();
            } catch (DispatcherException | IOException | SessionException | IllegalAccessException e) {
                throw new TreadPoolException("ThreadPool.stop() is failed ");
            }
        }
        log.info("TreadPool is stopped");
    }

}
