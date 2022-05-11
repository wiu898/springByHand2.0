package com.xc.spring.spring.framework.webmvc.servlet;

import com.sun.deploy.net.HttpResponse;
import com.sun.org.apache.regexp.internal.RE;
import com.xc.spring.spring.framework.annotation.XCRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * SpringMvc 视图解析器
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 6:22 PM
 */
public class XCHandlerAdapter {


    public XCModelAndView handler(HttpServletRequest req, HttpServletResponse resp, XCHandlerMapping handler) throws Exception{
        //保存形参列表-将参数名称和参数位置的关系保存起来
        Map<String, Integer> paramIndexMapping = new HashMap<String, Integer>();

        //通过运行时的状态去拿到注解的值 所以用 method.getParameterAnnotations()
        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for(int i = 0; i < pa.length; i++){
            for(Annotation a : pa[i]){
                if(a instanceof XCRequestParam){
                    String paramName = ((XCRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //初始化
        //获取形参列表 类型
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();
        for(int i = 0; i < paramTypes.length; i++){
            Class<?> paramType = paramTypes[i];
            if(paramType == HttpServletRequest.class || paramType == HttpServletResponse.class){
                paramIndexMapping.put(paramType.getName(),i);
            }
        }

        //拼接实参列表
        Map<String, String[]> params = req.getParameterMap();
        Object[] paramValues = new Object[paramTypes.length];

        for(Map.Entry<String, String[]> param : params.entrySet()){
            //目前多个参数暂时用,拼接
            String value = Arrays.toString(params.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");
            if(!paramIndexMapping.containsKey(param.getKey())){
                continue;
            }
            //获取形参列表中对应的位置，实参放到相同下标位置
            int index = paramIndexMapping.get(param.getKey());
            //允许自定义的类型转换器
            paramValues[index] = castStringValue(value,paramTypes[index]);
        }

        //request 和 response 可有可无单独处理
        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        //触发方法执行
        Object result  = handler.getMethod().invoke(handler.getController(), paramValues);
        if(result == null || result instanceof Void){
            return null;
        }
        boolean isModelAndView = handler.getMethod().getReturnType() == XCModelAndView.class;
        if(isModelAndView){
            return (XCModelAndView) result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> paramType) {
        if(String.class == paramType){
            return value;
        }else if(Integer.class == paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        }else{
            if(value != null){
                return value;
            }
        }
        return null;
    }

}
