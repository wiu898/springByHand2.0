package com.xc.spring.demo.action;


import com.xc.spring.demo.service.IQueryService;
import com.xc.spring.spring.framework.annotation.XCAutowired;
import com.xc.spring.spring.framework.annotation.XCController;
import com.xc.spring.spring.framework.annotation.XCRequestMapping;
import com.xc.spring.spring.framework.annotation.XCRequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@XCController
@XCRequestMapping("/")
public class PageAction {

    @XCAutowired
    IQueryService queryService;

    @XCRequestMapping("/first.html")
    public GPModelAndView query(@XCRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new GPModelAndView("first.html",model);
    }

}
