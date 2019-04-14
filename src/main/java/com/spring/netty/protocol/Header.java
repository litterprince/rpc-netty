package com.spring.netty.protocol;

public class Header {
    final static Byte T_REQ = 1; // 发送请求
    final static Byte T_RESP = 2; // 发送响应
    public final static Byte T_EX_RESP = 3; // 错误响应

    private Integer length;

    /**
     * 每次RPC调用对应一个requestId
     */
    private String requestId;

    private Integer requestIdLength;

    /**
     * 预留用于链路追踪
     */
    private String traceId;

    private Integer traceIdLength;

    private String spanId;

    private Integer spanIdLength;

    /**
     * 调用类型 1:RPC请求 2:RPC响应 2:异常RPC响应
     */
    private Byte type;

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getTraceIdLength() {
        return traceIdLength;
    }

    public void setTraceIdLength(Integer traceIdLength) {
        this.traceIdLength = traceIdLength;
    }

    public Integer getRequestIdLength() {
        return requestIdLength;
    }

    public void setRequestIdLength(Integer requestIdLength) {
        this.requestIdLength = requestIdLength;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public Integer getSpanIdLength() {
        return spanIdLength;
    }

    public void setSpanIdLength(Integer spanIdLength) {
        this.spanIdLength = spanIdLength;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }
}
