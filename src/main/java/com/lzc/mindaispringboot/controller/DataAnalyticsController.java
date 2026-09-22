package com.lzc.mindaispringboot.controller;

import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.response.DataAnalyticsVO;
import com.lzc.mindaispringboot.service.DataAnalyticsService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-analytics")
public class DataAnalyticsController {
    @Resource
    private DataAnalyticsService dataAnalyticsService;
    @GetToken
    @PreAuthorize("hasRole('2')")
    @GetMapping("/overview")
    public Result<DataAnalyticsVO> overview(){
        return Result.success(dataAnalyticsService.overview());
    }
}
