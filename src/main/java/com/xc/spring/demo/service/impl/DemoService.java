package com.xc.spring.demo.service.impl;

import com.gupao.spring.demo.service.IDemoService;
import com.gupao.spring.framework.annotation.GPService;

/**
 * 手写Spring 简单实现
 * 核心业务逻辑
 * @author lichao chao.li07@hand-china.com 2021-01-10 15:16
 */
@GPService
public class DemoService implements IDemoService {

    @Override
    public String get(String name) {
        return "My name is " + name + ",from service.";
    }

}
