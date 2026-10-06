package com.lzc.mindaispringboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.entity.User;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.UserMapper;
import com.lzc.mindaispringboot.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAllAuthorities(T(com.lzc.mindaispringboot.enumClass.UserType).ROLE_ADMIN)")
public class AdminUserController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminService adminService;
    /**
     * 分页查询用户列表
     * 支持按用户名/邮箱模糊搜索、按状态筛选
     */
    @GetToken
    @GetMapping
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ){
        Page<User> page = adminService.Page(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }
    /**
     * 禁用/启用用户
     */
    @GetToken
    @PutMapping("/{userId}/status")
    public Result<Void> changeStatus(@PathVariable Long userId, @RequestBody Map<String,Integer> body){
        adminService.changestatus(userId,body);
        return Result.success();
    }
    /**
     * 查看用户详情（含统计数据）
     */
    @GetToken
    @GetMapping("/{userId}")
    public Result<User> detail(@PathVariable Long userId){
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusionessException("用户不存在");
        user.setPassword(null);
        return Result.success(user);
    }
}
