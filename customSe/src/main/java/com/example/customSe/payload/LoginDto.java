package com.example.customSe.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public LoginDto setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
        return this;
    }

    private  String usernameOrEmail;

    public String getPassword() {
        return password;
    }

    public LoginDto setPassword(String password) {
        this.password = password;
        return this;
    }

    private String password;
}
