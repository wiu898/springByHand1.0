package com.gupao.spring.framework.aop.support;

import com.gupao.spring.framework.aop.GPAdvisedSupport;
import com.gupao.spring.framework.aop.aspect.GPAdvice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-21 17:07
 */
public class GPJdkDynamicAopProxy implements InvocationHandler {

    private GPAdvisedSupport config;

    public GPJdkDynamicAopProxy(GPAdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, GPAdvice> advices = config.getAdvices(method,null);
        Object returnValue;
        try{
            invokeAdvice(advices.get("before"));

            //真正的业务逻辑
            returnValue = method.invoke(this.config.getTarget(),args);

            invokeAdvice(advices.get("after"));
        }catch (Exception e){
            invokeAdvice(advices.get("afterThrow"));
            throw e;
        }
        return returnValue;
    }

    private void invokeAdvice(GPAdvice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                this.config.getTargetClass().getInterfaces(),this);
    }
}
