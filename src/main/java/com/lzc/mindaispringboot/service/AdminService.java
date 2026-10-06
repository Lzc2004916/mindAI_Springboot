package com.lzc.mindaispringboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminService {
    @Resource
    private UserMapper userMapper;
    public Page<User> Page(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)){
            qw.and(w -> w.like(User::getUsername,keyword).or().like(User :: getEmail,keyword));
        }
        if (status != null) {
            qw.eq(User::getStatus,status);
        }
        qw.orderByDesc(User::getCreatedAt);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        page.getRecords().forEach(u -> u.setPassword(null));
        return page;
    }

    public void changestatus(Long userId, Map<String, Integer> body) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusionessException("用户不存在");
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)){
            throw new BusionessException("状态值不合法");
        }
        //防止禁用自己
        if (userId.equals(Token_Aspect.getUserId())){
            throw new BusionessException("无效");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

}
