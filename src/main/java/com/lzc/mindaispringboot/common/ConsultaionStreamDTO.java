package com.lzc.mindaispringboot.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultaionStreamDTO {
    @NotBlank(message = "sessionId不能为空")
    private String sessionId;
    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000,message = "初始消息不能超过2000字符")
    private String userMessage;
}
