package com.pinyougou.shop.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

/**
 * 认证类
 * @author Administrator
 *
 */
public class UserDetailServiceImpl implements UserDetailsService{
	
	// 注入SellerService，用户校验用户名和密码
	private SellerService sellerService;
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}


	/* 
	 * username  是用户登录的时候输入的用户名
	 * 返回null   就登录失败
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// 获取角色组
		List<GrantedAuthority> authorities = new ArrayList<>();
		GrantedAuthority e = new SimpleGrantedAuthority("ROLE_SELLER");
		authorities.add(e );
		
		// 根据用户名查询对应的对象
		TbSeller seller = sellerService.findOne(username);
		if(null != seller){
			if(seller.getStatus().equals("1")){ // 只有审核通过的用户才允许登录
				// 认证
				User user = new User(username, seller.getPassword(), authorities);
				return user;
			}
		}
		return null;
	}

}
