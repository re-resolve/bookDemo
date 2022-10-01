package com.example.bookDemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体类：图书信息
 */
@Data
public class Book implements Serializable {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    //书名
    private String name;
    
    //分类id
    private Long categoryId;
    
    //作者
    private String author;
    
    //状态 0:停售  1:起售
    private Integer status;
    
    //顺序
    private Integer sort;
    
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    //创建人
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    
    //修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
    
    //逻辑删除（0：没删 1：已删）
    private Integer deleted;
    
    //乐观锁
    @Version
    private Integer version;
}
