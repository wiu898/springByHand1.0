package com.gupao.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-21 16:12
 */
@Data
public class GPAdvice {

    private Object aspect;      //切面类

    private Method adviceMethod;  //消息通知方法

    private String throwName;   //异常方法名


    public GPAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
