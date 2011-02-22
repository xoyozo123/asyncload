/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.template;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadConfig;
import com.agapple.asyncload.AsyncLoadExecutor;
import com.agapple.asyncload.impl.AsyncLoadResult;

/**
 * ����templateģʽ�ṩ��һ��AsyncLoad���ƣ����ʽ
 * 
 * @author jianghang 2011-1-24 ����07:01:07
 */
public class AsyncLoadTemplate {

    private AsyncLoadExecutor executor;
    private Long              defaultTimeout = AsyncLoadConfig.DEFAULT_TIME_OUT; // 3��

    /**
     * �첽ִ��callbackģ��,����Ĭ�ϵĳ�ʱʱ�䣬ͬʱ���ض�Ӧ��proxy model,ִ��AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @return
     */
    public <R> R execute(AsyncLoadCallback<R> callback) {
        return execute(callback, defaultTimeout);
    }

    /**
     * �첽ִ��callbackģ��,ͬʱ���ض�Ӧ��proxy model,ִ��AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @param timeout
     * @return
     */
    public <R> R execute(final AsyncLoadCallback<R> callback, long timeout) {
        Assert.notNull(callback, "");

        Future<R> future = executor.submit(new Callable<R>() {

            public R call() throws Exception {
                return callback.doAsyncLoad();
            }
        });

        Type type = callback.getClass().getGenericInterfaces()[0];
        if (!(type instanceof ParameterizedType)) {
            // �û���ָ��AsyncLoadCallBack�ķ�����Ϣ
            // TODO: ���Կ��ǣ������ָ�����ض���,Ĭ�ϲ���lazyLoad
            throw new RuntimeException(
                                       "you should specify AsyncLoadCallBack<R> for R type, ie: AsyncLoadCallBack<OfferModel>");
        }
        Class returnClass = (Class) getGenericClass((ParameterizedType) type, 0);
        // ����һ�����ص�AsyncLoadResult
        AsyncLoadResult result = new AsyncLoadResult(returnClass, future, timeout);
        // ��������һ���������
        return (R) result.getProxy();
    }

    /**
     * �첽ִ��callbackģ��,����Ĭ�ϵĳ�ʱʱ�䣬ͬʱ���ض�Ӧ��proxy model,ִ��AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @param returnClass �����ķ��ض���class
     * @return
     */
    public <R> R execute(AsyncLoadCallback<R> callback, Class<?> returnClass) {
        return execute(callback, returnClass, defaultTimeout);
    }

    /**
     * �첽ִ��callbackģ��,ͬʱ���ض�Ӧ��proxy model,ִ��AsyncLoad
     * 
     * @param <R>
     * @param callback
     * @param returnClass �����ķ��ض���class
     * @param timeout
     * @return
     */
    public <R> R execute(final AsyncLoadCallback<R> callback, Class<?> returnClass, long timeout) {
        Assert.notNull(callback, "");

        Future<R> future = executor.submit(new Callable<R>() {

            public R call() throws Exception {
                return callback.doAsyncLoad();
            }
        });
        // ����һ�����ص�AsyncLoadResult
        AsyncLoadResult result = new AsyncLoadResult(returnClass, future, timeout);
        // ��������һ���������
        return (R) result.getProxy();
    }

    /**
     * ȡ�÷�����Ϣ
     * 
     * @param cls
     * @param i
     * @return
     */
    private Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {
        Object genericClass = parameterizedType.getActualTypeArguments()[i];
        if (genericClass instanceof ParameterizedType) { // ����༶����
            return (Class<?>) ((ParameterizedType) genericClass).getRawType();
        } else if (genericClass instanceof GenericArrayType) { // �������鷺��
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
        } else {
            return (Class<?>) genericClass;
        }
    }

    // ===================== setter / getter =============================

    public void setExecutor(AsyncLoadExecutor executor) {
        this.executor = executor;
    }

    public void setDefaultTimeout(Long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

}
