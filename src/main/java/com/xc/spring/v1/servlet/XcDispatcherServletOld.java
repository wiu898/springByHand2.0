package com.xc.spring.v1.servlet;

import com.xc.spring.spring.framework.annotation.XCAutowired;
import com.xc.spring.spring.framework.annotation.XCController;
import com.xc.spring.spring.framework.annotation.XCRequestMapping;
import com.xc.spring.spring.framework.annotation.XCRequestParam;
import com.xc.spring.spring.framework.annotation.XCService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 300行代码简单手写spring Servlet
 *
 * @author lichao chao.li07@hand-china.com 4/21/22 6:52 PM
 */
public class XcDispatcherServletOld extends HttpServlet {

    private Properties contextConfig = new Properties();

    //享元模式，缓存-存储所有文件下的class类名
    private List<String> classNames = new ArrayList<String>();

    //IoC容器,Key默认是类名首字母小写 例如 aService  Value就是对应的实例对象
    private Map<String,Object> ioc = new HashMap<String,Object>();

    //HandlerMapping 维护URL和Method的对应关系
    private Map<String, Method> handlerMapping = new HashMap<String,Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception,Detail:" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        //判断url是否存在-对应Controller中初始化的路径
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        //获取对应方法进行调用
        Method method = handlerMapping.get(url);

        //1.先把形参的位置和参数名称建立映射关系，并且缓存下来
        Map<String, Integer> paramIndexMapping = new HashMap<String, Integer>();

        Annotation[][] pa = method.getParameterAnnotations();
        for(int i = 0; i< pa.length ; i++){
            for(Annotation a : pa[i]){
                if(a instanceof XCRequestParam){
                    String paramName = ((XCRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }

        //获取形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i< parameterTypes.length; i++){
            Class<?> paramterType = parameterTypes[i];
            if(paramterType == HttpServletRequest.class
                    || paramterType == HttpServletResponse.class){
                paramIndexMapping.put(paramterType.getName(),i);
            }
        }

        //2.根据参数位置匹配参数名字，从url中取到参数名字对应的值
        //动态实参列表
        Object[] paramValues = new Object[parameterTypes.length];
        Map<String,String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue())
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");
            if(!paramIndexMapping.containsKey(param.getKey())){
                continue;
            }
            int index = paramIndexMapping.get(param.getKey());
            //涉及到类型强转
            paramValues[index] = value;
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        //调用方法
        method.invoke(ioc.get(beanName),paramValues);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        // contextConfigLocation在web.xml中配置对应application.properties文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.扫描需要加载的类
        // scanPackage 对应application.properties中配置的具体包路径
        doScanner(contextConfig.getProperty("scanPackage"));

        //==============IoC部分==============
        //3.初始化IoC容器，将扫描到的相关类实例化，保存到IoC容器
        doInstance();

        //==============DI部分==============
        //4.完成依赖注入
        doAutowired();

        //==============MVC部分==============
        //5.初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("GP Spring framework is init.");

    }

    /*
     * 加载配置文件
     */
    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * 扫描相关类 ClassPath下符合包路径规则的所有类
     */
    private void doScanner(String scanPackage) {
        //获取包文件的路径,将名称中的.替换为/ 类型的目录结构
        URL url = this.getClass().getClassLoader()
                .getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        //获取到路径下的所有文件，当成是一个classPath文件夹
        for(File file : classPath.listFiles()){
            //递归调用,如果当前是文件夹 继续遍历子文件夹下文件,否则直接取出类名
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                //如果不是.class文件不进行处理
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                //拿到.class文件的文件名 - 此处防止重名文件，所以用包名加上类名
                String className = (scanPackage + "." + file.getName()).replace(".class","");
                classNames.add(className);
            }
        }
    }

    /*
     * 初始化IOC容器
     */
    private void doInstance() {
        //如果没有需要实例化的对象直接返回
        if(classNames.isEmpty()){
            return;
        }
        try{
            for(String className : classNames){
                Class<?> clazz = Class.forName(className);
                //有注解的才需要Spring去创建对象，加了注解写控制权反转，由Spring创建管理
                //此处假设Controller没有同名类，即不同包下没有同名Controller存在
                if(clazz.isAnnotationPresent(XCController.class)){
                    //获取IOC容器中的key(benName),因为Spring默认bean名称是类首字母小写，所以此处获取类名进行处理
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                } else if (clazz.isAnnotationPresent(XCService.class)){
                    //1.默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());

                    //2、在多个包下出现相同的类名，只能自己定义一个全局唯一的别名
                    //自定义命名
                    XCService service = clazz.getAnnotation(XCService.class);
                    if(!"".equals(service.value())){
                        beanName = service.value();
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    //3.如果是接口，只能初始化它的实现类。接口有多个实现类，抛出异常。
                    for(Class<?> i : clazz.getInterfaces()){
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The " + i.getName() + " is exists!!");
                        }
                        ioc.put(i.getName(),instance);
                    }
                }else{
                    continue;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /*
     * 依赖注入
     */
    private void doAutowired() {
        if(ioc.isEmpty()){
            return;
        }
        for(Map.Entry<String,Object> entry : ioc.entrySet()){
            //获取类下的所有属性 包括private/protected/default/public 修饰的字段
            //比如 @Autowired private IService service;
            for(Field field : entry.getValue().getClass().getDeclaredFields()){
                //如果没有声明@Autowired直接跳过不处理
                if(!field.isAnnotationPresent(XCAutowired.class)){ continue; }

                //获取@Autowired注解
                XCAutowired autowired = field.getAnnotation(XCAutowired.class);
                String beanName = autowired.value().trim();
                //如果用户没有自定义的beanName,就默认根据类型注入
                if("".equals(beanName)){
                    //获取字段类型 field.getType().getName(),得到接口全名
                    beanName = field.getType().getName();
                }
                //暴力访问
                field.setAccessible(true);
                //ioc.get(beanName)根据接口名beanName，拿到接口实现类型的实例也就是实现类
                //相当于通过接口的全名拿到接口实现的实例，然后赋值给field
                //这样 @Autowired private IService service;就完成了依赖注入 IService被实现类实例赋值
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * 初始化HandlerMapping
     * 完成URL和Method的对应关系
     */
    private void doInitHandlerMapping() {
        if(ioc.isEmpty()){ return; }
        for(Map.Entry<String,Object> entry : ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            //如果没有注解@GPController，不解析
            if(!clazz.isAnnotationPresent(XCController.class)){ continue; }

            //相当于提取class上配置的url,如果Controller类上配置了路径，需要取出最上层url用于拼接完整路径
            String baseUrl = "";
            if(clazz.isAnnotationPresent(XCRequestMapping.class)){
                XCRequestMapping requestMapping = clazz.getAnnotation(XCRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            /*
             * 只获取public方法
             * clazz.getMethods() 只获取public方法
             * clazz.getDeclaredMethods() 获取所有描述符修饰的方法
             */
            for(Method method : clazz.getMethods()){
                //没有@GPRequestMapping注解的方法不需要解析
                if(!method.isAnnotationPresent(XCRequestMapping.class)){continue;}
                XCRequestMapping requestMapping = method.getAnnotation(XCRequestMapping.class);

                //获取拼接了 类上配置的RequestMapping的全路径
                String url = ("/" + baseUrl + "/" + requestMapping.value())
                        .replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("Mapped : " + url + "," + method);
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
