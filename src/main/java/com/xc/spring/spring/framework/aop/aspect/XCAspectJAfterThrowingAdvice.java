package com.xc.spring.spring.framework.aop.aspect;

import com.xc.spring.spring.framework.aop.intercept.XCMethodInterceptor;
import com.xc.spring.spring.framework.aop.intercept.XCMethodInvocation;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/26/22 4:34 PM
 */
public class XCAspectJAfterThrowingAdvice extends XCAbstractAspectJAdvice implements XCMethodInterceptor {

    private String throwName;

    public XCAspectJAfterThrowingAdvice(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    @Override
    public Object invoke(XCMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }
        catch (Throwable ex) {
            invokeAdviceMethod(mi , null, ex);
            throw ex;
        }
    }


    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }
}
