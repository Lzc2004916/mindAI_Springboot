package com.lzc.mindaispringboot.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateDTO {
    @Size(max = 50,message = "昵称最长50字")
    private String nickname;
    @Size(max = 255,message = "头像地址过长")
    private String avatar;
    @Min(1)
    @Max(2)
    private Integer gender;// 1男 2女
    @Past(message = "生日不能是未来日期")
    private LocalDate birthday;
}
