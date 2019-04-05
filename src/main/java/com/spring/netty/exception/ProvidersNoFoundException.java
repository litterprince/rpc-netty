package com.spring.netty.exception;

public class ProvidersNoFoundException extends Exception {
    public ProvidersNoFoundException() {
        super("rpc could not found any available providers");
    }
}
