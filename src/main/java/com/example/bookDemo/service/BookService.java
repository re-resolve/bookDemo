package com.example.bookDemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.bookDemo.dto.BookDto;
import com.example.bookDemo.entity.Book;

import java.util.List;

public interface BookService extends IService<Book> {
    
    /**
     * 新增图书，同时保存图书和标签的关联关系
     *
     * @param bookDto
     */
    public void saveWithLabel(BookDto bookDto);
    
    /**
     * 删除多本图书及关联的标签
     *
     * @param ids
     */
    public void removeWithLabel(List<Long> ids);
    
    /**
     * 根据bookId查询图书信息，对应的分类名称和关联的标签信息
     *
     * @param bookId
     * @return
     */
    public BookDto getByIdWithLabelWithCategoryName(Long bookId);
    
    /**
     * 修改图书及其分类和标签信息
     *
     * @param bookDto
     */
    public void updateBookWithLabelWithCategory(BookDto bookDto);
}
