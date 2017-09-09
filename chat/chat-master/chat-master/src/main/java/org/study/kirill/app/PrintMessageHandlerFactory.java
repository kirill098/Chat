package org.study.kirill.app;

import org.study.kirill.netutils.MessageHandler;
import org.study.kirill.netutils.MessageHandlerFactory;

public class PrintMessageHandlerFactory implements MessageHandlerFactory{
    /**
     *  создается и возвращается экземпляр класса PrintMessageHandler
     * @return
     */
    @Override
    public MessageHandler create() {
        return new PrintMessageHandler();
    }
}
