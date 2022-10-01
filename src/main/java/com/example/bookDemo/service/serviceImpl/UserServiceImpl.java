package com.example.bookDemo.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookDemo.entity.User;
import com.example.bookDemo.mapper.UserMapper;
import com.example.bookDemo.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
