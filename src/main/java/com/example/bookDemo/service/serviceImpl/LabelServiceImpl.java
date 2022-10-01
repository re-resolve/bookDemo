package com.example.bookDemo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookDemo.entity.Label;
import com.example.bookDemo.mapper.LabelMapper;
import com.example.bookDemo.service.LabelService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LabelServiceImpl extends ServiceImpl<LabelMapper,Label> implements LabelService {

}
