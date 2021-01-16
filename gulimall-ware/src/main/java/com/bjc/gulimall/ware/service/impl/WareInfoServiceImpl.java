package com.bjc.gulimall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.ware.dao.WareInfoDao;
import com.bjc.gulimall.ware.entity.WareInfoEntity;
import com.bjc.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String)params.get("key");
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            wrapper.and(q -> {
                q.eq("id",key).or().like("name",key).or().like("address",key).or().like("areacode",key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}
