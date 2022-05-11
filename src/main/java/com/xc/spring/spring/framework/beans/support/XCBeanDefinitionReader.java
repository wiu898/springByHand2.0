package com.xc.spring.spring.framework.beans.support;

import com.xc.spring.spring.framework.beans.config.XCBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 负责服务解析配置文件的工具类
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 9:59 AM
 */
public class XCBeanDefinitionReader {

    private Properties contextConfig = new Properties();

    //保存扫描结果-类名
    private List<String> regitryBeanClasses = new ArrayList<String>();

    public XCBeanDefinitionReader(String... configLocations) {
        System.out.println("加载配置文件======="+configLocations);
        //加载配置文件- 此处默认只有一个配置文件，加载第一个
        doLoadConfig(configLocations[0]);
        //扫描配置文件中配置的相关包路径
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    /*
     * 加载配置文件
     */
    private void doLoadConfig(String contextConfigLocation) {
        //通过输入流读取配置文件 目录在class下
        InputStream is = this.getClass().getResourceAsStream("/"+
                (contextConfigLocation.replaceAll("classpath:","")));
        try {
            contextConfig.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(null != is){
                    is.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    /*
     * 扫描相关类
     */
    private void doScanner(String scanPackage) {
        //获取包文件的路径,将名称中的.替换为/ 类型的目录结构
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        //获取到路径下的所有文件，当成是一个classPath文件夹
        for (File file : classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage  + "." + file.getName());
            }else {
                //如果不是.class文件不进行处理
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                //拿到.class文件的文件名 - 此处防止重名文件，所以用包名加上类名的全称形式
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                regitryBeanClasses.add(className);
            }
        }

    }

    public List<XCBeanDefinition> loadBeanDefinitions() {
        //创建BeanDefinition结果集
        List<XCBeanDefinition> result = new ArrayList<XCBeanDefinition>();
        try{
            for (String className : regitryBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                //如果是接口不进行注入，因为接口无法实例化
                if(beanClass.isInterface()){
                    continue;
                }
                //保存类对应的className全类名
                //保存beanName
                //1.默认是类名首字母小写
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));

                //2.自定义benName
                //3.接口实现类注入
                for(Class<?> i : beanClass.getInterfaces()){
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private XCBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        XCBeanDefinition beanDefinition = new XCBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    public Properties getConfig(){
        return this.contextConfig;
    }

    /*
     * 首字母小写
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


}
