/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadConfig;
import com.agapple.asyncload.AsyncLoadExecutor;
import com.agapple.asyncload.AsyncLoadProxy;
import com.agapple.asyncload.impl.AsyncLoadEnhanceProxy;

/**
 * ����spring FactoryBeanʵ�ֵ�һ��AsyncLoad���ƣ�����ʽ
 * 
 * @author jianghang 2011-1-24 ����07:00:17
 */
public class AsyncLoadFactoryBean implements FactoryBean, InitializingBean {

    private Object            target;
    private AsyncLoadExecutor executor;
    private AsyncLoadConfig   config;

    public Object getObject() throws Exception {
        AsyncLoadProxy proxy = new AsyncLoadEnhanceProxy(target, config, executor);
        return proxy.getProxy(); // ���ض�Ӧ�Ĵ������
    }

    public Class getObjectType() {
        return target.getClass();
    }

    public boolean isSingleton() {
        return true; // ��Ϊʹ��proxy����������Ϊtrue
    }

    public void afterPropertiesSet() throws Exception {
        // check
        Assert.notNull(config, "config should not be null!");
        Assert.notNull(executor, "executor should not be null!");
        Assert.notNull(target, "target should not be null!");
    }

    // ======================= setter / getter ======================

    public void setExecutor(AsyncLoadExecutor executor) {
        this.executor = executor;
    }

    public void setConfig(AsyncLoadConfig config) {
        this.config = config;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

}
