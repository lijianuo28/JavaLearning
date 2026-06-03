package com.example.springwebstudy_1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class UserController {
    @GetMapping("/user")
    public String userPage(Model model) {
        List<String> names = Arrays.asList("张三", "李四", "王五");
        model.addAttribute("userList", names);
        model.addAttribute("title", "用户列表");
        return "user";  // 对应 templates/user.html
    }
}
