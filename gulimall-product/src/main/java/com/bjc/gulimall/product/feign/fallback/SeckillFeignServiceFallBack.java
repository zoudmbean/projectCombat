package com.bjc.gulimall.product.feign.fallback;

import com.bjc.common.enums.BizCodeEnume;
import com.bjc.common.utils.R;
import com.bjc.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @描述：秒杀降级熔断类
 * @创建时间: 2021/4/18
 */
@Component
@Slf4j
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("SeckillFeignServiceFallBack熔断方法调用。。。。");
        return R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(),BizCodeEnume.TOO_MANY_REQUEST.getMsg());
    }
}
