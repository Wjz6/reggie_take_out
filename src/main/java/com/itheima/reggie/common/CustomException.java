package com.itheima.reggie.common;

/**
 * 自定义业务异常，创建异常
 */
public class CustomException extends RuntimeException{
    //传入异常信息
    public CustomException(String message){
        super(message);
    }
}
