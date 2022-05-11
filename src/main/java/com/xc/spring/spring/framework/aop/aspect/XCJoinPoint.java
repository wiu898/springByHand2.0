package com.xc.spring.spring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 6:30 PM
 */
public interface XCJoinPoint {

    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value );

    Object getUserAttribute(String key);

}
