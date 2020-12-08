package com.bjc.gulimall.product;

import com.bjc.gulimall.product.entity.BrandEntity;
import com.bjc.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    public void contextLoads() {
        BrandEntity entity = new BrandEntity()
                                .setDescript("")
                                .setFirstLetter("H")
                                .setName("华为")
                                .setShowStatus(1)
                                .setSort(1)
                                .setLogo("");
        brandService.save(entity);
    }

}
