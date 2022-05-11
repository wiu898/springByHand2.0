package com.xc.spring.spring.framework.context;

import com.xc.spring.spring.framework.annotation.XCAutowired;
import com.xc.spring.spring.framework.annotation.XCController;
import com.xc.spring.spring.framework.annotation.XCService;
import com.xc.spring.spring.framework.aop.XCDefaultAopProxyFactory;
import com.xc.spring.spring.framework.aop.config.XCAopConfig;
import com.xc.spring.spring.framework.aop.support.XCAdvisedSupport;
import com.xc.spring.spring.framework.beans.XCBeanWrapper;
import com.xc.spring.spring.framework.beans.config.XCBeanDefinition;
import com.xc.spring.spring.framework.beans.support.XCBeanDefinitionReader;
import com.xc.spring.spring.framework.beans.support.XCDefaultListableBeanFactory;
import com.xc.spring.spring.framework.core.XCBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 职责：完成Bean的创建和DI
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 9:52 AM
 */
public class XCApplicationContext implements XCBeanFactory {

    private XCBeanDefinitionReader reader;

    private XCDefaultListableBeanFactory regitry = new XCDefaultListableBeanFactory();

    //循环依赖的标识，当前正在创建的BeanName，Mark一下
    private Set<String> singletonsCurrentlyInCreation = new HashSet<String>();

    //一级缓存：保存成熟的Bean
    private Map<String,Object> singletonObjects = new HashMap<String, Object>();

    //二级缓存：保存早期的Bean
    private Map<String,Object> earlySingletonObjects = new HashMap<String, Object>();

    //三级缓存（终极缓存）
    private Map<String,XCBeanWrapper> factoryBeanInstanceCache = new HashMap<String, XCBeanWrapper>();
    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();

    private XCDefaultAopProxyFactory proxyFactory = new XCDefaultAopProxyFactory();

    public XCApplicationContext(String... configLocations) {

        //1.通过 BeanDefinitionReader 加载配置文件
        reader = new XCBeanDefinitionReader(configLocations);
        try{
            //2.通过 BeanDefinitionReader 解析配置文件，封装成 Spring内部的 BeanDefinition
            List<XCBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

            //3.缓存BeanDefinition
            regitry.doRegistBeanDefinition(beanDefinitions);

            //4.执行依赖注入
            doAutowrited();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /*
     * 依赖注入方法
     * 此处所有的bean并没有真正的实例化，还只是配置阶段
     */
    private void doAutowrited() {
        //此处所有的bean并没有真正的实例化，还只是配置阶段
        for(Map.Entry<String,XCBeanDefinition> beanDefinitionEntry
                : regitry.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            //调用getBean触发依赖注入-这里才是bean真正注入的地方
            getBean(beanName);
        }
    }


    @Override
    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }

    /*
     * 依赖注入方法
     * Bean的实例化，DI是从这个方法真正开始的
     */
    @Override
    public Object getBean(String beanName) {
        //1.拿到BeanDefinition配置信息
        XCBeanDefinition beanDefinition = regitry.beanDefinitionMap.get(beanName);

        /* 可能涉及到循环依赖
         * 解决方案：用两个缓存，一级缓存和二级缓存循环两次
         * 1.把第一次读取结果为空的BeanDefinition存到第一个缓存
         * 2.等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值
         */
        //从缓存中获取初始化好的bean
        Object singleton = getSingleton(beanName,beanDefinition);
        if(singleton != null){
            return singleton;
        }

        //标记一级缓存中不存在并且当前正在创建的beanName
        if(!singletonsCurrentlyInCreation.contains(beanName)){
            singletonsCurrentlyInCreation.add(beanName);
        }


        //2.通过反射实例化对象，并放入缓存
        Object instance = instantiateBean(beanName,beanDefinition);
        //存入一级缓存
        singletonObjects.put(beanName,instance);

        //3.将实例化的 BeanDefinition对象 封装成BeanWrapper
        XCBeanWrapper beanWrapper = new XCBeanWrapper(instance);
        //4.执行依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);
        //5.将BeanWrapper保存到IOC容器
        factoryBeanInstanceCache.put(beanName,beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    /*
     * 从缓存中获取初始化的bean信息,
     * 解决循环依赖问题
     */
    private Object getSingleton(String beanName, XCBeanDefinition beanDefinition) {
        //1.从一级缓存中获取bean
        Object bean = singletonObjects.get(beanName);
        //一级缓存中不存在，并且存在创建标识。则为循环依赖
        if(bean == null && singletonsCurrentlyInCreation.contains(beanName)){
            //2.从二级缓存中获取bean
            bean = earlySingletonObjects.get(beanName);
            //如果二级缓存也没有，从三级缓存中获取
            if(bean == null){
                //3.从三级缓存中获取，没有就会创建
                bean = instantiateBean(beanName, beanDefinition);
                //将三级缓存中获取的对象放入二级缓存(避免后续相同对象的多次创建)
                earlySingletonObjects.put(beanName, bean);
            }
        }
        return bean;
    }

    //

    /*
     * 依赖注入真正执行的方法
     */
    private void populateBean(String beanName, XCBeanDefinition beanDefinition, XCBeanWrapper beanWrapper) {

        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = beanWrapper.getWrappedClass();

        //在Spring中是 @Component
        if((!clazz.isAnnotationPresent(XCController.class)
                || clazz.isAnnotationPresent(XCService.class))){
            return;
        }

        //获取类下的所有属性 包括private/protected/default/public 修饰的字段
        //比如 @Autowired private IService service;
        for (Field field : clazz.getDeclaredFields()) {
            //如果没有声明@Autowired直接跳过不处理
            if(!field.isAnnotationPresent(XCAutowired.class)){
                continue;
            }
            //获取@Autowired注解
            XCAutowired autowired = field.getAnnotation(XCAutowired.class);
            //获取@GPAutowired注解中的值
            String autowiredBeanName = autowired.value().trim();
            //如果用户没有自定义的beanName,就默认根据类型注入
            if("".equals(autowiredBeanName)){
                //获取字段类型 field.getType().getName(),得到接口全名
                autowiredBeanName = field.getType().getName() ;
            }
            //暴力访问 比如 private属性
            field.setAccessible(true);
            try {
//                if(factoryBeanInstanceCache.get(autowiredBeanName) == null){
//                    continue;
//                }
//                //给类声明的属性赋值
//                //ioc.get(beanName)根据接口名beanName，拿到接口实现类型的实例也就是实现类
//                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName)
//                        .getWrapperInstance());
                //调用getBean()方法，解决循环依赖
                field.set(instance,getBean(autowiredBeanName));
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }

    }

    /*
     * 实例化方法
     * 创建真正的实例对象
     */
    private Object instantiateBean(String beanName, XCBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try{
            //保证单例模式，如果已经存在直接取出使用，如果没有再创建
            if(this.factoryBeanObjectCache.containsKey(beanName)){
                instance = this.factoryBeanObjectCache.get(beanName);
            }else{
                //根据类名获取类然后实例化
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                //======================AOP开始=========================
                //1.加载AOP配置文件
                XCAdvisedSupport config = instantionAopConfig(beanDefinition);
                //判断目标类，类名是否符合AOP配置规则
                config.setTargetClass(clazz);
                //设置对象本身
                config.setTarget(instance);

                //AOP判断规则，判断要不要生成代理类。 如果满足条件，覆盖原生对象，如果不满足 就不做任何处理返回原生对象
                if(config.pointCutMatch()){
                    //生成代理类，用代理类覆盖原生对象
                    instance = proxyFactory.createAopProxy(config).getProxy();
                }
                //======================AOP结束=========================

                //将真正实例化的对象保存到缓存中
                this.factoryBeanObjectCache.put(beanName,instance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return instance;
    }

    private XCAdvisedSupport instantionAopConfig(XCBeanDefinition beanDefinition) {
        XCAopConfig config = new XCAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new XCAdvisedSupport(config);

    }

    /*
     * 获取容器中已经实例化的Bean数量
     */
    public int getBeanDefinitionCount() {
        return regitry.beanDefinitionMap.size();
    }

    /*
     * 获取容器中已经实例化的BeanNames
     */
    public String[] getBeanDefinitionNames() {
        return regitry.beanDefinitionMap.keySet().toArray(new String[regitry.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }

}
