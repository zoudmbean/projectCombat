package com.pinyougou.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**用户登录控制层
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/login")
public class LoginController {
	
	@RequestMapping("/showName")
	public Map<String,Object> showName(){
		Map<String, Object> map = new HashMap<String, Object>();
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if(StringUtils.isEmpty(name)){
			map.put("loginName", null);
		} else {
			map.put("loginName", name);
		}
		return map;
	}

}
