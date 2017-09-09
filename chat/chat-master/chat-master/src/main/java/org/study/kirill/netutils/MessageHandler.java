package org.study.kirill.netutils;

import org.study.kirill.ChatMessage;

public interface MessageHandler {
    void handle (String name, String msg);
    void handle (ChatMessage msg, Session session);
}
