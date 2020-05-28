package com.sun.controller;

import com.sun.error.BusinessException;
import com.sun.error.EmBusinessError;
import com.sun.response.CommonReturnType;
import com.sun.service.OrderService;
import com.sun.service.model.OrderModel;
import com.sun.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/18 10:55
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //封装下单请求
    @RequestMapping(value = "/createorder" , method = {RequestMethod.POST} , consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId" , required = false) Integer promoId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {


        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN , "用户还未登录,不能下单");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        return CommonReturnType.create(null);

    }
}
