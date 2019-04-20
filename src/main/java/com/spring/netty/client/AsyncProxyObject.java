package com.spring.netty.client;

import com.spring.netty.message.Request;
import com.spring.netty.promise.impl.Deferred;
import com.spring.netty.trace.NamedThreadFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncProxyObject implements InvocationHandler {
    private static AtomicLong requestTimes = new AtomicLong(0);

    private static ThreadPoolExecutor asyncSendExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 3,
            15, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new NamedThreadFactory("AsyncSend"),
            new ThreadPoolExecutor.DiscardPolicy());

    private Deferred promise;

    public AsyncProxyObject(Deferred promise) {
        this.promise = promise;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        //直接返回promise 其他操作全部异步
        asyncSendExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Request request = new Request();
                String requestId = buildRequestID(method.getName());
                request.setRequestId(requestId);
                request.setClassName(method.getDeclaringClass().getName());//返回表示声明由此 Method 对象表示的方法的类或接口的Class对象
                request.setMethodName(method.getName());
                //request.setParameterTypes(method.getParameterTypes());//返回形参类型
                request.setParameters(args);//输入的实参
                RPCClient.getInstance().getPromiseMap().put(request.getRequestId(), promise);
                try {
                    RPCClient.getInstance().asyncSend(request);
                } catch (Exception e) {
                    //暂时预留 目前尚未做其他异常封装处理
                    e.printStackTrace();
                }
            }
        });
        return promise;
    }

    private String buildRequestID(String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestTimes.incrementAndGet());
        sb.append(System.currentTimeMillis());
        sb.append(methodName);
        Random random = new Random();
        sb.append(random.nextInt(1000));
        return sb.toString();
    }
}
