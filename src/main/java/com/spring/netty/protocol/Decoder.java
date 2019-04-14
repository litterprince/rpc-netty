package com.spring.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;

// TODO：待续，完成自定义协议
public class Decoder extends LineBasedFrameDecoder {
    public Decoder(int maxLength) {
        super(maxLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        // 完成 byte数组 -> protocol 转变
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx, buffer);
        if (byteBuf == null) {
            return null;
        }

        Protocol protocol = new Protocol();
        Header header = new Header();
        Body body = new Body();
        // header
        header.setLength(byteBuf.readInt());
        header.setRequestId(new String(byteBuf.readBytes(byteBuf.readInt()).array()));
        header.setTraceId(new String(byteBuf.readBytes(byteBuf.readInt()).array()));
        header.setSpanId(new String(byteBuf.readBytes(byteBuf.readInt()).array()));
        header.setType(byteBuf.readByte());
        // body
        body.setService(new String(byteBuf.readBytes(byteBuf.readInt()).array()));
        body.setMethod(new String(byteBuf.readBytes(byteBuf.readInt()).array()));
        int argsNum = byteBuf.readInt();
        body.setArgsNum(argsNum);
        if(argsNum > 0){
            Body.Arg[] args = new Body.Arg[argsNum];
            for (int i = 0; i < argsNum; i++) {
                args[i] = body.new Arg();
                args[i].setArgName(new String(byteBuf.readBytes(byteBuf.readInt()).array()));
                args[i].setContent(byteBuf.readBytes(byteBuf.readInt()).array());
            }
            body.setArgs(args);
        }
        if (!header.getType().equals(Header.T_REQ)){
            body.setResult(byteBuf.readBytes(byteBuf.readInt()).array());
        }
        protocol.setHeader(header);
        protocol.setBody(body);
        return protocol;
    }
}
