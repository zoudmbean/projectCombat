package com.bjc.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

// 二级分类VO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Category2Vo {
    private String catalog1Id;             // 一级父分类id
    private List<CateLog3Vo> catalog3List;     // 三级子分类
    private String id;
    private String name;

    // 三级分类VO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class CateLog3Vo {
        private String catalog2Id;          // 父分类  2级分类id
        private String id;
        private String name;
    }

}
