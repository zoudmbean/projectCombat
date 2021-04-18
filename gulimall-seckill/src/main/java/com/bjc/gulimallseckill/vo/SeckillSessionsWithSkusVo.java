package com.bjc.gulimallseckill.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @描述：sku秒杀vo
 * @创建时间: 2021/4/6
 */
@Data
@ToString
public class SeckillSessionsWithSkusVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<SeckillSkuVo> relationSkus;

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;
}
