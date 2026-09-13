package com.lzc.mindaispringboot.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.common.Dto.UserLoginCommandDTO;
import com.lzc.mindaispringboot.common.Dto.UserRegisterCommandDTO;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;
import com.lzc.mindaispringboot.service.UserService;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.antlr.runtime.Token;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    //登录
    @Resource
    private UserService userService;
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO userLoginCommandDTO) {
        UserLoginResponseDTO result = userService.login(userLoginCommandDTO);
        return Result.success(result);
    }
    //添加账号
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO userRegisterCommandDTO){
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(userRegisterCommandDTO);
        return Result.success(result);
    }
    //获取账号信息
    @GetToken
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser(){
        Long userId = Token_Aspect.getUserId();
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserById(userId);
        return Result.success(result);
    }
}
