package com.spring.netty.promise.impl;

import com.spring.netty.promise.Promise;
import com.spring.netty.promise.SuccessCallBack;
import com.spring.netty.promise.ThenCallBack;

import java.util.ArrayList;
import java.util.List;

// TODO：待续，完成异步调用
public class PromiseImpl implements Promise {
    List<ThenCallBack> callBacks = new ArrayList<>();
    private SuccessCallBack successCallBack;

    public void resolve(){
        /*for (ThenCallBack thenCallBack : callBacks){
            thenCallBack.done();
        }*/
        successCallBack.done();
    }

    @Override
    public Promise then(ThenCallBack thenCallBack) {
        callBacks.add(thenCallBack);
        return this;
    }

    @Override
    public Promise success(SuccessCallBack successCallBack) {
        this.successCallBack = successCallBack;
        return this;
    }
}
