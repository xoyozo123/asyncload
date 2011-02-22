/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.proxy.Callback;

/**
 * ������cglib enhancer�Ĳ��ִ���,Ϊ�˽���ܹ�cache��Ӧ��proxyClass����,�����ɸ��ݶ�Ӧ��class��̬��������
 * 
 * @author jianghang 2011-1-24 ����05:05:26
 */
public class EnhancerHelper {

    private static final String SET_THREAD_CALLBACKS_NAME = "CGLIB$SET_THREAD_CALLBACKS";

    public static void setThreadCallbacks(Class type, Callback[] callbacks) {
        setCallbacksHelper(type, callbacks, SET_THREAD_CALLBACKS_NAME);
    }

    public static void setCallbacksHelper(Class type, Callback[] callbacks, String methodName) {
        try {
            Method setter = getCallbacksSetter(type, methodName);
            setter.invoke(null, new Object[] { callbacks });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type + " is not an enhanced class");
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new CodeGenerationException(e);
        }
    }

    public static Method getCallbacksSetter(Class type, String methodName) throws NoSuchMethodException {
        return type.getDeclaredMethod(methodName, new Class[] { Callback[].class });
    }
}
