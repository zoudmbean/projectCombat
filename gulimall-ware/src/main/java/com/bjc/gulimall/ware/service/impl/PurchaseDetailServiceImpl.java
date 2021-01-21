package com.bjc.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.ware.dao.PurchaseDetailDao;
import com.bjc.gulimall.ware.entity.PurchaseDetailEntity;
import com.bjc.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();

        String key = (String)params.get("key");
        if(StringUtils.isNotEmpty(key)){
            wrapper.and(q -> q.eq("purchase_id",key).or().eq("sku_id",key));
        }
        String status = (String)params.get("status");
        if(StringUtils.isNotEmpty(status)){
            wrapper.eq("status",status);
        }
        String wareId = (String)params.get("wareId");
        if(StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPrrchaseId(Long id) {
        QueryWrapper<PurchaseDetailEntity> wraper = new QueryWrapper<PurchaseDetailEntity>();
        wraper.eq("purchase_id",id);
        return this.list(wraper);
    }

}
