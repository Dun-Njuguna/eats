package com.dunk.eats.models;


import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.InstanceIdResult;

public class Token {

    private String token;
    private boolean isServerToken;

    public Token() {
    }

    public Token(Task<InstanceIdResult> instanceId, boolean isServerToken) {
    }

    public Token(String token, boolean isServerToken) {
        this.token = token;
        this.isServerToken = isServerToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServerToken() {
        return isServerToken;
    }

    public void setServerToken(boolean serverToken) {
        isServerToken = serverToken;
    }
}
