package com.xc.spring.spring.framework.aop;

import com.sun.istack.internal.Nullable;

/**
 * description
 *
 * @author lichao chao.li07@hand-china.com 4/25/22 11:58 AM
 */
public interface XCAopProxy {

    Object getProxy();

    Object getProxy(@Nullable ClassLoader classLoader);

}
