package com.dataprotection.dataprotection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Personal Data Storage & File Management System API is running.";
    }
}
