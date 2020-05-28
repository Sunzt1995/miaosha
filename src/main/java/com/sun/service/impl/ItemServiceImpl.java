package com.sun.service.impl;

import com.sun.dao.ItemDOMapper;
import com.sun.dao.ItemStockDOMapper;
import com.sun.dataobject.ItemDO;
import com.sun.dataobject.ItemStockDO;
import com.sun.error.BusinessException;
import com.sun.error.EmBusinessError;
import com.sun.service.ItemService;
import com.sun.service.PromoService;
import com.sun.service.model.ItemModel;
import com.sun.service.model.PromoModel;
import com.sun.validator.ValidationResult;
import com.sun.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/15 19:52
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;


    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , result.getErrorMsg());
        }
        //转化itemmodel -> dataobject
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = this.convertItemStockFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOS = itemDOMapper.listItem();

        List<ItemModel> itemModelList = itemDOS.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null) {
            return null;
        }
        //操作获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //将dataobject -> model
        ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus().intValue() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
        return affectedRow > 0 ? true : false;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId , amount);
    }


    private ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if(itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel , itemDO);
        return itemDO;
    }

    private ItemStockDO convertItemStockFromItemModel(ItemModel itemModel){
        if(itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return  itemStockDO;
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO , ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO , itemModel);
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }
}
