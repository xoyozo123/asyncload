package com.agapple.asyncload.impl;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

/**
 * �첽���ط��ص�proxy result
 * 
 * @author jianghang 2011-1-21 ����09:45:14
 */
public class AsyncLoadResult {

    private Class  returnClass;
    private Future future;
    private Long   timeout;

    public AsyncLoadResult(Class returnClass, Future future, Long timeout){
        this.returnClass = returnClass;
        this.future = future;
        this.timeout = timeout;
    }

    public Object getProxy() {
        Class proxyClass = AsyncLoadProxyRepository.getProxy(returnClass.getCanonicalName());
        if (proxyClass == null) { // ����cache����
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(returnClass);
            enhancer.setCallbackType(AsyncLoadFutureInterceptor.class);
            proxyClass = enhancer.createClass();

            AsyncLoadProxyRepository.registerProxy(returnClass.getCanonicalName(), proxyClass);
        }

        EnhancerHelper.setThreadCallbacks(proxyClass, new Callback[] { new AsyncLoadFutureInterceptor() });
        try {
            // ���ض���
            return ReflectUtils.newInstance(proxyClass);
        } finally {
            // clear thread callbacks to allow them to be gc'd
            EnhancerHelper.setThreadCallbacks(proxyClass, null);
        }

    }

    class AsyncLoadFutureInterceptor implements LazyLoader {

        public Object loadObject() throws Exception {
            // ʹ��cglib lazyLoader������ÿ�ε���future
            return future.get(timeout, TimeUnit.MILLISECONDS);
        }

    }

}
