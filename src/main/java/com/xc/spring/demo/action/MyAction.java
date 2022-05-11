package com.xc.spring.demo.action;

import com.xc.spring.demo.service.IModifyService;
import com.xc.spring.demo.service.IQueryService;
import com.xc.spring.spring.framework.annotation.XCAutowired;
import com.xc.spring.spring.framework.annotation.XCController;
import com.xc.spring.spring.framework.annotation.XCRequestMapping;
import com.xc.spring.spring.framework.annotation.XCRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tom
 *
 */
@XCController
@XCRequestMapping("/web")
public class MyAction {

	@XCAutowired
	IQueryService queryService;
	@XCAutowired
	IModifyService modifyService;

	@XCRequestMapping("/query.json")
	public GPModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@XCRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}

	@XCRequestMapping("/add*.json")
	public GPModelAndView add(HttpServletRequest request, HttpServletResponse response,
							  @XCRequestParam("name") String name, @XCRequestParam("addr") String addr){
		try{
			String result = modifyService.add(name,addr);
			return out(response,result);
		}catch (Exception e){
			Map<String,String> model = new HashMap<String,String>();
			model.put("detail",e.getCause().getMessage());
			model.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return new GPModelAndView("500",model);
		}
	}
	
	@XCRequestMapping("/remove.json")
	public GPModelAndView remove(HttpServletRequest request, HttpServletResponse response,
								 @XCRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@XCRequestMapping("/edit.json")
	public GPModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@XCRequestParam("id") Integer id,
			@XCRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	

	private GPModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
