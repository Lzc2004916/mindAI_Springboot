package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.Dto.ChangePasswordRequest;
import com.lzc.mindaispringboot.Dto.UserLoginCommandDTO;
import com.lzc.mindaispringboot.Dto.UserRegisterCommandDTO;
import com.lzc.mindaispringboot.common.ResultCode;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.enumClass.UserType;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.VO.UserLoginResponseDTO;
import com.lzc.mindaispringboot.Dto.convert.UserConvert;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //登录
    public UserLoginResponseDTO login(UserLoginCommandDTO userLoginCommandDTO) {
        String account = userLoginCommandDTO.getUsername();

        // 2. 先按用户名查，查不到再按邮箱查
        //    不用 eq(username).or().eq(email)：若某人的用户名恰好等于另一人的邮箱，
        //    selectOne 会因命中两行而抛 TooManyResultsException。
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, account));
        if (user == null) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, account));
        }
        //检查是否被锁定
        if (user != null && user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())){
            throw new BusionessException("登录失败次数过多，账号已被锁定，请稍后再试");
        }
        // 3. 账号和密码一起校验：无论"用户不存在"还是"密码错误"，对外都返回同一句提示，
        //    避免攻击者靠提示差异枚举出哪些用户名真实存在。
        String inputPassword = userLoginCommandDTO.getPassword().trim();
        if (user == null || !passwordEncoder.matches(inputPassword, user.getPassword())) {
            recordLoginFailure(user);
            throw new BusionessException("用户名或密码错误");
        }
        // 4. 密码正确才看账号状态。顺序不能反 —— 否则"已被禁用"这个提示本身也泄露了账号存在。
        if (!user.isActive()) {
            throw new BusionessException("用户已被禁用，请联系管理员");
        }
        //登录成功后清零失败计数
        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        //创建新token
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType(), user.getTokenVersion());
        //返回值
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
        return UserConvert.entityToLoginResponse(token, userInfo);
    }

    /// 注册
    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO userRegisterCommandDTO) {
        if (!userRegisterCommandDTO.getPassword().equals(userRegisterCommandDTO.getConfirmPassword())){
            throw new BusionessException("两次密码不一致");
        }
        LambdaQueryWrapper<User> userNameQuery = new LambdaQueryWrapper<>();
        userNameQuery.eq(User::getUsername,userRegisterCommandDTO.getUsername());
        if (userMapper.selectCount(userNameQuery) > 0){
            throw new BusionessException(ResultCode.ACCOUNT_SAME.getCode(),"用户名已存在");
        }
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail,userRegisterCommandDTO.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusionessException("邮箱已存在");
        }
        //将用户注册时输入的明文密码，去除首尾空格后进行 BCrypt 加密
        String encodePassword = passwordEncoder.encode(userRegisterCommandDTO.getPassword().trim());
        User user = UserConvert.RegisterCommandToEntity(userRegisterCommandDTO, encodePassword);
        // ⭐ 注册一律是普通用户：写死角色，忽略请求体可能携带的 userType，防止自行提权成管理员
        user.setUserType(UserType.USER.getCode());
        user.setTokenVersion(0);
        userMapper.insert(user);
        // 只记用户名，不要打印整个 DTO —— DTO 里带明文密码，会一路进日志
        log.info("新用户注册成功：username={}, userId={}", user.getUsername(), user.getId());
        return UserConvert.entityToDetailResponse(user);
    }

    /// 获取账号信息
    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId){
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusionessException("用户不存在");
        return UserConvert.entityToDetailResponse(user);
    }
    private static final Pattern PWD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$");
    ///修改密码
    public String changePassword(Long userId, @Valid ChangePasswordRequest changesPasswordUsername) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getId, userId)
        );
        if (user == null) throw new BusionessException("没有该用户");

        String password = changesPasswordUsername.getPassword().trim();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusionessException("原密码错误");
        }
        String newPassword =changesPasswordUsername.getNewPassword().trim();
        if (!newPassword.equals(changesPasswordUsername.getConfirmPassword())) {
            throw new BusionessException("两次密码不一致");
        }
        if (!PWD_PATTERN.matcher(newPassword.trim()).matches()){
            throw new BusionessException("新密码需 8-50 位，且同时包含字母和数字");
        }
        if (passwordEncoder.matches(newPassword,user.getPassword())){
            throw new BusionessException("新密码不能与原密码相同");
        }
        int newVersion = (user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1;
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(newVersion);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType(), newVersion);
    }
    public void logout(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return;
        int newVersion = (user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1;
        user.setTokenVersion(newVersion);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
    public String renewToken(Long userId){
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusionessException("用户不存在");
        if (!user.isActive()) throw new BusionessException("用户已经被禁用，请联系管理员");
        return JwtTokenUtil.generateToken(user.getId(),user.getUsername(),user.getUserType(), user.getTokenVersion());
    }
    //登录失败次数校验
    private void recordLoginFailure(User user){
        if (user == null) return;
        int failCount = user.getLoginFailCount() == null ? 1 : user.getLoginFailCount() + 1;
        user.setLoginFailCount(failCount);
        user.setLockedUntil(failCount >= 5 ? LocalDateTime.now().plusMinutes(30) : null);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}