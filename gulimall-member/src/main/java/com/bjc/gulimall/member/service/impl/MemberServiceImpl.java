package com.bjc.gulimall.member.service.impl;

import com.bjc.gulimall.member.dao.MemberLevelDao;
import com.bjc.gulimall.member.entity.MemberLevelEntity;
import com.bjc.gulimall.member.exception.PhoneExistException;
import com.bjc.gulimall.member.exception.UserNameExistException;
import com.bjc.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.Query;

import com.bjc.gulimall.member.dao.MemberDao;
import com.bjc.gulimall.member.entity.MemberEntity;
import com.bjc.gulimall.member.service.MemberService;


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

}
