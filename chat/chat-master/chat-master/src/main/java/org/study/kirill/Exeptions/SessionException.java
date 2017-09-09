package org.study.kirill.Exeptions;

import java.io.IOException;


public class SessionException extends Throwable {
    private String clName;

    public SessionException(String msg, String clName) {
        super(msg);
        this.clName = clName;
    }
    public SessionException(String msg) {
        super(msg);
    }

    public SessionException(String msg, IOException e) {
        super(msg, e);
    }
}
