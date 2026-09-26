package com.lzc.mindaispringboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzc.mindaispringboot.common.Dto.UserLoginCommandDTO;
import com.lzc.mindaispringboot.common.Dto.UserRegisterCommandDTO;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.enumClass.UserType;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.response.UserLoginResponseDTO;
import com.lzc.mindaispringboot.service.convert.UserConvert;
import com.lzc.mindaispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 登录失败计数：key = 传入的账号（用户名或邮箱），value = [失败次数, 首次失败时间戳] */
    private static final Map<String, long[]> LOGIN_FAIL = new ConcurrentHashMap<>();
    /** 连续失败多少次后临时锁定 */
    private static final int MAX_FAIL_COUNT = 5;
    /** 锁定时长（毫秒） */
    private static final long LOCK_MILLIS = 5 * 60 * 1000L;

    //登录
    public UserLoginResponseDTO login(UserLoginCommandDTO userLoginCommandDTO) {
        String account = userLoginCommandDTO.getUsername();

        // 1. 先看该账号是否因连续失败被临时锁定
        if (isLocked(account)) {
            throw new BusionessException("登录失败次数过多，请 5 分钟后再试");
        }

        // 2. 先按用户名查，查不到再按邮箱查
        //    不用 eq(username).or().eq(email)：若某人的用户名恰好等于另一人的邮箱，
        //    selectOne 会因命中两行而抛 TooManyResultsException。
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, account));
        if (user == null) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, account));
        }

        // 3. 账号和密码一起校验：无论"用户不存在"还是"密码错误"，对外都返回同一句提示，
        //    避免攻击者靠提示差异枚举出哪些用户名真实存在。
        String inputPassword = userLoginCommandDTO.getPassword().trim();
        if (user == null || !passwordEncoder.matches(inputPassword, user.getPassword())) {
            recordFail(account);
            throw new BusionessException("用户名或密码错误");
        }
        // 4. 密码正确才看账号状态。顺序不能反 —— 否则"已被禁用"这个提示本身也泄露了账号存在。
        if (!user.isActive()) {
            throw new BusionessException("用户已被禁用，请联系管理员");
        }

        LOGIN_FAIL.remove(account);   // 登录成功，清零失败计数
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
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
            throw new BusionessException("用户名已存在");
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

    /** 该账号是否处于"失败次数过多"的锁定状态 */
    private boolean isLocked(String account) {
        if (account == null || account.isBlank()) return false;
        long[] rec = LOGIN_FAIL.get(account);
        if (rec == null) return false;
        // 锁已过期 → 清掉记录，重新开始计数
        if (System.currentTimeMillis() - rec[1] > LOCK_MILLIS) {
            LOGIN_FAIL.remove(account);
            return false;
        }
        return rec[0] >= MAX_FAIL_COUNT;
    }

    /** 记一次登录失败（超过锁定时长的旧记录会重新开始计数） */
    private void recordFail(String account) {
        if (account == null || account.isBlank()) return;
        long now = System.currentTimeMillis();
        LOGIN_FAIL.compute(account, (k, rec) -> {
            if (rec == null || now - rec[1] > LOCK_MILLIS) {
                return new long[]{1, now};
            }
            rec[0]++;
            return rec;
        });
    }
}
