package com.network.model;

import java.io.Serializable;

public class LoginFormData implements Serializable {

    private static final long serialversionUID = 312312313123L;

    public String username;
    public String password;

    public LoginFormData(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
