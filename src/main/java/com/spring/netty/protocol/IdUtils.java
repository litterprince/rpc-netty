package com.spring.netty.protocol;

import java.util.concurrent.atomic.AtomicLong;

public class IdUtils {
    private static AtomicLong traceCount = new AtomicLong();
    private static AtomicLong spanCount = new AtomicLong();
    private static AtomicLong requestCount = new AtomicLong();

    /**
     * 时间戳转32进制+atomicLong转32进制
     * 无需考虑跨进程冲突 跨进程不在同一个channel内
     */
    public static String getTraceId() {
        // TODO：待续，是否开启trace
        /*if (RPC.isTrace()) {*/
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Long.toUnsignedString(System.currentTimeMillis(), 16));
        Integer appId = 0;//RPC.getTraceConfig().getAppId();
        stringBuilder.append(appId);
        stringBuilder.append(Long.toUnsignedString(traceCount.getAndIncrement(), 16));
        return stringBuilder.toString();
        /*}
        return "";*/
    }

    public static String getSpanId() {
        /*if (RPC.isTrace()) {*/
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Long.toUnsignedString(System.currentTimeMillis(), 16));
        Integer appId = 0;//RPC.getTraceConfig().getAppId();
        stringBuilder.append(appId);
        stringBuilder.append(Long.toUnsignedString(spanCount.getAndIncrement(), 16));
        return stringBuilder.toString();
        /*}
        return "";*/
    }

    public static String getRequestId() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Long.toUnsignedString(System.currentTimeMillis(), 16));
        stringBuilder.append(Long.toUnsignedString(requestCount.getAndIncrement(), 16));
        return stringBuilder.toString();
    }
}
