package com.spring.netty.promise;

public interface ThenCallBack<T> {
    Promise done(T result);
}
