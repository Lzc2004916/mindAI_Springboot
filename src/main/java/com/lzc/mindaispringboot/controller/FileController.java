package com.lzc.mindaispringboot.controller;

import com.lzc.mindaispringboot.Aop.GetToken;
import com.lzc.mindaispringboot.Aop.Token_Aspect;
import com.lzc.mindaispringboot.common.Result;
import com.lzc.mindaispringboot.response.FileUploadVO;
import com.lzc.mindaispringboot.service.FileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Resource
    private FileService fileService;
    @GetToken
    @PostMapping("/upload")
    public Result<FileUploadVO> upload(
            @RequestParam("file")MultipartFile file,
            @RequestParam(value = "businessType",required = false) String businessType,
            @RequestParam(value = "businessId",required = false) String businessId,
            @RequestParam(value = "businessField",required = false) String businessField,
            @RequestParam(value = "isTemp",required = false) Boolean isTemp
            ){
        Long userId = Token_Aspect.getUserId();
        return Result.success(fileService.upload(file, businessType, businessId, businessField, isTemp, userId));
    }
}
