package com.sun.service;

import com.sun.error.BusinessException;
import com.sun.service.model.OrderModel;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/17 21:11
 */
public interface OrderService {

    //1.通过前端url上传过来秒杀活动id,然后下单接口内校验对应id是否属于对应商品 ,且活动已开始 **使用这种**
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动, 若存在进行中的, 则以秒杀价格下单
    OrderModel createOrder(Integer userId , Integer itemId , Integer promoId ,  Integer amount) throws BusinessException;

    String generateOrderNo();
}
