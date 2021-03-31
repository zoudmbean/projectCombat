package com.bjc.common.exception;

/**
 * @描述：库存不足异常
 * @创建时间: 2021/3/23
 */
public class NoStockException extends RuntimeException {
    private Long skuId;
    public NoStockException(Long skuId){
        super("商品id = " + skuId + "的商品，没有足够的库存了！");
    }
    public NoStockException(){
        super("商品没有足够的库存了！");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
