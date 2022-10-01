package com.example.bookDemo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookDemo.common.CustomException;
import com.example.bookDemo.entity.Book;
import com.example.bookDemo.entity.Category;
import com.example.bookDemo.entity.Label;
import com.example.bookDemo.mapper.CategoryMapper;
import com.example.bookDemo.service.BookService;
import com.example.bookDemo.service.CategoryService;
import com.example.bookDemo.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private LabelService labelService;
    
    @Override
    public void removeByIdWithBookWithLabel(Long id) {
        LambdaQueryWrapper<Book>queryWrapper=new LambdaQueryWrapper<>();
        //查询与所删除分类关联的图书是否禁止了售卖（否则不允许删除分类）
        queryWrapper.eq(Book::getCategoryId,id).eq(Book::getStatus,1);
    
        int count = bookService.count(queryWrapper);
        //还有图书正在售卖中，无法删除分类
        if(count>0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("仍有该分类图书正在售卖，无法删除当前图书分类");
        }
        
        //如果可以删除，先删除Book数据表中的图书
        LambdaQueryWrapper<Book> queryWrapper1=new LambdaQueryWrapper<>();
        
        queryWrapper1.eq(Book::getCategoryId,id).select(Book::getId);
    
        List<Book> bookIds = bookService.list(queryWrapper1);
        
        bookService.removeByIds(bookIds);
        
        //然后删除Label表中的标签信息
        LambdaQueryWrapper<Label> queryWrapper2=new LambdaQueryWrapper<>();
        
        queryWrapper2.in(Label::getBookId,bookIds);
        
        labelService.remove(queryWrapper2);
    }
}
