package com.example.bookDemo.dto;

import com.example.bookDemo.entity.Book;
import com.example.bookDemo.entity.Label;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookDto extends Book {
    //一本书的多个标签
    private List<Label> labels =new ArrayList<>();
    //一本书的分类名
    private String categoryName;
}
