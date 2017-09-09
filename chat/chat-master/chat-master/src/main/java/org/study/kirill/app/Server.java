package org.study.kirill.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.kirill.ChatHistory;
import org.study.kirill.UserList;
import org.study.kirill.concurrentutils.*;
import org.study.kirill.Exeptions.DispatcherException;
import org.study.kirill.Exeptions.TreadPoolException;
import org.study.kirill.netutils.Host;
import org.study.kirill.netutils.MessageHandlerFactory;

import java.rmi.ServerException;


public class Server {
    private static Logger log = LoggerFactory.getLogger(Server.class.getSimpleName());

    private static int maxSessionNum;
    private static int portNumber;
    private static Class classMHFactory;
    private static MessageHandlerFactory mHFactory;
    private static Channel<Stoppable> channel;
    private static Host host;
    private static Dispatcher dispatcher;
    private static ThreadPool threadPool;
    private static UserList userList;
    private static ChatHistory chatHistory;

    public Server(String port, String maxSN, String className) {
        try {
            portNumber = Integer.parseInt(port);
            maxSessionNum = Integer.parseInt(maxSN);
            classMHFactory = Class.forName(className);
            mHFactory = (MessageHandlerFactory) classMHFactory.newInstance();
            channel = new Channel<>(maxSessionNum);
            chatHistory= new ChatHistory();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("{}", e);
        }
    }

    public synchronized static UserList serverUserList() {

            return userList;

    }


    public void launch() throws ServerException {
        // shutdown-ловушка
        MyShutdownHook shutdownHook = new MyShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        userList = new UserList();
        host = new Host(portNumber, channel, mHFactory);
        host.start();
        threadPool = new ThreadPool(maxSessionNum);
        dispatcher = new Dispatcher(channel, threadPool); //
        dispatcher.start();
    }


    public static void main(String[] args) {
        Server server = new Server(args[0], args[1], args[2]);


        try {
            server.launch();
        } catch (ServerException e) {
            log.error("", e);
        }


    }

    private class MyShutdownHook extends Thread {
        public void run() {
            shutdown();
        }

    }

    public void shutdown() {
        try {

            log.info("Shutting down");
            host.stop();
            threadPool.stop();
            dispatcher.stop();

            log.info("Good night!");
        } catch (TreadPoolException | DispatcherException e1) {
            log.error("error of shutting down");
        }
    }

}




