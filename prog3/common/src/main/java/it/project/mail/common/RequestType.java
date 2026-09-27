package it.project.mail.common;

import java.io.Serial;
import java.io.Serializable;

public final class RequestType implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int LOGIN = 1;
    public static final int SEND = 2;
    public static final int RECEIVE = 3;
    public static final int DELETE = 4;
    public static final int DISCONNECT = 5;

    private final String email;
    private final int type;

    public RequestType(String email, int type) {
        this.email = email;
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public int getType() {
        return type;
    }
}
