package com.xc.spring.spring.framework.aop.intercept;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 6:30 PM
 */
public interface XCMethodInterceptor {

    Object invoke(XCMethodInvocation invocation) throws Throwable;

}
