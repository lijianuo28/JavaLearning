package com.example.ch1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class EnvReaderConfigController{
    @Autowired
    private Environment env;
    @GetMapping("/testEnv")
    public String testEnv() {
        return "方法一：" + env.getProperty("test.msg");
    }
}
