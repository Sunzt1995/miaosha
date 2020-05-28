package com.sun.service;

import com.sun.service.model.PromoModel;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/18 16:42
 */
public interface PromoService {

    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

}

