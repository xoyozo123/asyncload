/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadConfig;
import com.agapple.asyncload.AsyncLoadExecutor;
import com.agapple.asyncload.AsyncLoadMethodMatch;
import com.agapple.asyncload.AsyncLoadProxy;

/**
 * ����cglib enhance proxy��ʵ��
 * 
 * @author jianghang 2011-1-21 ����10:56:39
 */
public class AsyncLoadEnhanceProxy<T> implements AsyncLoadProxy<T> {

    private T                 service;
    private AsyncLoadConfig   config;
    private AsyncLoadExecutor executor;

    public AsyncLoadEnhanceProxy(){
    }

    public AsyncLoadEnhanceProxy(T service, AsyncLoadExecutor executor){
        this(service, new AsyncLoadConfig(), executor);
    }

    public AsyncLoadEnhanceProxy(T service, AsyncLoadConfig config, AsyncLoadExecutor executor){
        this.service = service;
        this.config = config;
        this.executor = executor;
    }

    public T getProxy() {
        validate();
        return getProxyInternal();
    }

    /**
     * ��Ӧ�ļ�鷽��
     */
    private void validate() {
        Assert.notNull(service, "service should not be null");
        Assert.notNull(config, "config should not be null");
        Assert.notNull(executor, "executor should not be null");

        if (Modifier.isFinal(service.getClass().getModifiers())) { // Ŀǰ�ݲ�֧��final���͵Ĵ����Ժ���Կ���ʹ��jdk proxy
            throw new IllegalArgumentException("Enhance proxy not support final class :" + service.getClass());
        }
    }

    class AsyncLoadCallbackFilter implements CallbackFilter {

        public int accept(Method method) {
            // Ԥ�Ƚ���ƥ�䣬ֱ�Ӽ������Ҫ�����method�����⶯̬ƥ���˷�����
            Map<AsyncLoadMethodMatch, Long> matches = config.getMatches();
            Set<AsyncLoadMethodMatch> methodMatchs = matches.keySet();
            if (methodMatchs != null && !methodMatchs.isEmpty()) {
                for (Iterator<AsyncLoadMethodMatch> methodMatch = methodMatchs.iterator(); methodMatch.hasNext();) {
                    if (methodMatch.next().matches(method)) {
                        return 1;
                    }
                }
            }

            return 0;
        }
    }

    class AsyncLoadDirect implements Dispatcher {

        public Object loadObject() throws Exception {
            return service;
        }

    }

    class AsyncLoadInterceptor implements MethodInterceptor {

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Long timeout = getMatchTimeout(method);
            final Object finObj = service;
            final Object[] finArgs = args;
            final Method finMethod = method;

            Class returnClass = method.getReturnType();
            if (Void.TYPE.isAssignableFrom(returnClass)) {// �жϷ���ֵ�Ƿ�Ϊvoid
                // ������void�ĺ�������
                return finMethod.invoke(finObj, finArgs);
            } else if (Modifier.isFinal(returnClass.getModifiers())) {
                // ���������final���ͣ�Ŀǰ�ݲ�֧�֣������ɲ���jdk proxy
                return finMethod.invoke(finObj, finArgs);
            } else if (returnClass.isPrimitive() || returnClass.isArray()) {
                // �������������ͣ���Ϊ�޷�ʹ��cglib����
                return finMethod.invoke(finObj, finArgs);
            } else {
                Future future = executor.submit(new Callable() {

                    public Object call() throws Exception {
                        try {
                            return finMethod.invoke(finObj, finArgs);// ��Ҫֱ��ί�ж�Ӧ��finObj(service)���д���
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                // ����һ�����ص�AsyncLoadResult
                AsyncLoadResult result = new AsyncLoadResult(returnClass, future, timeout);
                // ��������һ���������
                return result.getProxy();
            }

        }

        /**
         * ���ض�Ӧ��ƥ���timeoutʱ�䣬һ�����ҵ���Ӧ��ƥ���
         * 
         * @param method
         * @return
         */
        private Long getMatchTimeout(Method method) {
            Map<AsyncLoadMethodMatch, Long> matches = config.getMatches();
            Set<Map.Entry<AsyncLoadMethodMatch, Long>> entrys = matches.entrySet();
            if (entrys != null && !entrys.isEmpty()) {
                for (Iterator<Map.Entry<AsyncLoadMethodMatch, Long>> iter = entrys.iterator(); iter.hasNext();) {
                    Map.Entry<AsyncLoadMethodMatch, Long> entry = iter.next();
                    if (entry.getKey().matches(method)) {
                        return entry.getValue();
                    }
                }
            }

            return config.getDefaultTimeout();
        }
    }

    // =========================== help mehotd =================================

    /**
     * ���ȴ�Repository���л�ȡProxyClass,������Ӧ��object
     * 
     * @return
     */
    private T getProxyInternal() {
        Class proxyClass = AsyncLoadProxyRepository.getProxy(service.getClass().getCanonicalName());
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(this.service.getClass());
            enhancer.setCallbackTypes(new Class[] { AsyncLoadDirect.class, AsyncLoadInterceptor.class });
            enhancer.setCallbackFilter(new AsyncLoadCallbackFilter());
            proxyClass = enhancer.createClass();
            // ע��proxyClass
            AsyncLoadProxyRepository.registerProxy(service.getClass().getCanonicalName(), proxyClass);
        }

        EnhancerHelper.setThreadCallbacks(proxyClass, new Callback[] { new AsyncLoadDirect(),
                new AsyncLoadInterceptor() });
        try {
            return (T) ReflectUtils.newInstance(proxyClass);
        } finally {
            // clear thread callbacks to allow them to be gc'd
            EnhancerHelper.setThreadCallbacks(proxyClass, null);
        }
    }

    // ====================== setter / getter ===========================

    public void setService(T service) {
        this.service = service;
    }

    public void setConfig(AsyncLoadConfig config) {
        this.config = config;
    }

    public void setExecutor(AsyncLoadExecutor executor) {
        this.executor = executor;
    }

}
