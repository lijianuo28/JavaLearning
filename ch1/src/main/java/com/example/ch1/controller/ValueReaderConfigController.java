package com.example.ch1.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class ValueReaderConfigController{
    @Value("${test.msg}")
    private String msg;
    @GetMapping("/testValue")
    public String testValue() {
        return "方法二：" + msg;
    }
}
