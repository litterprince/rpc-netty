package com.spring.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class Encoder extends MessageToMessageEncoder<Protocol> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Protocol protocol, List<Object> list) throws Exception {
        // 完成 protocol -> byte数组 转变
        ByteBuf byteBuf = Unpooled.buffer();
        // header
        Header header = protocol.getHeader();
        byteBuf.writeInt(header.getLength());// 4
        byteBuf.writeInt(header.getRequestIdLength()); // 4
        byteBuf.writeBytes(header.getRequestId().getBytes()); //length
        byteBuf.writeInt(header.getTraceIdLength()); // 4
        byteBuf.writeBytes(header.getTraceId().getBytes());// length
        byteBuf.writeInt(header.getSpanIdLength());// 4
        byteBuf.writeBytes(header.getSpanId().getBytes());// length
        byteBuf.writeByte(header.getType());// 1
        // body
        Body body = protocol.getBody();
        byteBuf.writeInt(body.getServiceLength());// 4
        byteBuf.writeBytes(body.getService().getBytes());// length
        byteBuf.writeInt(body.getMethodLength());// 4
        byteBuf.writeBytes(body.getMethod().getBytes());// length
        byteBuf.writeInt(body.getArgsNum());// 4
        // args
        if (body.getArgsNum() > 0) {
            for (Body.Arg arg : body.getArgs()) {
                byteBuf.writeInt(arg.getArgName().length());// 4
                byteBuf.writeBytes(arg.getArgName().getBytes());// length
                byteBuf.writeInt(arg.getContentLength());// 4
                byteBuf.writeBytes(arg.getContent());// length
            }
        }
        // 不是请求则加上响应信息
        if (!header.getType().equals(Header.T_REQ)) {
            byteBuf.writeInt(body.getResultLength());// 4
            byteBuf.writeBytes(body.getResult());// length
        }
        // 分包符
        byteBuf.writeBytes(System.getProperty("line.separator").getBytes());
        list.add(byteBuf);
    }
}
