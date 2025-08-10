package com.toeicify.toeic.controller;

import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/hello")
@RequiredArgsConstructor
public class HelloController {
    private final UserRepository userRepository;
    @GetMapping
    public String hello() {
        userRepository.ping();
        return "Hello World";
    }
}
