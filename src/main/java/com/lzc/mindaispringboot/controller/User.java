package com.lzc.mindaispringboot.controller;

import com.lzc.mindaispringboot.Dto.UserLoginCommandDTO;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;
import com.lzc.mindaispringboot.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class User {
    @Resource
    private UserService userService;
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO userLoginCommandDTO) {
        userService.login(userLoginCommandDTO);
        return null;
    }
}
