package com.spring.netty.util;

import com.spring.netty.exception.ProvidersNoFoundException;

public interface LoadBalance {
    String chooseIP(String serviceName) throws ProvidersNoFoundException;
}
