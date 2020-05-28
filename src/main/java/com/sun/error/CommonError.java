package com.sun.error;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/14 15:55
 */
public interface CommonError {

    public int getErrCode();

    public String getErrMsg();

    public CommonError setErrMsg(String errMsg);
}
