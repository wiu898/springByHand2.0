package com.xc.spring.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 6:16 PM
 */
public class XCModelAndView {

    private String viewName;
    //传递给页面文件的map参数类型
    private Map<String,?> model;


    public XCModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public XCModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

}
