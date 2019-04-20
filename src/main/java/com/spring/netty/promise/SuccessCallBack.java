package com.spring.netty.promise;

public interface SuccessCallBack<T> {
    void done(T result);
}
