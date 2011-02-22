/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

/**
 * �ṩ�����Ĳ��Է���
 * 
 * @author jianghang 2011-1-30 ����11:15:54
 */
public class TestUtils {

    /**
     * ��ȡ��Ӧ���Ե�ֵ
     * 
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getField(Object obj, String fieldName) {
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, obj);
    }

    /**
     * ���ö�Ӧ������ֵ
     * 
     * @param target
     * @param methodName
     * @param args
     * @return
     * @throws Exception
     */
    public static void setField(Object target, String fieldName, Object args) throws Exception {
        // ���Ҷ�Ӧ�ķ���
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, target, args);
    }

    /**
     * ���÷�����������һЩ˽�з���
     * 
     * @param target
     * @param methodName
     * @param args
     * @return
     * @throws Exception
     */
    public static Object invokeMethod(Object target, String methodName, Object... args) throws Exception {
        Method method = null;
        // ���Ҷ�Ӧ�ķ���
        if (args == null || args.length == 0) {
            method = ReflectionUtils.findMethod(target.getClass(), methodName);
        } else {
            Class[] argsClass = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argsClass[i] = args[i].getClass();
            }
            method = ReflectionUtils.findMethod(target.getClass(), methodName, argsClass);
        }
        ReflectionUtils.makeAccessible(method);

        if (args == null || args.length == 0) {
            return ReflectionUtils.invokeMethod(method, target);
        } else {
            return ReflectionUtils.invokeMethod(method, target, args);
        }
    }
}
