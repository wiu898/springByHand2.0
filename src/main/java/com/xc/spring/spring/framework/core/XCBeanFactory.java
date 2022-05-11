package com.xc.spring.spring.framework.core;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 9:48 AM
 */
public interface XCBeanFactory {

    public Object getBean(Class beanClass);

    public Object getBean(String beanName);

}
