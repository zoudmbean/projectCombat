package com.bjc.gulimall.gateway.sentinel;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSONObject;
import com.bjc.common.enums.BizCodeEnume;
import com.bjc.common.utils.R;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @描述：Sentinel网关定制配置类
 * @创建时间: 2021/4/18
 */
public class SentinelGatewayConfig {
    public SentinelGatewayConfig(){
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            // 网关限流了请求，就会调用该回调  Mono  Flux
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R error = R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(), BizCodeEnume.TOO_MANY_REQUEST.getMsg());
                String json = JSONObject.toJSONString(error);
                return ServerResponse.ok().body(Mono.just(json),String.class);
            }
        });
    }
}
