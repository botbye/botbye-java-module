package com.botbye.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BotbyeUserInfo implements Serializable {
    private final String accountId;
    private final String username;
    private final String email;
    private final String phone;

    public BotbyeUserInfo(String accountId) {
        this(accountId, null, null, null);
    }

    public BotbyeUserInfo(String accountId, String username, String email, String phone) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
