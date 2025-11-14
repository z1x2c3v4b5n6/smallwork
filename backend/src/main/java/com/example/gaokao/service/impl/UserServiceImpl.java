package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.domain.User;
import com.example.gaokao.mapper.UserMapper;
import com.example.gaokao.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
