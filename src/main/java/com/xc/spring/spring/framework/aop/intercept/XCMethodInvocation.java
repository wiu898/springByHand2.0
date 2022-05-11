package com.xc.spring.spring.framework.aop.intercept;

import com.xc.spring.spring.framework.aop.aspect.XCJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 6:31 PM
 */
public class XCMethodInvocation implements XCJoinPoint {

    protected final Object proxy;

    protected final Object target;

    protected final Method method;

    protected Object[] arguments = new Object[0];

    private final Class<?> targetClass;

    private Map<String, Object> userAttributes = new HashMap<String, Object>();

    protected final List<?> interceptorsAndDynamicMethodMatchers;

    private int currentInterceptorIndex = -1;

    public XCMethodInvocation(
            Object proxy,  Object target, Method method, Object[] arguments,
            Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Throwable{
        //We start with an index of -1 and increment early.
        //如果Interceptor执行完了，则执行joinPoint
        if(this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1){
            return this.method.invoke(target, this.arguments);
        }
        Object interceptorOrInterceptionAdvice
                = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        if(interceptorOrInterceptionAdvice instanceof XCMethodInterceptor){
            XCMethodInterceptor mi = (XCMethodInterceptor) interceptorOrInterceptionAdvice;
            return mi.invoke(this);
        }else{
            // Dynamic matching failed.
            // Skip this interceptor and invoke the next in the chain.
            //动态匹配失败时,略过当前Intercetpor,调用下一个Interceptor
            //递归调用,确保完成整个chain链的调用
            return proceed();
        }
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        this.userAttributes.put(key,value);
    }

    @Override
    public Object getUserAttribute(String key) {
        return this.userAttributes.get(key);
    }
}
