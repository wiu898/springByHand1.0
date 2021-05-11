package com.gupao.spring.framework.beans;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-16 16:50
 */
public class GPBeanWrapper {

    private Object wrapperInstance;

    private Class<?> wrappedClass;

    public GPBeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrappedClass = instance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrappedClass() {
        return wrappedClass;
    }
}
