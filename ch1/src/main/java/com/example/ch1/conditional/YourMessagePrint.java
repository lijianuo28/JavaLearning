package com.example.ch1.conditional;

public class YourMessagePrint implements MessagePrint{
    @Override
    public String showMessage() {
        return "test.properties 文件不存在！";
    }
}
