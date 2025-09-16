package com.example.customSe.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Userrr {
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String abhi(){
        return "abhi";
    }
}
