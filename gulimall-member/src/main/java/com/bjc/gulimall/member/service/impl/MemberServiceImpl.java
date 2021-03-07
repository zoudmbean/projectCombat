package com.bjc.gulimall.member.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bjc.gulimall.member.dao.MemberLevelDao;
import com.bjc.gulimall.member.entity.MemberLevelEntity;
import com.bjc.gulimall.member.exception.PhoneExistException;
import com.bjc.gulimall.member.exception.UserNameExistException;
import com.bjc.gulimall.member.utils.HttpUtils;
import com.bjc.gulimall.member.vo.MemberLoginVo;
import com.bjc.gulimall.member.vo.MemberRegistVo;
import com.bjc.gulimall.member.vo.SocialUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.member.dao.MemberDao;
import com.bjc.gulimall.member.entity.MemberEntity;
import com.bjc.gulimall.member.service.MemberService;
import org.springframework.util.ObjectUtils;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo memberRegist) {
        MemberEntity entity = new MemberEntity();

        // 检查用户名或者手机号是否唯一
        checkPhoneUnique(memberRegist.getPhone());
        checkUserNameUnique(memberRegist.getUserName());

        // 新注册的会员，有一个默认会员等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        entity.setLevelId(memberLevelEntity.getDefaultStatus().longValue());

        entity.setMobile(memberRegist.getPhone());
        entity.setUsername(memberRegist.getUserName());

        // 设置密码（需要加密）
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(memberRegist.getPassword());
        entity.setPassword(encode);

        entity.setCreateTime(new Date());
        entity.setNickname(StringUtils.isEmpty(memberRegist.getUserName())?memberRegist.getPhone():memberRegist.getUserName());

        this.baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer selectCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(null == selectCount || selectCount > 0){
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException{
        Integer selectCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if(null == selectCount || selectCount > 0){
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", memberLoginVo.getLoginacct()).or().eq("mobile", memberLoginVo.getLoginacct()));
        // 如果数据库查询数据不为空
        if(!ObjectUtils.isEmpty(memberEntity)){
            String password = memberEntity.getPassword();
            BCryptPasswordEncoder bp = new BCryptPasswordEncoder();
            // 比较密码是否匹配
            boolean matches = bp.matches(memberLoginVo.getPassword(), memberEntity.getPassword());
            // 匹配才返回
            if(matches){
                return memberEntity;
            }
        }
        return null;
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        // 1. 判断用户是否存在
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getUid()));
        if(null != memberEntity){ // 表示已存在，更新
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());

            this.baseMapper.updateById(update);

            // 更新memberEntity的社交信息
            memberEntity.setAccessToken(socialUser.getAccess_token());
            return memberEntity;
        } else {    // 表示不存在，注册
            MemberEntity update = new MemberEntity();
            update.setCreateTime(new Date());
            update.setAccessToken(socialUser.getAccess_token());
            update.setSocialUid(socialUser.getUid());
            // 设置默认会员等级
            MemberLevelEntity memberLevelEntity = memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
            update.setLevelId(memberLevelEntity.getDefaultStatus().longValue());
            // 根据token查询社交账号的信息（昵称、性别。。。）
            try {
                Map<String,String> map = new HashMap<>();
                map.put("access_token",socialUser.getAccess_token());
                map.put("uid",socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), map);
                if(response.getStatusLine().getStatusCode() == 200){
                    // 查询成功
                    HttpEntity entity = response.getEntity();
                    String jsonStr = EntityUtils.toString(entity);
                    // 获取返回对象的JSON对象
                    JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                    // 获取用户信息
                    String nikName = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    String location = jsonObject.getString("location");
                    update.setNickname(nikName);
                    update.setGender(StringUtils.equalsIgnoreCase("m",gender) ? 1 : 0);
                    update.setCity(location);
                }
            } catch (Exception e) {
                log.error("查询社交用户信息异常：",e);
            }
            this.baseMapper.insert(update);
            return update;
        }
    }

}
