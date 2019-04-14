package com.spring.netty.promise;

public interface Promise {
    Promise then(ThenCallBack thenCallBack);

    Promise success(SuccessCallBack successCallBack);
}
