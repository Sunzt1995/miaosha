package com.sun.error;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/14 15:57
 */
public enum EmBusinessError implements CommonError {

    //通用错误类型10001,
    PARAMETER_VALIDATION_ERROR(10001 , "参数不合法"),
    UNKNOWN_ERROR(10002 , "未知错误"),

    //20000开头为用户消息相关错误定义
    USER_NOT_EXIST(20001 , "用户不存在"),
    USER_LOGIN_FAIL(20002,"用户手机号或密码不正确"),
    USER_NOT_LOGIN(20003,"用户还未登录"),

    //30000开头为交易信息错误定义
    STOCK_NOT_ENOUGH(300001 , "库存不足"),

    ;

    private int errCode;
    private String errMsg;

    EmBusinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }

}
