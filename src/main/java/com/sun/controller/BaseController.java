package com.sun.controller;

/**
 * @author Sunzt
 * @version 1.0
 * @date 2020/5/14 17:01
 */
public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    //定义exceptionhandler解决未被controller层吸收的exception
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
//    public Object handlerException(HttpServletRequest request , Exception ex) {
//        Map<String , Object> responseData = new HashMap<>();
//        if(ex instanceof BusinessException) {
//            BusinessException businessException = (BusinessException) ex;
//            responseData.put("errCode" , businessException.getErrCode());
//            responseData.put("errMsg" , businessException.getErrMsg());
//        }else {
//            responseData.put("errCode" , EmBusinessError.UNKNOWN_ERROR.getErrCode());
//            responseData.put("errMsg" , EmBusinessError.UNKNOWN_ERROR.getErrMsg());
//        }
//        return CommonReturnType.create(responseData , "fail");
//    }
}
