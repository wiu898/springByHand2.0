package com.xc.spring.spring.framework.beans.support;

import com.xc.spring.spring.framework.beans.config.XCBeanDefinition;
import com.xc.spring.spring.framework.core.XCBeanFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 11:08 AM
 */
public class XCDefaultListableBeanFactory implements XCBeanFactory {

    public Map<String, XCBeanDefinition> beanDefinitionMap = new HashMap<String, XCBeanDefinition>();

    @Override
    public Object getBean(Class beanClass) {
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }

    /*
     * 缓存Map格式的BeanDefinition
     */
    public void doRegistBeanDefinition(List<XCBeanDefinition> beanDefinitions) throws Exception {
        for(XCBeanDefinition beanDefinition : beanDefinitions){
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
        }
    }
}
