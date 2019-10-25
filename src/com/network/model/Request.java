package com.network.model;

import java.io.Serializable;

public class Request<T> implements Serializable {

    private static final long serialversionUID = 432425412423L;

    public Type type;
    public Session session;
    public T data;

    public static enum Type {
        LOGIN(1), SEND_FILE(2);
        private int id;

        private Type(int id) {
            this.id = id;
        }
    }
}
