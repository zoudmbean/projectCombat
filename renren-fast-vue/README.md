## renren-fast-vue
- renren-fast-vue基于vue、element-ui构建开发，实现[renren-fast](https://gitee.com/renrenio/renren-fast)后台管理前端功能，提供一套更优的前端解决方案
- 前后端分离，通过token进行数据交互，可独立部署
- 主题定制，通过scss变量统一一站式定制
- 动态菜单，通过菜单管理统一管理访问路由
- 数据切换，通过mock配置对接口数据／mock模拟数据进行切换
- 发布时，可动态配置CDN静态资源／切换新旧版本
- 演示地址：[http://demo.open.renren.io/renren-fast](http://demo.open.renren.io/renren-fast) (账号密码：admin/admin)

![输入图片说明](https://images.gitee.com/uploads/images/2019/0305/133529_ff15f192_63154.png "01.png")
![输入图片说明](https://images.gitee.com/uploads/images/2019/0305/133537_7a1b2d85_63154.png "02.png")


## 说明文档
项目开发、部署等说明都在[wiki](https://github.com/renrenio/renren-fast-vue/wiki)中。


## 更新日志
每个版本的详细更改都记录在[release notes](https://github.com/renrenio/renren-fast-vue/releases)中。

## 我们自己的业务逻辑代码
## 1. 路由规则
在菜单管理中配置了菜单之后，路径地址对应的访问连接a/b会变成a-b ，对应的目录就是a/b.vue
例如：配置了菜单分类维护  URL为product/category  那么点击这个菜单，访问路径就是product-category 对应的vue文件就是product/category.vue

## 2. 修改基准路径，给网关发请求
##    给网关发请求，让网关统一处理ajax请求
修改 static/config/index.js文件的：window.SITE_CONFIG['baseUrl'] = 'http://localhost:8888/api';
这样修改之后，刷新页面，发现需要登录，同时，验证码已经消失了，查看network发现请求验证码的请求，请求到了网关，而发送验证码我们需要访问renren-fast项目
所以，我们可以让网关默认的将所有的请求转给renren-fast服务

## 3. 后端修改
####  3.1 将renren-fast注册到注册中心
 1）引入common依赖
 `
    <dependency>
        <groupId>com.bjc.gulimall</groupId>
        <artifactId>gulimall-common</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
 `
 2） 配置文件修改
 添加如下配置：
 `
  application:
    name: renren-fast
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
 `
 3）启动服务与发现
####  3.2 网关路由配置
    1）规则匹配配置
    `
        gateway:
              routes:
                - id: admin_route
                  uri: lb://renren-fast     # lb://renren-fast  表示负载均衡到renren-fast服务
                  predicates:
                    - Path=/api/**             # 路径匹配  这句话的意思是，从管理系统发出的任意带/api的请求都路由到admin_route
    `
    注意：这样配置还不够，因为这样配置最后的请求路径是这样的
    客户端请求路径：http://localhost:8888/captcha.jpg?uuid=59143c58-e511-4dd7-8943-8d70fac09020
    网关路由路径；http://renren-fast:8080/captcha.jpg?uuid=59143c58-e511-4dd7-8943-8d70fac09020
    但是我们真实的访问验证码的路径是http://localhost:8080/renren-fast/captcha.jpg?uuid=59143c58-e511-4dd7-8943-8d70fac09020
    这时候，我们就需要使用网关的路径重写功能
    2）路径重写配置
    在后面加上如下配置即可
    `
        filters:
                    - RewritePath=/api/(?<segment>.*),/renren-fast/$\{segment}       # 所有的路径都是以/api开始的,都重写成/renren-fast/指定的路径
    `
    再次刷新，验证码可以正确获取了，但是，登录的时候，提示403 Forbidden跨域错误了。
