package com.xc.spring.spring.framework.aop.support;

import com.xc.spring.spring.framework.aop.aspect.XCMethodBeforeAdviceInterceptor;
import com.xc.spring.spring.framework.aop.config.XCAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析AOP配置的工具类
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 11:50 AM
 */
public class XCAdvisedSupport {

    private XCAopConfig config;           //AOP配置类
    private Object target;                //目标对象
    private Class<?> targetClass;         //需要切入切面的目标类
    private Pattern pointCutClassPattern; //切面类正则

    private Map<Method, List<Object>> methodCache;


    public XCAdvisedSupport(XCAopConfig config) {
        this.config = config;
    }

    /*
     * 解析配置文件
     */
    private void parse(){
        //一定是离不开正则
        //把spring的Excpress变成一个Java能够识别的正则表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        //三段
        //第一段：方法的修饰符和返回值

        //第二段：类名

        //第三段：方法的名称和形参列表

        //生成匹配Class的正则-保存专门匹配Class的正则
        String pointCutForClassRegex
                = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile(pointCutForClassRegex
                .substring(pointCutForClassRegex.lastIndexOf(" ") +1));
        //享元的共享池 保存回调通知和目标切点方法之前的关系
        methodCache = new HashMap<Method, List<Object>>();
        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try{
            //切面类
            Class aspectClass = Class.forName(this.config.getAspectClass());
            Map<String, Method> aspectMethods = new HashMap<String, Method>();
            for(Method method : aspectClass.getMethods()){
                aspectMethods.put(method.getName(), method);
            }
            //以上都是初始化工作，准备阶段。从此处开始封装GPAdvice
            for(Method method : this.targetClass.getMethods()){
                //method.toString方法会拿到方法的所有名称信息，全路径名、方法类型、返回值、方法名、形参列表
                String methodString = method.toString();
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
                    List<Object> advices = new LinkedList<Object>();

                    if(!(null== config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        advices.add(new XCMethodBeforeAdviceInterceptor(aspectClass.newInstance(),
                                aspectMethods.get(config.getAspectBefore())));
                    }

                }


            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception{
        //从缓存中获取
        List<Object> cached = this.methodCache.get(method);
        //缓存未命中，则进行下一步处理
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            //获取所有的拦截器
            cached = methodCache.get(m);
            //存入缓存
            this.methodCache.put(m, cached);
        }
        return cached;
    }

    /*
     * 给ApplicationContext IOC中的对象初始化时调用，决定要不要生成代理类逻辑
     * 根据目标类名以及包名和aop配置文件中配置的需要切入的路径做匹配
     */
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    /*
     * 判断目标类，类名是否符合AOP配置规则å
     */
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Object getTarget() {
        return this.target;
    }
}
