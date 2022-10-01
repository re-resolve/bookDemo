package com.example.bookDemo;

import com.example.bookDemo.entity.User;
import com.example.bookDemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;

@SpringBootTest
class BookDemoApplicationTests {

	@Test
	void contextLoads() {
	}
	@Autowired
	UserService userService ;
	@Test
	void test1(){
		User user =new User();
		user.setId(null);
		user.setName("root123");
		user.setCreateUser(235L);
		user.setUpdateUser(235L);
		user.setCreateTime(LocalDateTime.now());
		user.setUpdateTime(LocalDateTime.now());
		user.setPassword(DigestUtils.md5DigestAsHex("root123".getBytes()));
		user.setStatus(1);
		userService.save(user);
	}
	
}
