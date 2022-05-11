package com.xc.spring.spring.framework.beans.config;

/**
 * bean对象
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 9:58 AM
 */
public class XCBeanDefinition {

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
