package com.example.bookDemo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookDemo.common.CustomException;
import com.example.bookDemo.dto.BookDto;
import com.example.bookDemo.entity.Book;
import com.example.bookDemo.entity.Category;
import com.example.bookDemo.entity.Label;
import com.example.bookDemo.mapper.BookMapper;
import com.example.bookDemo.service.BookService;
import com.example.bookDemo.service.CategoryService;
import com.example.bookDemo.service.LabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
    
    @Autowired
    private LabelService labelService;
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 新增图书，同时保存图书和标签的关联关系
     *
     * @param bookDto
     */
    @Override
    @Transactional
    public void saveWithLabel(BookDto bookDto) {
        //保存图书的基本信息，操作Book，执行insert操作
        this.save(bookDto);
        
        List<Label> labels = bookDto.getLabels();
        labels.stream().map((item) -> {
            item.setBookId(bookDto.getId());
            return item;
        }).collect(Collectors.toList());
        
        //保存图书和标签的关联关系，操作Label，执行insert操作
        labelService.saveBatch(labels);
        
        log.info("成功新增图书，同时保存图书和标签的关联关系");
        
    }
    
    /**
     * 删除多本图书及关联的标签（前提是：先把书禁用了，若没禁用图书则无法选择删除）
     *
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithLabel(List<Long> ids) {
        
        //查询这些图书是否有正在售卖的
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.in(Book::getId, ids).eq(Book::getStatus, 1);
        
        int count = this.count(wrapper);
        
        if (count > 0) {
            //当前要删除的图书中有书仍在售卖中
            throw new CustomException("当前要删除的图书中有书仍在售卖中,无法删除");
        }
        
        this.removeByIds(ids);
        
        LambdaQueryWrapper<Label> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.in(Label::getBookId, ids);
        
        labelService.remove(queryWrapper);
        
        log.info("成功删除多本图书及关联的标签");
    }
    
    /**
     * 根据bookId查询图书信息，对应的分类名称和关联的标签信息
     *
     * @param bookId
     * @return
     */
    @Override
    @Transactional
    public BookDto getByIdWithLabelWithCategoryName(Long bookId) {
        
        BookDto bookDto = new BookDto();
        
        Book book = super.getById(bookId);
        
        BeanUtils.copyProperties(book, bookDto);
        //在Category表查询CategoryName
        
        Long categoryId = book.getCategoryId();
        
        Category category = categoryService.getById(categoryId);
        
        if (category != null) {
            bookDto.setCategoryName(category.getName());
        }
        
        LambdaQueryWrapper<Label> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(Label::getBookId, bookId);
        
        List<Label> labels = labelService.list(queryWrapper);
        
        bookDto.setLabels(labels);
        
        return bookDto;
    }
    
    /**
     * 修改图书及其分类和标签信息 (前端先通过id查询该图书，然后对得到的对象进行修改再发过来)
     *
     * @param bookDto
     */
    @Override
    @Transactional
    public void updateBookWithLabelWithCategory(BookDto bookDto) {
        this.updateById(bookDto);
        
        Long bookId = bookDto.getId();
        
        //清理当前图书对应标签数据---Label表的delete操作
        LambdaQueryWrapper<Label> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(Label::getBookId, bookDto.getId());
        
        labelService.remove(queryWrapper);
        
        //添加当前提交过来的图书数据---Label表的insert操作
        List<Label> labels = bookDto.getLabels().stream().map((item) -> {
            item.setBookId(bookId);
            return item;
        }).collect(Collectors.toList());
        
        labelService.saveBatch(labels);
    }
}
