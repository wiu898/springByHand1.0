package com.gupao.spring.framework.core;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-25 22:31
 */
public interface GPBeanFactory {

    public Object getBean(Class beanClass);

    public Object getBean(String beanName);

}
