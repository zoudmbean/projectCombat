
package com.bjc.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	// 利用fastJSON提供的typeReference可以传入复杂的类型
public <T> T getData(TypeReference<T> typeReference){
	Object obj = get("data");
	String jsonStr = JSON.toJSONString(obj);
	T t = JSON.parseObject(jsonStr,typeReference);
	return t;
}

	public R setData(Object data){
		put("data",data);
		return this;
	}

	public R() {
		put("code", 0);
		put("msg", "success");
	}

	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}

	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}

	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}

	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R ok() {
		return new R();
	}

	public Integer getCode() {
		return Integer.parseInt(this.get("code").toString());
	}

	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
