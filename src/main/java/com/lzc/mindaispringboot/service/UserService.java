package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.Dto.UserLoginCommandDTO;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.enumClass.UserStatus;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public Result<UserLoginResponseDTO> login(UserLoginCommandDTO commandDTO) {
        //构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,commandDTO.getUsername())
                .or().eq(User::getEmail,commandDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusionessException("用户不存在");
        }
        String inputPassword = commandDTO.getPassword().trim();
        //明文与密文比对
        boolean matches = passwordEncoder.matches(inputPassword, user.getPassword());
        //验证密码和用户状态
        if (!matches){
            throw new BusionessException("密码不一致");
        } else if (!user.isActive()) {
            throw new BusionessException("用户已被禁用，请联系管理员");
        }
        passwordEncoder.encode(inputPassword);
        return null;
    }
}
