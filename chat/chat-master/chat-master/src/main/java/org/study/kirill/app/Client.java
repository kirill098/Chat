package org.study.kirill.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import org.study.kirill.ChatMessage;
import org.study.kirill.ClientGUI.ClientApp;
import org.study.kirill.Exeptions.*;

public class Client {
    private static Logger log = LoggerFactory.getLogger(Client.class.getSimpleName());

    private static final String STOP_MSG = "@exit";
    private static final String CTRL_MSG = "ok";
    private ObjectInputStream objIn;
    private ObjectOutputStream objOut;
    private Socket fromServer;
    private String userName;
    private static final String HELLO_MSG = "#I'm fine";
    private ClientApp clientApp;
    private ServerListener serverListener;

    public Client(String host, String port, String name, ClientApp app) throws ClientException {

        try {

            log.info("Connection...");
            clientApp = app;
            clientApp.printReceivedMsg(new ChatMessage("CONNECTION..."));


            userName = name;
            fromServer = new Socket(host, Integer.parseInt(port));
            objOut = new ObjectOutputStream(this.fromServer.getOutputStream());
            objIn = new ObjectInputStream(this.fromServer.getInputStream());
            clientApp = app;
            getCtrlMsg();
            sendHelloMsg();
            serverListener = new ServerListener();
            serverListener.start();

            log.info("Connected!");

        } catch (IOException e) {
            throw new ClientException("Client constructor: Socket error:");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public Client(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
        fromServer = socket;
        objOut = oos;
        objIn = ois;

    }


    private void getCtrlMsg() throws ClientException, IOException, ClassNotFoundException {
        ChatMessage fromServerCtrlMsg = (ChatMessage) objIn.readObject();
        log.info("ctrl msh is received: " + fromServerCtrlMsg.getMessage());
        if (!(fromServerCtrlMsg.getMessage().equals(CTRL_MSG))) {
            throw new ClientException("Client constructor: invalid control message=", fromServerCtrlMsg.getMessage());
        } else log.info("Ctrl msg is right");

    }


    //для GUI
    public void sendMsg(String msg) throws IOException {
        if (!Objects.equals(msg, "")) {
            ChatMessage chatMessage = new ChatMessage(userName, msg);
            objOut.writeObject(chatMessage);
        }
    }


    /**
     * While client has not sent  STOP_MSG he can send  clientMsg to Server
     */
    private void sendMessages() throws ClientException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Dear User, to exit this app use command @exit");
        String clientMsg = "";
        try {
            while (!clientMsg.equals(STOP_MSG)) {
                clientMsg = bufferedReader.readLine();
                ChatMessage chatMessage = new ChatMessage(userName, clientMsg);
                objOut.writeObject(chatMessage);
                log.info("sent! ");
                ChatMessage confirmMsg = (ChatMessage) objIn.readObject();
                System.out.println(confirmMsg.getMessage());
                log.info("got! ");

            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new ClientException("Client sendMessage stream error: {}", e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void sendHelloMsg() throws IOException {
        ChatMessage confirmMsg = new ChatMessage(userName, HELLO_MSG);
        objOut.writeObject(confirmMsg);
    }

    /**
     * When client sent STOP_MSG or closed console/window/app this method is called.
     */
    public void shutDownClient() throws ClientException {
        try {

            fromServer.shutdownInput();
            fromServer.shutdownOutput();
            fromServer.close();
            serverListener.interrupt();

        } catch (IOException e) {
            throw new ClientException("Client shutDownClient: socket error{}", e);
        }
    }

    public Object getOutputStream() {
        return objOut;
    }


    public static void main(String[] args) {

        try {

            Client client = new Client(args[0], args[1], args[2], null);
            client.sendMessages();
            client.shutDownClient();
        } catch (ClientException e) {
            log.error("Client can't be created: ", e);
        }
    }

    class ServerListener extends Thread {

        public void run() {
            try {
                while (true) {
                    ChatMessage msg = (ChatMessage) objIn.readObject();
                    if (clientApp == null) {
                        System.out.println(msg);
                    } else {
                        clientApp.printReceivedMsg(msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error("Server is unconnected");
                if (clientApp != null) clientApp.connectionFailed();
                try {
                    clientApp.printReceivedMsg(new ChatMessage("TRY TO CONNECT AGAIN!"));
                    clientApp.connectionFailed();
                    shutDownClient();
                } catch (ClientException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}


