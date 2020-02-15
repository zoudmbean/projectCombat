package com.pinyougou.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 认证类
 * @author Administrator
 *
 */
public class UserDetailServiceImpl implements UserDetailsService{
	

	/* 
	 * username  是用户登录的时候输入的用户名
	 * 返回null   就登录失败
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("认证类开始：" + username);
		// 获取角色组
		List<GrantedAuthority> authorities = new ArrayList<>();
		// 注意：这里的角色名称要和配置文件中<intercept-url pattern="/**" access="ROLE_USER"/>的角色名称一样
		GrantedAuthority e = new SimpleGrantedAuthority("ROLE_USER");
		authorities.add(e );
		
		/*
		 * 注意：这里的第二个参数是密码，可以给空，因为这个类已经不做认证了，所以可以随便写什么，关键的是第三个参数，所有的角色列表
		 * */
		return new User(username, "", authorities); 
	}

}