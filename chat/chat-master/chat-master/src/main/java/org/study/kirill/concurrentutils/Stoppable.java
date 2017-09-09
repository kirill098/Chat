package org.study.kirill.concurrentutils;

import org.study.kirill.Exeptions.DispatcherException;
import org.study.kirill.Exeptions.SessionException;

import java.io.IOException;

public interface Stoppable extends Runnable {

    void stop() throws IllegalAccessException, SessionException, IOException, DispatcherException;
}
