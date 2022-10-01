package com.example.bookDemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookDemo.common.CustomException;
import com.example.bookDemo.common.Result;
import com.example.bookDemo.dto.BookDto;
import com.example.bookDemo.entity.Book;
import com.example.bookDemo.entity.Category;
import com.example.bookDemo.entity.Label;
import com.example.bookDemo.service.BookService;
import com.example.bookDemo.service.CategoryService;
import com.example.bookDemo.service.LabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/book")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private LabelService labelService;
    
    /**
     * 新增图书与其标签信息
     *
     * @param bookDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody BookDto bookDto) {
        
        log.info("新增图书信息：{}", bookDto.toString());
        
        bookService.saveWithLabel(bookDto);
        
        return Result.success("新增图书成功");
    }
    
    /**
     * 图书分页查询(可根据书名模糊查询)
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Transactional
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        log.info("图书分页查询：page = {},pageSize = {} ,name = {}", page, pageSize, name);
        
        //构造分页构造器
        Page<Book> bookPage = new Page<>(page, pageSize);
        Page<BookDto> bookDtoPage = new Page<>();
        
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.like(StringUtils.isNotBlank(name), Book::getName, name);
        
        queryWrapper.orderByDesc(Book::getUpdateTime);
        
        bookService.page(bookPage, queryWrapper);
        
        //拷贝对象，bookPage的records是List<Book>,而bookDtoPage的records应是List<BookDto>的，因此不能copy records
        BeanUtils.copyProperties(bookPage, bookDtoPage, "records");
        
        List<Book> books = bookPage.getRecords();
        
        List<BookDto> bookDtoList = books.stream().map((item) -> {
            BookDto bookDto = new BookDto();
            
            BeanUtils.copyProperties(item, bookDto);
            
            Long categoryId = item.getCategoryId();
            
            Category category = categoryService.getById(categoryId);
            
            if (category != null) {
                String categoryName = category.getName();
                bookDto.setCategoryName(categoryName);
            }
            return bookDto;
        }).collect(Collectors.toList());
        
        bookDtoPage.setRecords(bookDtoList);
        
        return Result.success(bookDtoPage);
    }
    
    /**
     * 根据ids删除图书（前提是：先把书禁用了，若没禁用图书则无法选择删除）
     *
     * @param ids
     * @return
     */
    @DeleteMapping()
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("根据ids删除图书");
        
        //判断ids是否为空
        if(CollectionUtils.isNotEmpty(ids)){
            
            bookService.removeWithLabel(ids);
        }
        else{
            throw new CustomException("ids为空");
        }
        return Result.success("删除图书成功");
    }
    
    /**
     * 根据bookId查询图书信息，对应的分类名称和关联的标签信息
     *
     * @param bookId
     * @return
     */
    @GetMapping("/{bookId}")
    public Result<BookDto> get(@PathVariable Long bookId) {
        log.info("根据bookId查询图书信息，对应的分类名称和关联的标签信息");
        
        BookDto bookDto = bookService.getByIdWithLabelWithCategoryName(bookId);
        
        return Result.success(bookDto);
    }
    
    /**
     * 修改图书售卖状态
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable(value = "status") Integer status, @RequestParam Long id) {
        
        log.info("修改图书售卖状态");
        
        Book book = bookService.getById(id);
        
        if (book != null) {
            LambdaUpdateWrapper<Book> updateWrapper = new LambdaUpdateWrapper<>();
            
            updateWrapper.eq(Book::getId, id);
            
            updateWrapper.set(status == 0 || status == 1, Book::getStatus, status);
            
            bookService.update(updateWrapper);
            
            return Result.success("修改图书售卖状态成功");
        }
        return Result.error("修改图书售卖状态失败");
    }
    
    /**
     * 修改图书及其分类和标签信息
     * (前端先通过id查询该图书，然后对得到的对象进行修改再发过来[1.version不能修改][2.updateUser和updateTime设为空就行])
     *
     * @param bookDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody BookDto bookDto) {
        
        log.info("修改图书及其分类和标签信息");
        
        bookService.updateBookWithLabelWithCategory(bookDto);
        
        return Result.success("修改图书及其分类和标签信息成功");
    }
    
    /**
     * 根据多个标签，分页查询图书
     * 如果没选标签，则直接调用 “图书分页查询” 接口
     *
     * @param page
     * @param pageSize
     * @param labelName
     * @return
     */
    @Transactional
    @GetMapping("/label")
    public Result<Page> pageLabel(int page, int pageSize, @RequestParam List<String> labelName) {
        
        log.info("根据多个标签，分页查询图书");
        
        //判断是否选择了标签
        if (CollectionUtils.isNotEmpty(labelName)) {
            //构造分页构造器(BookDto为返回给前端的类型)
            Page<Book> bookPage = new Page<>(page, pageSize);
            
            Page<BookDto> bookDtoPage = new Page<>();
            
            //查询与第一个 labelName 所关联的 bookIds
            LambdaQueryWrapper<Label> queryWrapper = new LambdaQueryWrapper<>();
            
            queryWrapper.eq(StringUtils.isNotBlank(labelName.get(0)), Label::getName, labelName.get(0)).select(Label::getBookId);
            
            Set<Long> bookIds = labelService.list(queryWrapper).stream().map(Label::getBookId).collect(Collectors.toSet());
            
            //遍历其余labelName
            for (int i = 1; i < labelName.size(); i++) {
                LambdaQueryWrapper<Label> queryWrapper2 = new LambdaQueryWrapper<>();
                
                queryWrapper2.eq(StringUtils.isNotBlank(labelName.get(i)), Label::getName, labelName.get(i)).select(Label::getBookId);
                
                //取交集（同时满足每个labelName的bookId）
                bookIds.retainAll(labelService.list(queryWrapper2).stream().map(Label::getBookId).collect(Collectors.toList()));
            }
            
            //分页查询图书
            LambdaQueryWrapper<Book> bookLambdaQueryWrapper = new LambdaQueryWrapper<>();
            
            bookLambdaQueryWrapper.in(Book::getId, bookIds);
            
            bookService.page(bookPage, bookLambdaQueryWrapper);
            
            //拷贝对象，bookPage的records是List<Book>,而bookDtoPage的records应是List<BookDto>的，因此不能copy records
            BeanUtils.copyProperties(bookPage, bookDtoPage, "records");
            
            List<BookDto> bookDtos = bookPage.getRecords().stream().map((item) -> {
                
                BookDto bookDto = new BookDto();
                
                BeanUtils.copyProperties(item, bookDto);
                
                Long categoryId = item.getCategoryId();
                
                Category category = categoryService.getById(categoryId);
                
                if (category != null) {
                    
                    String categoryName = category.getName();
                    
                    bookDto.setCategoryName(categoryName);
                }
                return bookDto;
            }).collect(Collectors.toList());
            
            bookDtoPage.setRecords(bookDtos);
            
            return Result.success(bookDtoPage);
        }
        //若没选择标签则抛出异常
        else {
            throw new CustomException("没有选择标签（根据多个标签分页查询图书）");
        }
        
    }
}
