package com.example.bookDemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookDemo.common.Result;
import com.example.bookDemo.entity.Category;
import com.example.bookDemo.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 新增图书分类信息
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody Category category){
        
        log.info("新增图书分类信息：{}",category.toString());
        
        categoryService.save(category);
        
        return Result.success("新增图书分类成功");
    }
    
    /**
     * 图书分类分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        log.info("图书分类分页查询：page = {},pageSize = {} ,name = {}", page, pageSize, name);
        
        Page<Category> categoryPage=new Page<>(page,pageSize);
    
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        
        queryWrapper.like(StringUtils.isNotBlank(name),Category::getName,name);
    
        queryWrapper.orderByAsc(Category::getSort);
        
        categoryService.page(categoryPage,queryWrapper);
        
        return Result.success(categoryPage);
    }
    
    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        log.info("根据id删除分类");
        
        categoryService.removeById(id);
        
        return Result.success("删除分类成功");
    }
}
