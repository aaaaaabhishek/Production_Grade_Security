package com.example.customSe.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
public class SignupDto {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String username;
    private String secretKey;
    private Set<String> roles;
}
