package com.qq.tars.rpc.protocol.tars;

import com.qq.tars.rpc.protocol.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.IOException;
import java.nio.charset.Charset;

public class TarsServerCodec implements Codec {

    private String charsetName;

    public TarsServerCodec(String charsetName) {
        this.charsetName = charsetName;
    }

    @Override
    public void encode(Channel channel, ByteBuf channelBuffer, Object message) throws IOException {

    }

    @Override
    public Object decode(Channel channel, ByteBuf buffer) throws IOException {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public Charset getCharset() {
        return null;
    }
}
