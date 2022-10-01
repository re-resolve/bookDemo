package com.example.bookDemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookDemo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
