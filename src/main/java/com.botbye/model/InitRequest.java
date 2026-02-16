package com.botbye.model;

import java.io.*;

public class InitRequest implements Serializable {
    private String serverKey;

    public InitRequest(String serverKey) {
        this.serverKey = serverKey;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }
}
