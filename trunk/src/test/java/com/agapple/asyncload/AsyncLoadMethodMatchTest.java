/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.agapple.asyncload.impl.AsyncLoadPerl5RegexpMethodMatcher;

/**
 * methodMatch匹配测试
 * 
 * @author jianghang 2011-1-29 下午05:06:25
 */
public class AsyncLoadMethodMatchTest extends BaseAsyncLoadTest {

    private static final String METHOD4 = "doOtherthing";
    private static final String METHOD3 = "doSomething";
    private static final String METHOD2 = "method2";
    private static final String METHOD1 = "method1";

    @Test
    public void testMatch_include() {
        AsyncLoadPerl5RegexpMethodMatcher matcher = new AsyncLoadPerl5RegexpMethodMatcher();
        matcher.setPatterns(new String[] { METHOD1, METHOD2 });

        Method method1 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD1);
        Assert.assertTrue(matcher.matches(method1));
        Method method2 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD2);
        Assert.assertTrue(matcher.matches(method2));
        Method method3 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD3);
        Assert.assertFalse(matcher.matches(method3));
        Method method4 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD4);
        Assert.assertFalse(matcher.matches(method4));
    }

    @Test
    public void testMatch_exclude() {
        AsyncLoadPerl5RegexpMethodMatcher matcher = new AsyncLoadPerl5RegexpMethodMatcher();
        matcher.setPatterns(new String[] { METHOD1, METHOD2 });
        matcher.setExcludedPatterns(new String[] { METHOD2, METHOD4 }); // 使用排除必须基于pattern基础上

        Method method1 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD1);
        Assert.assertTrue(matcher.matches(method1));
        Method method2 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD2);
        Assert.assertFalse(matcher.matches(method2));
        Method method3 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD3);
        Assert.assertFalse(matcher.matches(method3));
        Method method4 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD4);
        Assert.assertFalse(matcher.matches(method4));
    }

    @Test
    public void testMatch_includeOveride() {
        AsyncLoadPerl5RegexpMethodMatcher matcher = new AsyncLoadPerl5RegexpMethodMatcher();
        matcher.setExcludedPatterns(new String[] { METHOD3, METHOD4 });
        matcher.setExcludeOveride(true);

        Method method1 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD1);
        Assert.assertTrue(matcher.matches(method1));
        Method method2 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD2);
        Assert.assertTrue(matcher.matches(method2));
        Method method3 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD3);
        Assert.assertFalse(matcher.matches(method3));
        Method method4 = ReflectionUtils.findMethod(MethodMatchTest.class, METHOD4);
        Assert.assertFalse(matcher.matches(method4));
    }

}

class MethodMatchTest {

    public void method1() {

    }

    public void method2() {

    }

    public void doSomething() {

    }

    public void doOtherthing() {

    }
}
