package com.example.ch1.conditional;

public class MyMessagePrint implements MessagePrint{
    @Override
    public String showMessage() {
        return "test.properties 文件存在。";
    }
}
