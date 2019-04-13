package com.spring.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class Encoder extends MessageToMessageEncoder<Protocol> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Protocol protocol, List<Object> list) throws Exception {
        ByteBuf byteBuf = Unpooled.buffer();
        // header
        Header header = protocol.getHeader();
        byteBuf.writeLong(header.getRequestId());
        byteBuf.writeLong(header.getTraceId());
        byteBuf.writeByte(header.getType());
        // body
        Body body = protocol.getBody();
        byteBuf.writeInt(body.getServiceLength());
        byteBuf.writeBytes(body.getService().getBytes());
        byteBuf.writeInt(body.getMethodLength());
        byteBuf.writeBytes(body.getMethod().getBytes());
        byteBuf.writeInt(body.getArgsNum());
        // args
        if (body.getArgsNum() > 0) {
            for (Body.Arg arg : body.getArgs()) {
                byteBuf.writeInt(arg.getArgName().length());
                byteBuf.writeBytes(arg.getArgName().getBytes());
                byteBuf.writeInt(arg.getContentLength());
                byteBuf.writeBytes(arg.getContent());
            }
        }
        // 不是请求则加上响应信息
        if (!header.getType().equals(Header.T_REQ)) {
            byteBuf.writeByte(body.getResultTag());
            byteBuf.writeInt(body.getResultLength());
            byteBuf.writeBytes(body.getResult());
        }
        // 分包符
        byteBuf.writeBytes(System.getProperty("line.separator").getBytes());
    }
}
