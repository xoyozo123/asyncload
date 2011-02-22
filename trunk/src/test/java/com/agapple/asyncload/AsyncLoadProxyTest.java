package com.agapple.asyncload;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.agapple.asyncload.domain.AsyncLoadTestModel;
import com.agapple.asyncload.domain.AsyncLoadTestService;
import com.agapple.asyncload.impl.AsyncLoadEnhanceProxy;
import com.agapple.asyncload.impl.AsyncLoadProxyRepository;

public class AsyncLoadProxyTest extends BaseAsyncLoadTest {

    @Resource(name = "asyncLoadTestService")
    private AsyncLoadTestService asyncLoadTestService;

    @Before
    public void init() {
        // ���repository�ڵ�cache��¼
        try {
            TestUtils.setField(new AsyncLoadProxyRepository(), "reponsitory", new ConcurrentHashMap<String, Class>());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testProxy() {
        // System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "/home/ljh/cglib");
        // ��ʼ��config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // ��ʼ��executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // ��ʼ��proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        // ִ�в���
        AsyncLoadTestService service = proxy.getProxy();
        AsyncLoadTestModel model1 = service.getRemoteModel("first", 1000); // ÿ������sleep 1000ms
        AsyncLoadTestModel model2 = service.getRemoteModel("two", 1000); // ÿ������sleep 1000ms
        AsyncLoadTestModel model3 = service.getRemoteModel("three", 1000); // ÿ������sleep 1000ms

        long start = 0, end = 0;
        start = System.currentTimeMillis();
        System.out.println(model1.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) > 500l); // ��һ�λ�����, ��Ӧʱ�����1000ms����

        start = System.currentTimeMillis();
        System.out.println(model2.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // �ڶ��β�����������Ϊ��һ���Ѿ�������1000ms

        start = System.currentTimeMillis();
        System.out.println(model3.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // �����β�����������Ϊ��һ���Ѿ�������1000ms

        // ����executor
        executor.destory();
    }

    public void testProxy_timeout() {
        // ��ʼ��config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 100l); // ���ó�ʱʱ��Ϊ300ms
        // ��ʼ��executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // ��ʼ��proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        AsyncLoadTestModel model1 = service.getRemoteModel("first", 1000); // ÿ������sleep 1000ms
        AsyncLoadTestModel model2 = service.getRemoteModel("two", 200); // ÿ������sleep 1000ms

        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            System.out.println(model1.getDetail());
            Assert.fail(); // �����ߵ���һ��
        } catch (Exception e) { // TimeoutException�쳣
            System.out.println(e);
        }
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // �ᳬʱ

        start = System.currentTimeMillis();
        try {
            System.out.println(model2.getDetail());
        } catch (Exception e) {
            Assert.fail(); // �����ߵ���һ��
        }
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // ���ᳬʱ
    }

    public void testProxy_block_reject() {
        // ��ʼ��config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l); // ���ó�ʱʱ��Ϊ300ms
        // ��ʼ��executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(8, 2, AsyncLoadExecutor.HandleMode.REJECT); // ����Ϊ�ܾ�,8�������߳�,2���ȴ�����
        executor.initital();
        // ��ʼ��proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        ExecutorService executeService = Executors.newFixedThreadPool(10);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            for (int i = 0; i < 10; i++) { // ����10������
                final AsyncLoadTestModel model = service.getRemoteModel("first:" + i, 1000); // ÿ������sleep 1000ms
                executeService.submit(new Runnable() {

                    public void run() {
                        System.out.println(model.getDetail());
                    }
                });
            }
        } catch (RejectedExecutionException e) { // �������reject
            Assert.fail();
        }

        try {
            final AsyncLoadTestModel model = service.getRemoteModel("first:" + 11, 1000); // ������11�����񣬻����reject�쳣
            executeService.submit(new Runnable() {

                public void run() {
                    System.out.println(model.getDetail());
                }
            });

            Assert.fail();// �����ߵ���һ��
        } catch (RejectedExecutionException e) {
            System.out.println(e);// �����reject
        }

        try {
            Thread.sleep(2000l);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        executeService.shutdown();
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    public void testProxy_block_reject_noQueue() {
        // ��ʼ��config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l); // ���ó�ʱʱ��Ϊ3000ms
        // ��ʼ��executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(2, 0, AsyncLoadExecutor.HandleMode.REJECT); // ����Ϊ�ܾ�
        executor.initital();
        // ��ʼ��proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        ExecutorService executeService = Executors.newFixedThreadPool(10);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            for (int i = 0; i < 5; i++) { // ����5������
                final AsyncLoadTestModel model = service.getRemoteModel("first:" + i, 1000); // ÿ������sleep 1000ms
                executeService.submit(new Runnable() {

                    public void run() {
                        System.out.println(model.getDetail());
                    }
                });
            }

            Assert.fail(); // �����ߵ���һ��
        } catch (RejectedExecutionException e) { // �����reject
            System.out.println(e);// �����reject
        }

        try {
            Thread.sleep(2000l);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        executeService.shutdown();
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    public void testProxy_block_discard() {
        // ��ʼ��config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l); // ���ó�ʱʱ��Ϊ300ms
        // ��ʼ��executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(1, 0, AsyncLoadExecutor.HandleMode.BLOCK); // ����Ϊ����,10�������߳�,0���ȴ�����
        executor.initital();
        // ��ʼ��proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        ExecutorService executeService = Executors.newFixedThreadPool(10);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            for (int i = 0; i < 20; i++) { // ����20������
                final AsyncLoadTestModel model = service.getRemoteModel("first:" + i, 1000); // ÿ������sleep 1000ms
                executeService.submit(new Runnable() {

                    public void run() {
                        System.out.println(model.getDetail());
                    }
                });
            }

            Thread.sleep(4000l);
        } catch (RejectedExecutionException e) { // �������reject
            Assert.fail();
        } catch (InterruptedException e) {
            Assert.fail();
        }

        executeService.shutdown();
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
