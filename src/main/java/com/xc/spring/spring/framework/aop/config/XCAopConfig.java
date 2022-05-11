package com.xc.spring.spring.framework.aop.config;

import lombok.Data;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 11:52 AM
 */
@Data
public class XCAopConfig {

    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;

}
