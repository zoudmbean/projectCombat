package com.bjc.gulimall.product;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.bjc.gulimall.product.entity.BrandEntity;
import com.bjc.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    private OSSClient ossClient;

    @Test
    public void test02() throws FileNotFoundException {
        InputStream in = new FileInputStream("D:\\1.jpg");
        ossClient.putObject("zoudm-gulimall", "1.png",in);
        ossClient.shutdown();
    }

    /*@Test
    public void test01(){
        // Endpoint以杭州为例，其它Region请按实际情况填写。oss-cn-hangzhou.aliyuncs.com
        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = "LTAI4GHU。。。";
        String accessKeySecret = "nLLv4x0a2。。。";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 创建PutObjectRequest对象。
        PutObjectRequest putObjectRequest = new PutObjectRequest("zoudm-gulimall", "拼多多.png", new File("F:\\拼多多.png"));

        // 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // metadata.setObjectAcl(CannedAccessControlList.Private);
        // putObjectRequest.setMetadata(metadata);

        // 上传文件。
        ossClient.putObject(putObjectRequest);

        // 关闭OSSClient。
        ossClient.shutdown();
    }*/

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
