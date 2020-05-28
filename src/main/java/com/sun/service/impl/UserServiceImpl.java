package com.sun.service.impl;

import com.sun.dao.UserDOMapper;
import com.sun.dao.UserPasswordDOMapper;
import com.sun.dataobject.UserDO;
import com.sun.dataobject.UserPasswordDO;
import com.sun.error.BusinessException;
import com.sun.error.EmBusinessError;
import com.sun.service.UserService;
import com.sun.service.model.UserModel;
import com.sun.validator.ValidationResult;
import com.sun.validator.ValidatorImpl;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/14 14:40
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO , userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

//        if(StringUtils.isEmpty(userModel.getName())
//                || userModel.getGender() == null
//                || userModel.getAge() == null
//                || StringUtils.isEmpty(userModel.getTelephone())) {
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }

        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , result.getErrorMsg());
        }


        //实现model -> dataobject方法
        UserDO userDO = convertFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR , "手机号已重复注册");
        }

        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;

    }

    @Override
    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException {
        //通过用户的手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO , userPasswordDO);

        //比对用户的加密密码
        if(!StringUtils.equals(encrptPassword , userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;

    }


    private UserPasswordDO convertPasswordFromModel(UserModel userModel) {
        if(userModel == null) {
            return  null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());

        return userPasswordDO;
    }

    private UserDO convertFromModel(UserModel userModel) {
        if(userModel == null) {
            return  null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel , userDO);

        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO , UserPasswordDO userPasswordDO) {
        if(userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO , userModel);

        if(userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
