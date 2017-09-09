package org.study.kirill;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class ChatHistory implements Serializable {
    private List<ChatMessage> history;
    private final int maxSize = 100;

    public ChatHistory() {
        this.history = new ArrayList<ChatMessage>(maxSize);
    }

    public void addMessage(ChatMessage msg) {
        if (this.history.size() > maxSize) {
            this.history.remove(0);
        }
        this.history.add(msg);
    }

    List<ChatMessage> getHistory() {
        return history;
    }


}
