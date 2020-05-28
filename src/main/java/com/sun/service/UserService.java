package com.sun.service;

import com.sun.error.BusinessException;
import com.sun.service.model.UserModel;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/14 14:39
 */
public interface UserService {

    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;

    /**
     *
     * @param telephone 用户注册手机
     * @param encrptPassword  用户加密后的密码
     * @throws BusinessException
     */
    UserModel validateLogin(String telephone , String encrptPassword) throws BusinessException;
}
