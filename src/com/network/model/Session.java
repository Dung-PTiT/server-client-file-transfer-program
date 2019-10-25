package com.network.model;

import java.io.Serializable;

public class Session implements Serializable {

    private static final long serialversionUID = 23745892374L;

    public boolean authenticated;
    public String sessionToken;
    public String clientIP;
}
