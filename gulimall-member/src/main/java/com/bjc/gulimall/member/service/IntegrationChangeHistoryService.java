package com.bjc.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 积分变化历史记录
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:25:27
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

