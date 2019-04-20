package com.spring.netty.promise.impl;

import com.spring.netty.promise.FailCallBack;
import com.spring.netty.promise.Promise;
import com.spring.netty.promise.SuccessCallBack;
import com.spring.netty.promise.ThenCallBack;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

// TODO：待续，完成异步调用
public class Deferred implements Promise {
    Queue<ThenCallBack> callBacks = new LinkedList<>();
    private SuccessCallBack successCallBack;
    private FailCallBack failCallBack;
    private String traceId;
    private String parentSpanId;

    public void resolve(Object result) {
        ThenCallBack thenCallBack = this.callBacks.poll();
        if(thenCallBack != null) {
            thenCallBack.done(result);
            return;
        }
        this.successCallBack.done(result);
    }

    public void reject(Exception e){
        this.failCallBack.done(e);
    }

    public Promise promise(){
        return this;
    }

    @Override
    public Promise then(ThenCallBack thenCallBack) {
        callBacks.offer(thenCallBack);
        return this;
    }

    @Override
    public Promise success(SuccessCallBack successCallBack) {
        this.successCallBack = successCallBack;
        return this;
    }

    @Override
    public Promise fail(FailCallBack failCallBack){
        this.failCallBack = failCallBack;
        return this;
    }
}
