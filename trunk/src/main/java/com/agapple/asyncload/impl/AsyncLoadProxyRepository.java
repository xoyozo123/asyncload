/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * �ṩ��Ӧ��proxy�ֿ�,�����ظ�������Ӧ��class
 * 
 * @author jianghang 2011-1-24 ����03:36:17
 */
public class AsyncLoadProxyRepository {

    private static Map<String, Class> reponsitory = new ConcurrentHashMap<String, Class>(); // �ڷ������ü������sync����,���ﲻ��Ҫʹ��cocurrent��

    /**
     * ������ڶ�Ӧ��key��ProxyClass�ͷ��أ�û���򷵻�null
     * 
     * @param key
     * @return
     */
    public static Class getProxy(String key) {
        return reponsitory.get(key);
    }

    /**
     * ע���Ӧ��proxyClass���ֿ���
     * 
     * @param key
     * @param proxyClass
     */
    public static void registerProxy(String key, Class proxyClass) {
        if (!reponsitory.containsKey(key)) { // �����ظ��ύ
            reponsitory.put(key, proxyClass);
        }
    }
}
