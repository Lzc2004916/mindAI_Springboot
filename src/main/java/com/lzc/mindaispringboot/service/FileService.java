package com.lzc.mindaispringboot.service;

import cn.hutool.core.util.StrUtil;
import com.lzc.mindaispringboot.entity.SysFileInfo;
import com.lzc.mindaispringboot.exception.BusionessException;
import com.lzc.mindaispringboot.mappper.SysFileInfoMapper;
import com.lzc.mindaispringboot.response.FileUploadVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    /** 单文件上限 10MB（跟 yml 里保持一致，双保险） */
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    /** 扩展名白名单：只允许图片和常见文档，禁止上传 exe/sh 等可执行文件 */
    private static final List<String> ALLOWED_EXT = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp",
            "pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Resource
    private SysFileInfoMapper sysFileInfoMapper;
    public FileUploadVO upload(
            MultipartFile file
            ,String businessType
            ,String businessId
            ,String businessField
            ,Boolean isTemp
            ,Long userId){
        if (file == null || file.isEmpty()){
            throw new BusionessException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_SIZE){
            throw new BusionessException("文件大小不能超过 10MB");
        }
        //文件名
        String originalName = file.getOriginalFilename();
        //获取文件后缀
        String ext = getExt(originalName);
        if (!ALLOWED_EXT.contains(ext)){
            throw new BusionessException("不支持文件类型："+ ext);
        }
        try {
            ///生成存储路径：根目录 / 年 / 月 / 日 / UUID.ext
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            ///拼接文件名
            String relative = datePath + "/" + UUID.randomUUID() + "." + ext;
            /// 拼接定义好的系统路径+文件名
            Path target = Paths.get(uploadDir).resolve(relative);
            ///目录如果不存在就将file二进制存进target
            Files.createDirectories(target.getParent());
            try(var in = file.getInputStream()) {
                //存进磁盘中
                Files.copy(in,target, StandardCopyOption.REPLACE_EXISTING);
            }
            //入库
            SysFileInfo info = SysFileInfo.builder()
                    .originalName(originalName)
                    .filePath("/files" + relative)
                    .fileSize(file.getSize())
                    .fileType(resolveFileType(ext))
                    .businessType(businessType)
                    .businessField(businessId)
                    .uploadUserId(userId)
                    //如果isTemp是1代表临时文件
                    .isTemp(Boolean.TRUE.equals(isTemp) ? 1 : 0)
                    .status(1)
                    .createTime(LocalDateTime.now())
                    //如果是临时文件给24小时有效期，方便后续定时清理
                    .expireTime(Boolean.TRUE.equals(isTemp) ? LocalDateTime.now().plusDays(24) : null)
                    .build();
            sysFileInfoMapper.insert(info);
            return FileUploadVO.builder()
                    .id(info.getId())
                    .originalName(info.getOriginalName())
                    .businessType(info.getBusinessType())
                    .fileSize(info.getFileSize())
                    .fileType(info.getFileType())
                    .build();
        }catch (IOException e){
            throw new BusionessException("文件上传失败" + e.getMessage());
        }

    }
    private String resolveFileType(String ext){
        return switch (ext){
            case "jpg", "jpeg", "png", "gif", "webp", "bmp" -> "IMG";
            case "pdf" -> "PDF";
            case "txt" -> "TXT";
            case "doc", "docx" -> "DOC";
            case "xls", "xlsx" -> "XLS";
            default -> "OTHER";
        };
    }
    private String getExt(String fileName){
        if (StrUtil.isNotBlank(fileName)) return "";
        int idx = fileName.lastIndexOf(".");
        return idx < 0 ? "" : fileName.substring(idx + 1).toLowerCase();
    }
}