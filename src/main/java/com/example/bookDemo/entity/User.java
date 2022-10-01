package com.example.bookDemo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体类：后台用户
 */
@Data
public class User implements Serializable//实现这个接口
{
    //用户ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    //用户名
    @TableField(value = "username")
    private String name;
    
    //密码（查询不返回）
    @TableField(value = "pwd")
    private String password;
    
    //员工账号状态 0：禁用 1：启用（默认为1）
    private Integer status;
    
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
}
