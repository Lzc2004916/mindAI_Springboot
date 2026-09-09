package com.lzc.mindaispringboot.mappper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzc.mindaispringboot.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
