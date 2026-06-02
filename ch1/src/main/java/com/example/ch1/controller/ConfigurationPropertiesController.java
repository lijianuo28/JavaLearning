package com.example.ch1.controller;

import com.example.ch1.model.StudentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigurationPropertiesController {
    @Autowired
    StudentProperties studentProperties;

    @GetMapping("/testConfigurationProperties")
    public String testConfigurationProperties() {
        return studentProperties.toString();
    }
}
