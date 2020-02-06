package com.pinyougou.shop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家登录控制层
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/login")
public class LoginController {
	
	@RequestMapping("/getLoginName")
	public Map<String, Object> getLoginName(){
		Map<String, Object> map = new HashMap<String, Object>();
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		map.put("loginName", name);
		return map;
	}
}
