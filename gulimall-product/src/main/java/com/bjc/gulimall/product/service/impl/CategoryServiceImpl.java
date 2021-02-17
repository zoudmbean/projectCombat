package com.bjc.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;
import com.bjc.gulimall.product.dao.CategoryBrandRelationDao;
import com.bjc.gulimall.product.dao.CategoryDao;
import com.bjc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.bjc.gulimall.product.entity.CategoryEntity;
import com.bjc.gulimall.product.service.CategoryService;
import com.bjc.gulimall.product.vo.Category2Vo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {

        // 1. 获取所有的分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. 获取所有的一级分类
        List<CategoryEntity> level1 = entities.stream()
                .filter(c -> c.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
                })
                .sorted((m1,m2) -> Optional.ofNullable(m1.getSort()).orElse(0)-Optional.ofNullable(m2.getSort()).orElse(0))  // sort的值可能为null
                .collect(Collectors.toList());

        return level1;
    }

    /*
    * 逻辑删除
    * */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO  一些业务逻辑

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
   /* @Caching(evict = {
            @CacheEvict(value={"category"},key = "'getLevel1Categorys'"),
            @CacheEvict(value={"category"},key = "'getCatagoryJson'")
    })*/
    @CacheEvict(value = {"category"},allEntries = true)
    @Transactional
    public void updateCategory(CategoryEntity category) {
        // 1. 修改分类表
        baseMapper.updateById(category);
        // 2. 修改品牌分类关系表
        if(StringUtils.isNotEmpty(category.getName())){
            CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
            entity.setCatelogId(category.getCatId());
            entity.setCatelogName(category.getName());
            QueryWrapper<CategoryBrandRelationEntity> wraper = new QueryWrapper();
            wraper.eq("catelog_id",category.getCatId());
            categoryBrandRelationDao.update(entity,wraper);
        }

        // 3. TODO 修改其他分类关系
    }

    /*
    * 根据当前菜单获取其子菜单
    * */
    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> entities) {
        List<CategoryEntity> children = entities.stream()
                .filter(c -> c.getParentCid().longValue() == categoryEntity.getCatId().longValue())  // 这里转成long型来比较值相等，否则值过大的时候就恒不等了
                .map(menu -> {
                    menu.setChildren(getChildren(menu,entities));
                    return menu;
                })
                .sorted((m1,m2) -> Optional.ofNullable(m1.getSort()).orElse(0)-Optional.ofNullable(m2.getSort()).orElse(0))  // sort的值可能为null
                .collect(Collectors.toList());
        return children;
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    // 每个需要缓存的数据都来指定要放到那个名字的缓存【缓存的分区】
    // @Cacheable(value={"category"},key = "#root.methodName")  // 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用，如果缓存中没有，会调用方法，最后将方法的结果放入缓存
    @Cacheable(value={"category"},key = "#root.methodName")  // 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用，如果缓存中没有，会调用方法，最后将方法的结果放入缓存
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys...");
        List<CategoryEntity> entities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    @Cacheable(value="category",key="#root.methodName")
    @Override
    public Map<String, List<Category2Vo>> getCatagoryJson() {
        /*
         * 将数据库多次查询改成一次查询
         * */
        List<CategoryEntity> allList = baseMapper.selectList(null);

        // 1. 查出所有1级分类
        // List<CategoryEntity> levelaCategorys = getLevel1Categorys();
        List<CategoryEntity> levelaCategorys = getParentIds(allList, 0L);
        // 封装数据
        Map<String, List<Category2Vo>> listMap = levelaCategorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1. 每一个的一级分类   查到这个一级分类的二级分类
            List<CategoryEntity> entities = getParentIds(allList, v.getCatId());

            // 2. 将查询到的list转成指定格式的list
            List<Category2Vo> category2Vos = Optional.ofNullable(entities).orElse(new ArrayList<>()).stream().map(l2 -> {
                Category2Vo category2Vo = new Category2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                // 当前2级分类的3级分类封装成VO
                List<Category2Vo.CateLog3Vo> cateLog3Vos = Optional.ofNullable(getParentIds(allList, l2.getCatId())).orElse(new ArrayList<>())
                        .stream().map(l3 -> {
                            Category2Vo.CateLog3Vo log3Vo = new Category2Vo.CateLog3Vo(l2.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return log3Vo;
                        }).collect(Collectors.toList());
                category2Vo.setCatalog3List(cateLog3Vos);
                return category2Vo;
            }).collect(Collectors.toList());
            return category2Vos;
        }));
        return listMap;
    }

    // 缓存数据都存json串，好处是跨语言，跨平台兼容
    // TODO 产生堆外内存溢出：io.netty.util.internal.OutOfDirectMemoryError
    // 产生原因：
    //  1）boot2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
    //  2）lettuce的bug导致堆外内存溢出。netty如果没有指定堆外内存，默认使用-Xmx指定的值作为堆内存
    //  3）所以解决办法是通过-Dio.netty.maxDirectMemory来设置堆外内存大小，但是不论设置多大，只是延迟该异常出现的时间而已，所以，根本原因是内存未得到及时释放。
    // 解决方案：
    //      1. 升级lettuce客户端（优点：使用netty作为底层的网络框架，吞吐量很大）
    //      2. 切换使用jedis客户端（优点：稳定，缺点：好久没更新了）
    public Map<String, List<Category2Vo>> getCatagoryJson1() {
        String cataforyJson = null;

        /*
        * 1. 空结果缓存，解决缓存穿透问题
        * 2. 设置过期时间，过期时间用随机值，解决缓存雪崩
        * 3. 加锁，解决缓存击穿问题
        * */

        try {
            // 为了不影响正常逻辑，这里用异常判断
            cataforyJson = redisTemplate.opsForValue().get("cataforyJson");
        } catch (Exception e) {
            log.error("从redis获取分类数据失败，失败原因：",e);
        }

        if(StringUtils.isNotEmpty(cataforyJson)){
            Map<String, List<Category2Vo>> map = JSON.parseObject(cataforyJson, new TypeReference<Map<String, List<Category2Vo>>>(){});
            return map;
        }
        // 缓存没有，从数据库查询
        Map<String, List<Category2Vo>> jsonFormDb = this.getCatagoryJsonWidthRedisLock();
        /*
        注释掉，将保存缓存的逻辑写在查询出数据之后就保存，也就是写在方法getCatagoryJsonFormDb中

        // 然后保存在redis中
        try {
            // 为了不影响正常逻辑，这里用异常判断
            redisTemplate.opsForValue().set("cataforyJson", JSONObject.toJSONString(jsonFormDb),Long.valueOf(Math.random()*10+""), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("保存分类数据到redis数据失败，失败原因：",e);
        }
        */
        return jsonFormDb;
    }

    /* 使用redisson */
    public Map<String, List<Category2Vo>> getCatagoryJsonWidthRedisson() {
        // 1. 要注意锁的名字，只要锁的名字一样，就是同一把锁
        // 锁的名字可以决定锁的粒度，锁的粒度越细越快
        // 锁的粒度：具体缓存的是某个数据，那么锁的粒度要越细越好
        RLock rLock = redisson.getLock("catagoryJson-lock");
        rLock.lock();
        Map<String, List<Category2Vo>> datasFromDb = null;
        try {
            // 加锁成功，执行业务逻辑
            datasFromDb = getDatasFromDb();
        } catch (Exception e) {
        } finally {
            // 释放锁
            rLock.unlock();
        }
        return datasFromDb;
    }

    /* 从redis获取数据 */
    public Map<String, List<Category2Vo>> getCatagoryJsonWidthRedisLock() {
        // 占分布式锁，其redis占坑  setIfAbsent 就是 set的NX  设置过期时间  30秒后自动删除
        String lockUuid = UUID.randomUUID().toString();
        Boolean iflocked = redisTemplate.opsForValue().setIfAbsent("lock", lockUuid,30,TimeUnit.SECONDS);
        if(iflocked){
            Map<String, List<Category2Vo>> datasFromDb = null;
            try {
                // 加锁成功，执行业务逻辑
                datasFromDb = getDatasFromDb();
            } catch (Exception e) {
            } finally {
                // 查询数据之后，删除lock
                // 自己的锁才允许删除
                // String lockVal = redisTemplate.opsForValue().get("lock");
                /*
                if(StringUtils.equals(lockVal,lockUuid)){
                    redisTemplate.delete("lock");
                }
                */
                // 使用redis官方的lua脚本原子性操作
                String script ="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 执行脚本删除
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), lockUuid);
            }
            return datasFromDb;
        } else {
            // 加锁失败，睡眠100毫秒，然后 自旋
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {}
            // 自旋
            return getCatagoryJsonWidthRedisLock();
        }
    }

    /* 从数据库获取数据 */
    private Map<String, List<Category2Vo>> getDatasFromDb() {
        String cataforyJson;// 得到锁以后，我么应该再去缓存中查询确定一次，如果没有才需要进行查询
        try {
            // 为了不影响正常逻辑，这里用异常判断
            cataforyJson = redisTemplate.opsForValue().get("cataforyJson");
            if (StringUtils.isNotEmpty(cataforyJson)) {   // 如果缓存有数据了，那么直接返回数据
                Map<String, List<Category2Vo>> map = JSON.parseObject(cataforyJson, new TypeReference<Map<String, List<Category2Vo>>>() {
                });
                return map;
            }
        } catch (Exception e) {
            log.error("从redis获取分类数据失败，失败原因：", e);
        }
        /*
         * 将数据库多次查询改成一次查询
         * */
        List<CategoryEntity> allList = baseMapper.selectList(null);

        // 1. 查出所有1级分类
        // List<CategoryEntity> levelaCategorys = getLevel1Categorys();
        List<CategoryEntity> levelaCategorys = getParentIds(allList, 0L);
        // 封装数据
        Map<String, List<Category2Vo>> listMap = levelaCategorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1. 每一个的一级分类   查到这个一级分类的二级分类
            List<CategoryEntity> entities = getParentIds(allList, v.getCatId());

            // 2. 将查询到的list转成指定格式的list
            List<Category2Vo> category2Vos = Optional.ofNullable(entities).orElse(new ArrayList<>()).stream().map(l2 -> {
                Category2Vo category2Vo = new Category2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                // 当前2级分类的3级分类封装成VO
                List<Category2Vo.CateLog3Vo> cateLog3Vos = Optional.ofNullable(getParentIds(allList, l2.getCatId())).orElse(new ArrayList<>())
                        .stream().map(l3 -> {
                            Category2Vo.CateLog3Vo log3Vo = new Category2Vo.CateLog3Vo(l2.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return log3Vo;
                        }).collect(Collectors.toList());
                category2Vo.setCatalog3List(cateLog3Vos);
                return category2Vo;
            }).collect(Collectors.toList());
            return category2Vos;
        }));

        // 然后保存在redis中
        try {
            // 为了不影响正常逻辑，这里用异常判断
            redisTemplate.opsForValue().set("cataforyJson", JSONObject.toJSONString(listMap), Long.valueOf(Math.random() * 10 + ""), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("保存分类数据到redis数据失败，失败原因：", e);
        }

        return listMap;
    }

    /*
    * 直接从数据库获取数据，优化之后改成上面的通过redis获取数据
    * */
    public Map<String, List<Category2Vo>> getCatagoryJsonWidthLocalDb() {

        /*
        * 只要是同一把锁，就能锁住需要这个锁的所有线程
        * 1）synchronized (this) 在boot所有组件在容器中都是单例的，因此可以使用this，this就指代容器中的CategoryServiceImpl对象
        * */
        String cataforyJson = null;
        // TODO 本地锁，synchronized，JUC（lock）
        synchronized (this){
            // 得到锁以后，我么应该再去缓存中查询确定一次，如果没有才需要进行查询
            return getDatasFromDb();
        }
    }

    private List<CategoryEntity> getParentIds(List<CategoryEntity> allList,long parent_cid){
        // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", parent_cid));
        return allList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
    }


    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }

}
