package com.spring.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class Decoder extends LineBasedFrameDecoder {
    public Decoder(int maxLength) {
        super(maxLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx, buffer);
        if (byteBuf == null) {
            return null;
        }

        Protocol protocol = new Protocol();
        Header header = new Header();
        Body body = new Body();
        // header
        header.setRequestId(byteBuf.readLong());
        header.setTraceId(buffer.readLong());
        header.setType(buffer.readByte());
        // body
        body.setService(new String(buffer.readBytes(buffer.readInt()).array()));
        body.setMethod(new String(buffer.readBytes(buffer.readInt()).array()));
        int argsNum = buffer.readInt();
        if(argsNum > 0){
            Body.Arg[] args = new Body.Arg[argsNum];
            for (int i = 0; i < argsNum; i++) {
                args[i].setArgName(new String(buffer.readBytes(buffer.readInt()).array()));
                args[i].setContent(buffer.readBytes(buffer.readInt()).array());
            }
            body.setArgs(args);
        }
        if (!header.getType().equals(Header.T_REQ)){
            body.setResultTag(byteBuf.readByte());
            body.setResult(byteBuf.readBytes(byteBuf.readInt()).array());
        }
        protocol.setHeader(header);
        protocol.setBody(body);
        return protocol;
    }
}
