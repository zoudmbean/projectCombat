package com.bjc.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

// import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.bjc.common.enums.BizCodeEnume;
import com.bjc.gulimall.member.exception.PhoneExistException;
import com.bjc.gulimall.member.exception.UserNameExistException;
import com.bjc.gulimall.member.feign.CouponFeignService;
import com.bjc.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bjc.gulimall.member.entity.MemberEntity;
import com.bjc.gulimall.member.service.MemberService;
import com.bjc.common.utils.PageUtils;
import com.bjc.common.utils.R;



/**
 * 会员
 *
 * @author zoudm
 * @email zoudmbean@163.com
 * @date 2020-12-08 23:25:27
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @GetMapping("/coupons")
    public R test(){
        return R.ok().put("member",new MemberEntity().setNickname("张三"))
                    .put("coupons",couponFeignService.memberCoupons().get("coupons"));
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegist){
        try {
            memberService.regist(memberRegist);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UserNameExistException e){
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        } catch (Exception e){
            return R.error(500, "未知异常");
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")  // shiro注解
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
