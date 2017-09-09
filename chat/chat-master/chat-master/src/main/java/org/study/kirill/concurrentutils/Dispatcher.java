package org.study.kirill.concurrentutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.kirill.Exeptions.DispatcherException;

public class Dispatcher implements Stoppable {

    private static Logger log = LoggerFactory.getLogger("dispatcher");
    private Channel<Stoppable> channel;
    private ThreadPool threadPool;
    private boolean status;
    private Thread dispatcher;

    public Dispatcher(Channel<Stoppable> chan, ThreadPool threadPool) {
        this.channel = chan;
        this.threadPool = threadPool;
        this.status = true;
        this.dispatcher = new Thread(this);
    }


    @Override
    public void run() {
        while (status) {
            Stoppable task = channel.get();
            threadPool.execute(task);
        }
    }

    public void start() {
        dispatcher.setDaemon(true);
        dispatcher.setName(Dispatcher.class.getSimpleName());
        dispatcher.start();
    }

    @Override
    public void stop() throws DispatcherException {

        if (status) {
            status = false;
            dispatcher.interrupt();
            log.info("dispatcher is stopped");
        } else throw new DispatcherException("Trial to stop not active dispatcher");
    }
}

