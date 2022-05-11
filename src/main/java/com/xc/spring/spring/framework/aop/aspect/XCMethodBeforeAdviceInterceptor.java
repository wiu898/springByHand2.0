package com.xc.spring.spring.framework.aop.aspect;

import com.xc.spring.spring.framework.aop.intercept.XCMethodInterceptor;
import com.xc.spring.spring.framework.aop.intercept.XCMethodInvocation;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/26/22 4:02 PM
 */
public class XCMethodBeforeAdviceInterceptor extends XCAbstractAspectJAdvice implements XCMethodInterceptor {

    private XCJoinPoint jp;

    public XCMethodBeforeAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    public void before(Method method, Object[] arguments, Object aThis) throws Throwable{
        invokeAdviceMethod(this.jp, null, null);
    }

    @Override
    public Object invoke(XCMethodInvocation mi) throws Throwable {
        jp = mi;
        this.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
