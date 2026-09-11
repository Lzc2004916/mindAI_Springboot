package com.lzc.mindaispringboot.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.Dto.UserLoginCommandDTO;
import com.lzc.mindaispringboot.Dto.UserRegisterCommandDTO;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.enumClass.UserType;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;
import com.lzc.mindaispringboot.service.convert.UserConvert;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public UserLoginResponseDTO login(UserLoginCommandDTO userLoginCommandDTO) {
        //构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,userLoginCommandDTO.getUsername())
                .or().eq(User::getEmail,userLoginCommandDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusionessException("用户不存在");
        }
        String inputPassword = userLoginCommandDTO.getPassword().trim();
        //明文与密文比对
        boolean matches = passwordEncoder.matches(inputPassword, user.getPassword());
        //验证密码和用户状态
        if (!matches){
            throw new BusionessException("密码不一致");
        } else if (!user.isActive()) {
            throw new BusionessException("用户已被禁用，请联系管理员");
        }
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
        return UserConvert.entityToLoginResponse(token,userInfo);
    }
    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO userRegisterCommandDTO) {
        System.out.println(JSONUtil.parseObj(userRegisterCommandDTO));
        if (!userRegisterCommandDTO.getPassword().equals(userRegisterCommandDTO.getConfirmPassword())){
            throw new  BusionessException("两次密码不一致");
        }
        LambdaQueryWrapper<User> userNameQuery = new LambdaQueryWrapper<>();
        userNameQuery.eq(User::getUsername,userRegisterCommandDTO.getUsername());
        if (userMapper.selectCount(userNameQuery) > 0){
            throw new BusionessException("用户名已存在");
        }
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail,userRegisterCommandDTO.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusionessException("邮箱已存在");
        }
        if (!UserType.isValidCode(userRegisterCommandDTO.getUserType())){
            throw new BusionessException("无效用户类型");
        }
        String encodePassword = passwordEncoder.encode(userRegisterCommandDTO.getPassword().trim());
        User user = UserConvert.RegisterCommandToEntity(userRegisterCommandDTO, encodePassword);
        userMapper.insert(user);
        return UserConvert.entityToDetailResponse(user);
    }
    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId){
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusionessException("用户不存在");
        return UserConvert.entityToDetailResponse(user);
    }
}
