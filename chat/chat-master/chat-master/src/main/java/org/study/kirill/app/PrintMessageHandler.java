package org.study.kirill.app;

import org.study.kirill.ChatMessage;
import org.study.kirill.netutils.MessageHandler;
import org.study.kirill.netutils.Session;

import static org.study.kirill.app.Server.serverUserList;


public class PrintMessageHandler implements MessageHandler{


    @Override
    public void handle(String name, String msg)
    {
        System.out.println(String.format("[%s] : %s", name, msg));

    }

    @Override
    public void handle(ChatMessage msg, Session session) {
        session.broadcast(serverUserList().getClientsList(), msg);

    }



}
