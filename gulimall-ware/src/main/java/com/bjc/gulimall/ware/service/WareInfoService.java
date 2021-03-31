package com.bjc.gulimall.ware.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bjc.common.utils.PageUtils;
import com.bjc.gulimall.ware.entity.WareInfoEntity;
import com.bjc.gulimall.ware.vo.LockStockResult;
import com.bjc.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:28:45
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    JSONObject getFace(Long id);

    Boolean orderlock(WareSkuLockVo vo);
}

