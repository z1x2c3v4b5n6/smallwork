package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.domain.University;
import com.example.gaokao.mapper.UniversityMapper;
import com.example.gaokao.service.UniversityService;
import org.springframework.stereotype.Service;

@Service
public class UniversityServiceImpl extends ServiceImpl<UniversityMapper, University> implements UniversityService {
}
