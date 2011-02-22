/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload;

import java.lang.reflect.Method;

/**
 * �첽���ػ��� ����ƥ�������
 * 
 * @author jianghang 2011-1-21 ����09:49:29
 */
public interface AsyncLoadMethodMatch {

    AsyncLoadMethodMatch TRUE = new AsyncLoadTrueMethodMatcher(); // Ĭ���ṩ����always true��ʵ��

    boolean matches(Method method);

}

class AsyncLoadTrueMethodMatcher implements AsyncLoadMethodMatch {

    public boolean matches(Method method) {
        return true;
    }

    public String toString() {
        return "AsyncLoadTrueMethodMatcher.TURE";
    }
}
