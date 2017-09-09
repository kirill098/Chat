package org.study.kirill.netutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.kirill.ChatMessage;
import org.study.kirill.Exeptions.SessionException;
import org.study.kirill.Exeptions.UserListException;
import org.study.kirill.app.Client;
import org.study.kirill.concurrentutils.Stoppable;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import static org.study.kirill.app.Server.serverUserList;


/**
 * /**
 * Input/ output streams for communication between Client and Server
 */
public class Session implements Stoppable {
    private Logger log = LoggerFactory.getLogger(Session.class.getSimpleName());

    private static final String CTRL_MSG = "ok";
    private static final String STOP_MSG = "@exit";
    private static final String HELLO_MSG = "#I'm fine";

    private Socket fromClientSocket;
    private MessageHandler messageHandler;

    private ObjectInputStream objIn;
    private ObjectOutputStream objOutForMyClient;

    private final Object lock = new Object();
    private String userName;
    private String LOG_ERR_MSG = "User with this name already exists :)";

    Session(Socket socket, MessageHandler messageHandler) {

        try {
            this.fromClientSocket = socket;
            this.messageHandler = messageHandler;
            InputStream in = fromClientSocket.getInputStream();
            OutputStream out = fromClientSocket.getOutputStream();
            objIn = new ObjectInputStream(in);
            objOutForMyClient = new ObjectOutputStream(out);
        } catch (IOException e) {
            log.error("Session constructor is failed");
        }

    }


    @Override
    public void run() {

        try {
            pingClient();

            String receivedMsg = "";
            while (!receivedMsg.equals(STOP_MSG)) {
                ChatMessage chatMessage;
                chatMessage = (ChatMessage) objIn.readObject();
                receivedMsg = chatMessage.getMessage();
                log.info("[{}]", receivedMsg);
                if (!receivedMsg.equals(STOP_MSG))
                    messageHandler.handle(chatMessage, this);
                else
                    messageHandler.handle(new ChatMessage(String.format("[%s] left chat room", userName)), this);
            }
            stop();
        } catch (IOException | ClassNotFoundException e) {
            log.error("error of readObject messages in Session.run()");
        } catch (UserListException e) {
            try {
                objOutForMyClient.writeObject(new ChatMessage(LOG_ERR_MSG));
                closeSession();
            } catch (IOException | SessionException e1) {
                e1.printStackTrace();
            }
        } catch (SessionException e) {
            e.printStackTrace();
        }


    }


    private void pingClient() throws UserListException {
        try {
            ChatMessage ctrlMessage = new ChatMessage(CTRL_MSG);
            objOutForMyClient.writeObject(ctrlMessage);

            ChatMessage helloMsg;
            helloMsg = (ChatMessage) objIn.readObject();

            userName = helloMsg.getUserName();
            if (!helloMsg.getMessage().equals(HELLO_MSG)) {
                throw new SessionException("Wrong value: Hello_msg ");
            } else {
                serverUserList().addUser(userName, fromClientSocket, objOutForMyClient, objIn);
                messageHandler.handle(new ChatMessage(String.format("[%s] is connected", userName)), this);
                log.info("[{}] is connected", helloMsg.getUserName());
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error(" stream's error in ping client", e);
        } catch (SessionException e1) {
            log.error("ping is failed: ", e1);
        }

    }


    public void broadcast(ArrayList<Client> clientsArrayList, ChatMessage message) {
        try {
            ObjectOutputStream objOut;

            for (Client client : clientsArrayList) {
                objOut = (ObjectOutputStream) client.getOutputStream();
                if (client.getOutputStream() != objOutForMyClient) //рассылаем всем, кроме отправляющего клиента
                    objOut.writeObject(message);
            }
        } catch (SocketException e) {
            log.info("[{}] is disconnected", userName);
            serverUserList().deleteUser(userName);
            messageHandler.handle(new ChatMessage(String.format("[System]\tuser [%s] has been disconnected", userName)), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Session stops when it's necessary (Incorrect exit or STOP_MSG from the client)
     */
    @Override
    public void stop() throws SessionException {
        serverUserList().deleteUser(userName);

        synchronized (lock) {
            if (fromClientSocket == null) {
                // if (fromClientSocket.isClosed()) throw new SessionException("Socket is closed ");
                try {

                    objOutForMyClient.writeObject(new ChatMessage("Good bye, my dear!"));
                    assert fromClientSocket != null;
                    log.info("[{}] is disconnected", userName);
                    serverUserList().deleteUser(userName);
                    messageHandler.handle(new ChatMessage(String.format("[System]\tuser [%s] has been disconnected", userName)), this);
                    // messageHandler(new ChatMessage(String.format("[System]\tuser [%s] has been disconnected", userName)), this);
                    fromClientSocket.shutdownInput();
                    fromClientSocket.shutdownOutput();
                    fromClientSocket.close();
                    objIn.close();
                    objOutForMyClient.close();
                    log.info("Session with [{}] was stopped");
                    lock.notifyAll();
                } catch (IOException e) {
                    throw new SessionException("Session stop(): threads error: {}", e);
                }

            }
        }

    }

    private void closeSession() throws SessionException {
        try {
            synchronized (lock) {
                fromClientSocket.shutdownInput();
                fromClientSocket.shutdownOutput();
                fromClientSocket.close();
                objIn.close();
                objOutForMyClient.close();
                log.info("Failed session was stopped");
                lock.notifyAll();
            }
        } catch (IOException e) {
            throw new SessionException("Session stop(): threads error: {}", e);
        }
    }
}