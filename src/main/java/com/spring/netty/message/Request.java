package com.spring.netty.message;

public class Request {
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] paraTypes;
    private Object[] parameters;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParaTypes() {
        return paraTypes;
    }

    public void setParaTypes(Class<?>[] paraTypes) {
        this.paraTypes = paraTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object obj) {
        Request target = (Request) obj;
        if (!target.getRequestId().equals(this.getRequestId()))
            return false;
        return true;
    }
}
