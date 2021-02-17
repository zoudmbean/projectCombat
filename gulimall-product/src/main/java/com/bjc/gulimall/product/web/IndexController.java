package com.bjc.gulimall.product.web;

import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.CategoryService;
import com.bjc.gulimall.product.vo.Category2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {
        // 1. 获取信号量
        RSemaphore semaphore = redisson.getSemaphore("park");
        // 2. 获取一个信号量，获取一个值，获取到之后，才执行下面的逻辑，如果获取不到，就一直等待，即该方法是一个阻塞方法
        // 可以理解为占一个车位
        semaphore.acquire();

        // 能获取到返回true，不能获取到false，也就是非阻塞式的
        // boolean tryAcquire = semaphore.tryAcquire();
        return "ok!";
    }

    @ResponseBody
    @GetMapping("/leave")
    public String leave(){
        // 1. 获取信号量
        RSemaphore semaphore = redisson.getSemaphore("park");
        // 2. 释放一个信号
        // 可以理解为停在车位的某辆车开走了
        semaphore.release();
        return "leave!";
    }

    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        // 1. 获取闭锁
        RCountDownLatch countDownLatch = redisson.getCountDownLatch("door");
        // 2. 设置闭锁计量
        countDownLatch.trySetCount(5);
        // 3. 等待
        countDownLatch.await();
        // 4. 执行后面的逻辑
        return "下班了！";
    }

    @ResponseBody
    @GetMapping("/goHome/{id}")
    public String goHome(@PathVariable("id") Long id){
        // 1. 获取闭锁
        RCountDownLatch countDownLatch = redisson.getCountDownLatch("door");
        // 2. 计数器减1
        countDownLatch.countDown();
        // 3. 执行业务逻辑
        return id + "的职员都离开了！";
    }

    @ResponseBody
    @GetMapping("/write")
    public String writeValue(){
        String str = "";
        // 1. 获取读写锁
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        // 2. 得到写锁
        RLock writeLock = readWriteLock.writeLock();
        try {
            // 加锁
            writeLock.lock(10,TimeUnit.SECONDS);
            str = UUID.randomUUID().toString();
            Thread.sleep(10000);
            redisTemplate.opsForValue().set("writeValue",str);
        } catch (Exception e) {

        } finally {
            // 释放锁资源
            writeLock.unlock();
        }

        return str;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue(){
        String value = "";
        // 1. 获取读写锁
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        // 2. 得到读锁
        RLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            value = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {

        } finally {
            readLock.unlock();
        }

        return value;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        // 1. 获取锁  参数为锁名称  只要锁名称一样，就是同一把锁
        //      RLock 继承了JUC中的Lock接口，因此我们可以在分布式环境中像使用本地锁一样的使用
        RLock lock = redisson.getLock("my_lock");

        // 2. 加锁
        lock.lock();
        lock.lock(10, TimeUnit.SECONDS);    // 10秒自动解锁，自动解锁的时间一定要大于业务执行的时间   在锁时间到了以后，不会自动续期
        /*
        * 1）锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s，不用担心业务时间长，锁自动过期被删除掉
        * 2）加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除
        * */
        try {
            // 3. 执行业务逻辑
            System.out.println(Thread.currentThread().getName() + "  - 加锁成功，执行业务！");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(Thread.currentThread().getName() + "  - 释放锁！");
            // 4. 解锁
            lock.unlock();
        }
        return "hello";
    }

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        // 1. 查询所有的一级分类
        List<CategoryEntity> list =  categoryService.getLevel1Categorys();
        model.addAttribute("categorys",list);
        // 默认前缀后缀都要，所以只需要写文件名，视图解析器就会自动进行拼串
        return "index";
    }

    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String, List<Category2Vo>> getCatagoryJson(){
        Map<String, List<Category2Vo>> map = categoryService.getCatagoryJson();
        return map;
    }

}
