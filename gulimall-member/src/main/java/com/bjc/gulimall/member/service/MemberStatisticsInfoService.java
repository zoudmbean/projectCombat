package com.bjc.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.member.entity.MemberStatisticsInfoEntity;

import java.util.Map;

/**
 * 会员统计信息
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:25:26
 */
public interface MemberStatisticsInfoService extends IService<MemberStatisticsInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

