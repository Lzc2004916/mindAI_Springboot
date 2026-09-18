package com.lzc.mindaispringboot.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVO {
    private Long id;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String businessType;

}