package com.gupao.spring.framework.beans.config;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 2021-01-16 15:46
 */
public class GPBeanDefinition {

    private String factoryBeanName;

    private String beanClassName;


    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
