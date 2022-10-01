package com.example.bookDemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookDemo.common.BaseContext;
import com.example.bookDemo.common.Result;
import com.example.bookDemo.entity.User;
import com.example.bookDemo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 后台用户登录
     *
     * @param request
     * @param user
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(HttpServletRequest request, @RequestBody User user) {
        
        String password = user.getPassword();
        
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(User::getName, user.getName());
        
        User user1 = userService.getOne(queryWrapper);
        
        if (user1 == null) {
            return Result.error("账号不存在!");
        }
        if (!user1.getPassword().equals(password)) {
            return Result.error("密码错误!");
        }
        if (user1.getStatus() == 0) {
            return Result.error("账号已禁用!");
        }
        
        //登录成功，将用户id放入session中
        request.getSession().setAttribute("user", user1.getId());
        
        return Result.success(user1);
    }
    
    /**
     * 用户登出账号
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        
        request.getSession().removeAttribute("user");
        
        return Result.success("退出成功");
    }
    
    /**
     * 后台用户信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        
        log.info("后台用户信息分页查询：page = {},pageSize = {} ,name = {}", page, pageSize, name);
        
        //1.构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        
        //2.构造条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        //3.添加过滤条件，使用StringUtils.isNotBlank()可判断空格也为空
        queryWrapper.like(StringUtils.isNotBlank(name), User::getName, name);
        
        //4.添加排序条件
        queryWrapper.orderByDesc(User::getUpdateTime);
        
        //执行查询
        userService.page(pageInfo, queryWrapper);
        return Result.success(pageInfo);
    }
    
    /**
     * 新增后台用户信息
     *
     * @param user
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody User user) {
        log.info("新增后台用户信息：{}", user.toString());
        
        //查询当前用户是否已经存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    
        queryWrapper.eq(User::getName, user.getName());
    
        User user1 = userService.getOne(queryWrapper);
    
        if (user1 != null) {
            return Result.error("账号已存在!");
        }
        
        //若不存在则创建账号
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        
        userService.save(user);
        
        return Result.success("新增后台用户信息成功");
    }
    
    /**
     * 修改后台用户信息（可禁用某个用户）
     *
     * @param user
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody User user) {
        
        //不允许禁用当前账户
        if (user.getId().equals(BaseContext.getCurrentId())) {
            if (user.getStatus() == 0) {
                return Result.error("无法禁用当前登录用户!");
            }
        }
        log.info("修改后台用户信息：{}", user.toString());
        
        userService.updateById(user);
        
        return Result.success("修改后台用户信息成功");
    }
    
    /**
     * 根据id查询账号信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        log.info("根据id查询账号信息");
        User user = userService.getById(id);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("没有查询到对应账号信息");
    }
}
