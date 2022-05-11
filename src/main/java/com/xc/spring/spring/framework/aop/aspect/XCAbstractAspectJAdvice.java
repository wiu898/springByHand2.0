package com.xc.spring.spring.framework.aop.aspect;


import org.omg.PortableServer.THREAD_POLICY_ID;

import java.lang.reflect.Method;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 6:34 PM
 */
public abstract class XCAbstractAspectJAdvice implements XCAdvice {

    private Object aspect;         //切面类

    private Method adviceMethod;   //消息通知方法

    private String throwName;      //异常方法名


    public XCAbstractAspectJAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

    public Object invokeAdviceMethod(XCJoinPoint joinPoint, Object returnValue, Throwable ex)
            throws Throwable {
        Class<?>[] paramTypes = this.adviceMethod.getParameterTypes();
        if(paramTypes == null || paramTypes.length == 0){
            return this.adviceMethod.invoke(aspect);
        }else{
            Object args[] = new Object[paramTypes.length];
            for(int i = 0; i < paramTypes.length; i++){
                if(paramTypes[i] == XCJoinPoint.class){
                    args[i] = joinPoint;
                }else if(paramTypes[i] == Throwable.class){
                    args[i] = ex;
                }else if(paramTypes[i] == Object.class){
                    args[i] = returnValue;
                }
            }
            return this.adviceMethod.invoke(aspect, args);
        }
    }

}
