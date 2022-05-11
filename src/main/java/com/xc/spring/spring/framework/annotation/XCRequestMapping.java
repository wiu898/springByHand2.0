package com.xc.spring.spring.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 手写Spring 简单实现 自动注解类
 *
 * @author lichao chao.li07@hand-china.com 2021-01-10 15:22
 */

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XCRequestMapping {
    String value() default "";
}
