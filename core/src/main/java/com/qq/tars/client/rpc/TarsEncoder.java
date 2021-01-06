package com.qq.tars.client.rpc;

import com.qq.tars.common.util.Constants;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.tars.support.TarsMethodParameterInfo;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TarsEncoder extends MessageToByteEncoder<Object> {

    private static final Logger logger = LoggerFactory.getLogger(TarsEncoder.class);
    protected String charsetName = Constants.default_charset_name;
    public static int No = 0;
    public static String Name = "";
    public static String retStr = "";

    public TarsEncoder(String charset) {
        this.charsetName = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf out) throws Exception {
        if (o instanceof Request) {
            try {
                ByteBuf ioBuffer = encodeRequest((TarsServantRequest) o);
                System.out.println("length is  " + ioBuffer.duplicate().getInt(0));
                System.out.println("io buffer write size is " + ioBuffer.readableBytes());
                int length = ioBuffer.getInt(0);
                if (length > ioBuffer.readableBytes()) {
                    throw new IndexOutOfBoundsException();
                }
                out.writeBytes(ioBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public ByteBuf encodeRequest(Request req) throws ProtocolException {
        TarsServantRequest request = (TarsServantRequest) req;
        request.setCharsetName(charsetName);
        TarsOutputStream os = new TarsOutputStream();
        os.setServerEncoding(charsetName);
        os.getByteBuffer().writeInt(0);
        os.write(request.getVersion(), 1);
        os.write(request.getPacketType(), 2);
        os.write(request.getMessageType(), 3);
        os.write(request.getRequestId(), 4);
        os.write(request.getServantName(), 5);
        os.write(request.getFunctionName(), 6);
        os.write(encodeRequestParams(request, charsetName), 7);
        os.write(request.getTimeout(), 8);
        os.write(request.getContext(), 9);
        os.write(request.getStatus(), 10);
        int length = os.getByteBuffer().readableBytes();
        os.getByteBuffer().setInt(0, length);
        if (length > TarsHelper.PACKAGE_MAX_LENGTH || length <= 0) {
            throw new ProtocolException("the length header of the package must be between 0~10M bytes. data length:" + Integer.toHexString(length));
        }
        return os.getByteBuffer();
    }

    protected byte[] encodeRequestParams(TarsServantRequest request, String charsetName) throws ProtocolException {
        TarsOutputStream os = new TarsOutputStream();
        os.setServerEncoding(charsetName);

        TarsMethodInfo methodInfo = request.getMethodInfo();
        List<TarsMethodParameterInfo> parameterInfoList = methodInfo.getParametersList();

        Object value = null;
        Object[] parameter = request.getMethodParameters();
        for (TarsMethodParameterInfo parameterInfo : parameterInfoList) {
            if (TarsHelper.isContext(parameterInfo.getAnnotations()) || TarsHelper.isCallback(parameterInfo.getAnnotations())) {
                continue;
            }

            value = parameter[request.isAsync() ? parameterInfo.getOrder() : parameterInfo.getOrder() - 1];
            if (TarsHelper.isHolder(parameterInfo.getAnnotations()) && value != null) {
                try {
                    value = TarsHelper.getHolderValue(value);
                } catch (Exception e) {
                    throw new ProtocolException(e);
                }
                if (value != null) {
                    os.write(value, parameterInfo.getOrder());
                }
            } else if (value != null) {
                os.write(value, parameterInfo.getOrder());
            }
        }
        return os.toByteArray();
    }


}




