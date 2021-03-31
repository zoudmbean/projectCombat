package com.bjc.common.enums;

/*
* 订单状态枚举
* */
public enum OrderStatusEnum {
    CREATE_NEW(0,"待付款"),
    PAYED(1,"已付款"),
    SENDED(2,"已发货"),
    RECEIVED(3,"已完成"),
    CANCLED(4,"已取消"),
    SERVING(5,"售后中"),
    SERVICED(6,"售后完成");

    private Integer code;
    private String name;

    OrderStatusEnum(Integer code,String name){
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
