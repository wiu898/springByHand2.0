package com.xc.spring.spring.framework.beans;

/**
 * XCBeanDefinition的包装类
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 11:28 AM
 */
public class XCBeanWrapper {

    private Object wrapperInstance;

    private Class<?> wrappedClass;

    public XCBeanWrapper(Object instance) {
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
