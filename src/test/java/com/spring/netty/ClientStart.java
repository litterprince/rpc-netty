package com.spring.netty;

import com.spring.common.service.HelloService;
import com.spring.netty.promise.Promise;
import com.spring.netty.promise.SuccessCallBack;
import com.spring.netty.promise.TestFunction;
import com.spring.netty.promise.ThenCallBack;
import com.spring.netty.promise.impl.Deferred;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring/spring-context.xml", "/spring/spring-client.xml"})
public class ClientStart {
    @Test
    public void start() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        final HelloService service = (HelloService) RPC.call(HelloService.class);
        int i = 3;
        while (i-- > 0) {
            int finalI = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(service.sayHello("jeff" + finalI));
                }
            });
            Thread.sleep(100);
        }
        System.in.read();
    }

    @Test
    public void asyncTest() throws IOException {
        // 异步
        Deferred promise = new Deferred();
        promise.success(new SuccessCallBack() {
            @Override
            public void done(Object result) {
                System.out.println(result);
            }
        });
        Object object = RPC.asyncCall(HelloService.class, promise);
        System.in.read();
    }

    @Test
    public void promiseTest() {
        Deferred deferred = new Deferred();
        TestFunction testFunction = (TestFunction) RPC.asyncCall(TestFunction.class, deferred);
        testFunction.remoteInteger().then(new ThenCallBack<Integer>() {
            @Override
            public Promise done(Integer data) {
                System.out.println(data);
                return testFunction.remoteString();
            }
        }).then((ThenCallBack<String>) data -> {
            System.out.println(data);
            return testFunction.remoteInteger();
        }).success(new SuccessCallBack<Integer>() {
            @Override
            public void done(Integer res) {
                System.out.println("done callback");
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("thread start");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                deferred.resolve(2);
                System.out.println("task finish 2 second");
            }
        }).start();
        System.out.println("异步完成");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
