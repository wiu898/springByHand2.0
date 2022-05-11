package com.xc.spring.spring.framework.webmvc.servlet;

import com.xc.spring.spring.framework.annotation.XCController;
import com.xc.spring.spring.framework.annotation.XCRequestMapping;
import com.xc.spring.spring.framework.context.XCApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 委派模式
 * 职责：负责任务调度，请求分发
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 9:51 AM
 */
public class XCDispatcherServlet extends HttpServlet {

    private XCApplicationContext applicationContext;

    private List<XCHandlerMapping> handlerMappings = new ArrayList<XCHandlerMapping>();

    private Map<XCHandlerMapping,XCHandlerAdapter> handlerAdapters
            = new HashMap<XCHandlerMapping, XCHandlerAdapter>();

    private List<XCViewResolver> viewResolvers = new ArrayList<XCViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //委派，根据URL找到一个对应的Method并通过response返回结果
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req,resp,new XCModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
                resp.getWriter().write("500 Exception,Detail:" + Arrays.toString(e.getStackTrace()));
            }
        }
    }



    @Override
    public void init(ServletConfig config) throws ServletException {

        //------ 初始化Spring的核心IOC容器 ------
        applicationContext = new XCApplicationContext(config.getInitParameter("contextConfigLocation"));
        //------ 完成了IoC、DI和MVC部分对接 ------

        //AOP
        
        //==============MVC部分==============
        //初始化MVC九大组件
        initStrategies(applicationContext);

        System.out.println("GP Spring framework is init.");

    }

    private void initStrategies(XCApplicationContext context){
//      //多文件上传的组件
//      initMultipartResolver(context);
//      //初始化本地语言环境
//      initLocaleResolver(context);
//      //初始化模板处理器
//      initThemeResolver(context);
        //初始化handlerMapping handlerMapping存放了每个controller方法对应的url信息
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//      //初始化异常拦截器
//      initHandlerExceptionResolvers(context);
//      //初始化视图预处理器
//      initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//      //FlashMap管理器
//      initFlashMapManager(context);
    }


    private void initHandlerMappings(XCApplicationContext context) {
        if(this.applicationContext.getBeanDefinitionCount() == 0){ return; }
        //获取IOC容器中所有Bean的key信息
        for(String beanName : this.applicationContext.getBeanDefinitionNames()){
            //通过beanName调用getBean方法拿到Spring封装好的BeanWrapper对象实例
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            //如果没有@GPController注解，不解析
            if(!clazz.isAnnotationPresent(XCController.class)){ continue; }

            //相当于提取Controller类头部配置的url,如果Controller类上配置了路径，需要取出最上层url用于拼接完整路径
            String baseUrl = "";
            if(clazz.isAnnotationPresent(XCRequestMapping.class)){
                XCRequestMapping requestMapping = clazz.getAnnotation(XCRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            //只获取public方法
            for(Method method : clazz.getMethods()){
                //没有@GPRequestMapping注解的方法不需要解析
                if(!method.isAnnotationPresent(XCRequestMapping.class)){continue;}
                XCRequestMapping requestMapping = method.getAnnotation(XCRequestMapping.class);
                //获取拼接了 类上配置的RequestMapping的全路径 将*替换成.* Sping正则替换成Java正则 用户动态匹配路径
                String regex = ("/" + baseUrl + "/" +
                        requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new XCHandlerMapping(pattern,instance,method));
                System.out.println("Mapped : " + regex + "," + method);
            }
        }

    }

    private void initHandlerAdapters(XCApplicationContext context) {
        for(XCHandlerMapping handlerMapping : handlerMappings){
            this.handlerAdapters.put(handlerMapping, new XCHandlerAdapter());
        }
    }

    private void initViewResolvers(XCApplicationContext context) {
        //通过配置文件 拿到页面存放的路径
        String templateRoot = context.getConfig().getProperty("templateRoot");
        //获取class下的页面文件路径信息
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        //获取目录下的所有文件,一个文件对应一个ViewResolver
        File templateRootDir = new File(templateRootPath);
        for(File file : templateRootDir.listFiles()){
            this.viewResolvers.add(new XCViewResolver(templateRoot));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //完成对HandlerMapping的封装
        //完成了对方法返回值的封装 ModelAndView
        //1.通过Url获得一个HandlerMapping
        XCHandlerMapping handler = getHandler(req);
        if(handler == null){
            processDispatchResult(req,resp,new XCModelAndView("404"));
            return;
        }
        //2.根据一个HandlerMapping获得一个HandlerAdapter
        XCHandlerAdapter ha = getHandlerAdapter(handler);
        //3.解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        XCModelAndView mv = ha.handler(req,resp,handler);
        //4.利用视图解析器ViewResolver将ModelAndView解析成对应的View返回给前台
        processDispatchResult(req,resp,mv);
    }

    private XCHandlerAdapter getHandlerAdapter(XCHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){
            return null;
        }
        return this.handlerAdapters.get(handler);
    }

    private XCHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+", "/");
        for (XCHandlerMapping mapping : handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if(!matcher.matches()){
                continue;
            }
            return mapping;
        }
        return null;
    }

    /*
     * 解析ModelAndView返回的结果
     */
    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, XCModelAndView mv) throws Exception {
        if(null == mv || this.viewResolvers.isEmpty()){
            return;
        }
        for(XCViewResolver viewResolver : this.viewResolvers){
            XCView view = viewResolver.resolveViewName(mv.getViewName());
            //直接向浏览器输出
            view.render(mv.getModel(), req, resp);
            return;
        }
    }

}
