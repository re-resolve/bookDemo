package com.example.bookDemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.bookDemo.entity.Category;

public interface CategoryService extends IService<Category> {
    public void removeByIdWithBookWithLabel(Long id);
}
