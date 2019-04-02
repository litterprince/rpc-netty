package com.spring.common.service;

import com.spring.common.annotation.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "Hi, " + name;
    }
}
