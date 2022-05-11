package com.xc.spring.demo.service.impl;

import com.gupao.spring.demo.service.IModifyService;
import com.gupao.spring.framework.annotation.GPService;

/**
 * 增删改业务
 * @author Tom
 *
 */
@GPService
public class ModifyService implements IModifyService {

	/**
	 * 增加
	 */
	@Override
	public String add(String name,String addr) throws Exception {
		throw new Exception("这是故意抛出来的异常");
		//return "modifyService add,name=" + name + ",addr=" + addr;
	}

	/**
	 * 修改
	 */
	@Override
	public String edit(Integer id,String name) {
		return "modifyService edit,id=" + id + ",name=" + name;
	}

	/**
	 * 删除
	 */
	@Override
	public String remove(Integer id) {
		return "modifyService id=" + id;
	}
	
}
