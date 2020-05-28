package com.sun.service.impl;

import com.sun.dao.OrderDOMapper;
import com.sun.dao.SequenceDOMapper;
import com.sun.dataobject.OrderDO;
import com.sun.dataobject.SequenceDO;
import com.sun.error.BusinessException;
import com.sun.error.EmBusinessError;
import com.sun.service.ItemService;
import com.sun.service.OrderService;
import com.sun.service.UserService;
import com.sun.service.model.ItemModel;
import com.sun.service.model.OrderModel;
import com.sun.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/17 21:12
 */
@Service
public class OrderServiceIpml implements OrderService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId , Integer amount) throws BusinessException {
        //1.校验下单状态,下单的商品是否存在,用户是否合法,购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , "商品信息不存在");
        }

        UserModel userModel = userService.getUserById(userId);
        if(userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , "用户信息不存在") ;
        }

        if(amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , "数量信息不正确") ;
        }

        //检验活动信息
        if(promoId != null) {
            //1.校验对应活动是否存在这个使用适用商品
            if(promoId.intValue() != itemModel.getPromoModel().getId().intValue()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , "活动信息不正确") ;
            //2.检验活动是否正在进行中
            }else if(itemModel.getPromoModel().getStatus().intValue() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , "活动还未开始") ;
            }
        }

        //2.落单减库存,(还有支付减库存)
        boolean result = itemService.decreaseStock(itemId, amount);
        if(!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号
        orderModel.setId(orderService.generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //加上商品的销量
        itemService.increaseSales(itemId , amount);

        //返回前端
        return  orderModel;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo() {
        //订单号有16位
        StringBuilder sb = new StringBuilder();
        //前8位为时间信息,年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(nowDate);

        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+ sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

        String seqStr = String.valueOf(sequence);
        for(int i = 0 ; i < 6 - seqStr.length() ; i++) {
            sb.append(0);
        }
        sb.append(seqStr);

        //最后2位为分库分表位,暂时写死
        sb.append("00");
        return  sb.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if(orderModel == null) {
            return  null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel , orderDO);
        return orderDO;
    }
}
