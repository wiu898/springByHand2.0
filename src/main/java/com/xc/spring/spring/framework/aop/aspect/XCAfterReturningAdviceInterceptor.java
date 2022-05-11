package com.xc.spring.spring.framework.aop.aspect;

import com.xc.spring.spring.framework.aop.intercept.XCMethodInterceptor;
import com.xc.spring.spring.framework.aop.intercept.XCMethodInvocation;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/26/22 4:11 PM
 */
public class XCAfterReturningAdviceInterceptor extends XCAbstractAspectJAdvice implements XCMethodInterceptor {

    private XCJoinPoint jp;

    public XCAfterReturningAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    private  void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable{
        this.invokeAdviceMethod(this.jp,returnValue,null);
    }

    @Override
    public Object invoke(XCMethodInvocation mi) throws Throwable {
        jp = mi;
        Object retVal = mi.proceed();
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }
}
