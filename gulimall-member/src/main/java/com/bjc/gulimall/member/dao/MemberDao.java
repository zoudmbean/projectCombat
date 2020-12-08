package com.bjc.gulimall.member.dao;

import com.bjc.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:25:27
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
