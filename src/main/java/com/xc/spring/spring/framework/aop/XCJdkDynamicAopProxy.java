package com.xc.spring.spring.framework.aop;

import com.xc.spring.spring.framework.aop.intercept.XCMethodInvocation;
import com.xc.spring.spring.framework.aop.support.XCAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 6:23 PM
 */
public class XCJdkDynamicAopProxy implements XCAopProxy, InvocationHandler {

    private XCAdvisedSupport advised;

    public XCJdkDynamicAopProxy(XCAdvisedSupport config) {
        this.advised = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //获取可以应用到此方法上的Interceptor列表
        List<Object> chain = this.advised
                .getInterceptorsAndDynamicInterceptionAdvice(method, this.advised.getTargetClass());
        XCMethodInvocation mi = new XCMethodInvocation(proxy, this.advised.getTarget(),
                method, args, this.advised.getTargetClass(), chain);
        return mi.proceed();
    }



    @Override
    public Object getProxy() {
        return getProxy(this.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.advised.getTargetClass().getInterfaces(), this);
    }


}
