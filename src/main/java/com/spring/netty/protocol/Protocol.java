package com.spring.netty.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.netty.message.Request;
import com.spring.netty.message.Response;

import java.io.IOException;

public class Protocol {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final int INT_LENGTH = 4;
    private static final int BYTE_LENGTH = 1;
    private Header header;
    private Body body;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    /**
     * request -> protocol
     */
    public void buildRequestProtocol(Request request) throws JsonProcessingException {
        int length = INT_LENGTH;
        // header
        header = new Header();
        header.setRequestId(request.getRequestId());
        int requestIdLength = request.getRequestId().getBytes().length;
        header.setRequestIdLength(requestIdLength);
        length += INT_LENGTH + requestIdLength;
        header.setTraceId(IdUtils.getTraceId());
        int traceIdLength = IdUtils.getTraceId().getBytes().length;
        header.setTraceIdLength(traceIdLength);
        length += INT_LENGTH + traceIdLength;
        header.setSpanId(IdUtils.getSpanId());
        int spanIdLength = IdUtils.getSpanId().getBytes().length;
        header.setSpanIdLength(spanIdLength);
        length += INT_LENGTH + spanIdLength;
        header.setType(Header.T_REQ);
        length += BYTE_LENGTH;

        // body
        body = new Body();
        body.setService(request.getClassName());
        int serviceLength = request.getClassName().getBytes().length;
        body.setServiceLength(serviceLength);
        length += INT_LENGTH + serviceLength;
        body.setMethod(request.getMethodName());
        int methodLength = request.getMethodName().getBytes().length;
        body.setMethodLength(methodLength);
        length += INT_LENGTH + methodLength;
        //Class<?>[] paraTypes = request.getParaTypes();
        Object[] parameters = request.getParameters();
        int argsNum = parameters == null ? 0 : parameters.length;
        body.setArgsNum(argsNum);
        length += INT_LENGTH;
        if(argsNum > 0) {
            Body.Arg[] args = new Body.Arg[argsNum];
            for (int i = 0; i < argsNum; i++) {
                Object value = parameters[i];
                args[i] = body.new Arg();
                args[i].setArgName(value.getClass().getName());
                int argNameLength = value.getClass().getName().getBytes().length;
                args[i].setArgNameLength(argNameLength);
                length += INT_LENGTH + argNameLength;
                byte[] content = objectMapper.writeValueAsBytes(value);
                args[i].setContent(content);
                args[i].setContentLength(content.length);
                length += INT_LENGTH + content.length;
            }
            body.setArgs(args);
        }

        header.setLength(length);
    }

    /**
     * response -> protocol
     */
    public void buildResponseProtocol(Response response) throws IOException {
        int length = header.getLength();

        // header
        /*header = new Header();*/
        header.setType(Header.T_RESP);

        // body
        /*body = new Body();*/
        byte[] result = objectMapper.writeValueAsBytes(response.getResult());
        body.setResult(result);
        body.setResultLength(result.length);
        length += INT_LENGTH + result.length;

        header.setLength(length);
    }

    /**
     * protocol -> request
     */
    public Request buildRequestByProtocol() throws IOException, ClassNotFoundException {
        //TODO 后续将request response命名改为适配器模式adapter
        Request request = new Request();
        request.setClassName(body.getService());
        request.setMethodName(body.getMethod());
        request.setRequestId(header.getRequestId());
        Integer argNum = body.getArgsNum();
        Object[] parameters = new Object[argNum];
        Body.Arg[] args = body.getArgs();
        for (int i = 0; i < argNum; i++) {
            Class clazz = Class.forName(args[i].getArgName());
            parameters[i] = objectMapper.readValue(args[i].getContent(), clazz);
        }
        request.setParameters(parameters);
        return request;
    }

    /**
     * protocol -> response
     */
    public Response buildResponseByProtocol() throws IOException {
        Response response = new Response();
        response.setRequestId(header.getRequestId());
        response.setResult(objectMapper.readValue(body.getResult(), Object.class));
        return response;
    }
}
