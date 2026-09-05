package com.lzc.mindaispringboot.controller;

import com.lzc.mindaispringboot.Dto.UserLoginCommandDTO;
import com.lzc.mindaispringboot.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class User {
    @PostMapping("/login")
    public Result login(@Valid @RequestBody UserLoginCommandDTO userLoginCommandDTO) {
        System.out.println(userLoginCommandDTO);
        return null;
    }
}
