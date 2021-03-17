package com.bjc.gulimall.cart.service;

import com.bjc.gulimall.cart.vo.Cart;
import com.bjc.gulimall.cart.vo.CartItem;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void delItem(Long skuId);

}
