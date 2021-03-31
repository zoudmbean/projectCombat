package com.bjc.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bjc.common.utils.R;
import com.bjc.gulimall.cart.feign.ProductFeignService;
import com.bjc.gulimall.cart.intercepter.CartIntercepter;
import com.bjc.gulimall.cart.service.CartService;
import com.bjc.gulimall.cart.to.UserInfoTo;
import com.bjc.gulimall.cart.vo.Cart;
import com.bjc.gulimall.cart.vo.CartItem;
import com.bjc.gulimall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @描述：购物车实现类
 * @创建时间: 2021/3/10
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor pool;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) {

        BoundHashOperations<String, Object, Object> operations = getCartOps();

        // 1. 查看redis是否有该商品
        String res = (String) operations.get(skuId.toString());


        if(StringUtils.isEmpty(res)){       // 购物车没有该商品
            // 新商品添加到购物车
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> skuInTask = CompletableFuture.runAsync(() -> {
                // 1. 远程查询当前要添加的商品的信息
                R r = productFeignService.getSkuInfo(skuId);
                if (r.getCode() == 0) {
                    SkuInfoVo data = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });

                    cartItem.setCheck(true);
                    cartItem.setSkuId(skuId);
                    cartItem.setCount(num);
                    cartItem.setImg(data.getSkuDefaultImg());
                    cartItem.setTitle(data.getSkuTitle());
                    cartItem.setPrice(data.getPrice());
                }
            }, pool);

            // 2. 远程查询sku的组合信息
            CompletableFuture<Void> attrvaluesTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, pool);

            try {
                CompletableFuture.allOf(skuInTask,attrvaluesTask).get();
            } catch (Exception e) {
                log.error("添加购物车异常：",e);
            }
            // 保存到redis
            operations.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            // 购物车有此商品
            CartItem cartItem = JSONObject.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            operations.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    /* 获取购物车某个购物项 */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();

        // 1. 查看redis是否有该商品
        String res = (String) operations.get(skuId.toString());
        CartItem cartItem = JSONObject.parseObject(res, CartItem.class);
        return cartItem;
    }

    /* 获取购物车列表 */
    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartIntercepter.threadLocal.get();
        Long userid = userInfoTo.getUserid();
        if(null == userid){     // 没有登录
            // 根据userKey从redis中获取所有的购物项
            String cartKey = CART_PREFIX+userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            if(!CollectionUtils.isEmpty(cartItems)){
                cart.setItems(cartItems);
            }
        } else {    // 登录
            // 1. 登录的key
            String cartKey = CART_PREFIX+userInfoTo.getUserid();

            // 2. 临时key
            List<CartItem> tempItems = getCartItems(CART_PREFIX+userInfoTo.getUserKey());
            if(!CollectionUtils.isEmpty(tempItems)){
                // 临时购物车有数据，需要合并
                tempItems.forEach(item -> {
                    addToCart(item.getSkuId(),item.getCount());
                });

                // 合并完成 清空临时购物车
                clearCart(CART_PREFIX+userInfoTo.getUserKey());
            }

            // 3. 获取登录状态下的购物车数据【包含合并了的临时购物车数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }

        return cart;
    }

    /* 获取购物车 */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        // threadLocal 只要是同一个请求都能获取到封装的信息
        UserInfoTo userInfoTo = CartIntercepter.threadLocal.get();
        String cartKey = "";
        if(userInfoTo.getUserid() != null){
            cartKey = CART_PREFIX + userInfoTo.getUserid();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        // 得到redis中所有的值（存的时候以JSON串存储的）
        List<Object> values = hashOps.values();
        if(!CollectionUtils.isEmpty(values)){
            List<CartItem> collect = values.stream().map(obj -> {
                String jsonStr = (String) obj;
                CartItem item = JSONObject.parseObject(jsonStr, CartItem.class);
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /* 清空购物车 */
    @Override
    public void clearCart(String cartKey){
        redisTemplate.delete(cartKey);
    }

    /* 勾选购物项 */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1?true:false);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSONObject.toJSONString(cartItem));
    }

    /* 改变商品数量 */
    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSONObject.toJSONString(cartItem));
    }

    @Override
    public void delItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId+"");
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartIntercepter.threadLocal.get();
        if(userInfoTo.getUserid() == null){
            return null;
        }
        String key = CART_PREFIX + userInfoTo.getUserid();
        // 返回所有被选中的购物项
        return getCartItems(key).stream().filter(CartItem::getCheck)
                .map(item -> {
                    // 查询最新价格
                    BigDecimal price = productFeignService.getPrice(item.getSkuId());
                    // TODO 1.更新为最新价格
                    item.setPrice(price);
                    return item;
                })
                .collect(Collectors.toList());
    }
}
