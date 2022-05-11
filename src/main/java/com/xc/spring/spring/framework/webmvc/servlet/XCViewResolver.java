package com.xc.spring.spring.framework.webmvc.servlet;

import java.io.File;

/**
 * 将页面变成View对象
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 6:25 PM
 */
public class XCViewResolver {

    //默认页面后缀
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public XCViewResolver(String templateRoot) {
        //获取页面维护路径
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(templateRootPath);
    }

    public XCView resolveViewName(String viewName) {
        if(null == viewName || "".equals(viewName)){
            return null;
        }
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName)
                .replaceAll("/+","/"));
        return new XCView(templateFile);
    }
}
