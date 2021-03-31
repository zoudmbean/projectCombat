package com.bjc.gulimall.order.web;

import com.bjc.common.exception.NoStockException;
import com.bjc.gulimall.order.entity.OrderEntity;
import com.bjc.gulimall.order.service.OrderService;
import com.bjc.gulimall.order.vo.OrderConfirmVo;
import com.bjc.gulimall.order.vo.OrderSubmitVo;
import com.bjc.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @描述：订单服务页面跳转
 * @创建时间: 2021/3/19
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo orderVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData",orderVo);

        return "confirm";
    }

    // 提交订单
    @PostMapping("/submitOrder")
    public String submitOrder(Model model, OrderSubmitVo orderSubmitVo, RedirectAttributes redirectAttributes){
        // 去创建订单、验令牌、验价格、锁定库存
        String msg = "下单失败：";
        try{
            SubmitOrderResponseVo resVO= orderService.submitOrder(orderSubmitVo);
            if(resVO.getCode() == 0){
                // 下单成功，跳转到支付页面
                model.addAttribute("submitOrderResp",resVO);
                return "pay";
            }
            switch (resVO.getCode()){
                case 1:
                    msg += "订单信息过期，请刷新再次提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化，请确认后再次提交";
                    break;
                case 3:
                    msg += "库存锁定失败，商品库存不足";
                    break;

            }
        }catch (NoStockException e1) {
            msg += e1.getMessage();
        } catch (Exception e2) {
            msg += "系统异常";
        }
        // 错误信息存入session
        redirectAttributes.addFlashAttribute("msg",msg);
        // 下单失败，回到订单确认页，重新确认订单信息
        return "redirect:http://order.gulimall.com/toTrade";
    }

}
