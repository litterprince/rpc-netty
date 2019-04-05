package com.spring.netty.client;

import com.spring.netty.message.Request;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyObject {
    public static Object getProxyObject(Class cls){
        Object proxyObj = Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new InvocationHandler() {
            // TODO: 学习，使用原子包来记录次数同时保证了线程安全
            private AtomicInteger requestTimes = new AtomicInteger(0);

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Request request = new Request();
                request.setRequestId(buildRequestId(method.getName()));
                //返回表示声明由此 Method 对象表示的方法的类或接口的Class对象
                request.setClassName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setParameters(args);//输入的实参
                RPCClient.getInstance().getRequestLockMap().put(request.getRequestId(),request);
                RPCClient.getInstance().send(request);

                RPCClient.getInstance().getRequestLockMap().remove(request.getRequestId());
                return request.getResult();//目标方法的返回结果
            }

            // TODO: 学习，生成requestId的方式
            private String buildRequestId(String methodName) {
                StringBuilder sb = new StringBuilder();
                sb.append(requestTimes.incrementAndGet()).append("-");
                sb.append(System.currentTimeMillis()).append("-");
                sb.append(methodName).append("-");
                Random random = new Random();
                sb.append(random.nextInt(1000));
                return sb.toString();
            }
        });
        return proxyObj;
    }
}
