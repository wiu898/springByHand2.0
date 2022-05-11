package com.xc.spring.spring.framework.aop;

import com.xc.spring.spring.framework.aop.support.XCAdvisedSupport;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 11:54 AM
 */
public class XCDefaultAopProxyFactory {

    public XCAopProxy createAopProxy(XCAdvisedSupport config) throws Exception{
        Class targetClass = config.getTargetClass();
        if(targetClass.getInterfaces().length > 0){
            return new XCJdkDynamicAopProxy(config);
        }
        return new XCCglibAopProxy();
    }

}
