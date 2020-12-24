package com.qq.tars.client.rpc;

import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.net.core.IoBuffer;
import com.qq.tars.net.core.Request;
import com.qq.tars.net.core.Response;
import com.qq.tars.net.core.Session;
import com.qq.tars.net.protocol.ProtocolException;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.tars.support.TarsMethodParameterInfo;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import com.qq.tars.rpc.protocol.tup.UniAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;
import java.util.Map;

public class TarsEncoder extends MessageToByteEncoder {

    protected String charsetName = Constants.default_charset_name;
    public static int No = 0;
    public static String Name = "";
    public static String retStr = "";

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf out) throws Exception {
    }

    public IoBuffer encodeResponse(Response resp, Session session) throws ProtocolException {
        TarsServantResponse response = (TarsServantResponse) resp;
        if (response.getPacketType() == TarsHelper.ONEWAY) {
            return null;
        }

        TarsOutputStream jos = new TarsOutputStream();
        jos.setServerEncoding(charsetName);
        try {
            jos.getByteBuffer().writeInt(0);
            jos.write(response.getVersion(), 1);
            jos.write(response.getPacketType(), 2);

            if (response.getVersion() == TarsHelper.VERSION) {
                jos.write(response.getRequestId(), 3);
                jos.write(response.getMessageType(), 4);
                jos.write(response.getRet(), 5);
                jos.write(encodeResult(response, charsetName), 6);
                if (response.getStatus() != null) {
                    jos.write(response.getStatus(), 7);
                }
                if (response.getRet() != TarsHelper.SERVERSUCCESS) {
                    jos.write(StringUtils.isEmpty(response.getRemark()) ? "" : response.getRemark(), 8);
                }
            } else if (TarsHelper.VERSION2 == response.getVersion() || TarsHelper.VERSION3 == response.getVersion()) {
                jos.write(response.getMessageType(), 3);
                jos.write(response.getTicketNumber(), 4);
                String servantName = response.getRequest().getServantName();
                jos.write(servantName, 5);
                jos.write(response.getRequest().getFunctionName(), 6);
                jos.write(encodeWupResult(response, charsetName), 7);
                jos.write(response.getTimeout(), 8);
                if (response.getContext() != null) {
                    jos.write(response.getContext(), 9);
                }
                if (response.getStatus() != null) {
                    jos.write(response.getStatus(), 10);
                }
            } else {
                response.setRet(TarsHelper.SERVERENCODEERR);
                System.err.println("un supported protocol, ver=" + response.getVersion());
            }
        } catch (Exception ex) {
            if (response.getRet() == TarsHelper.SERVERSUCCESS) {
                response.setRet(TarsHelper.SERVERENCODEERR);
            }
        }
        //
        //ByteBuffer buffer = jos.getByteBuffer();
        ByteBuf buf = jos.getByteBuffer();
        //int datalen = buf.position();
        int datalen = buf.writerIndex();
        buf.writerIndex(0);
//        buffer.position(0);
//        buffer.putInt(datalen);
//        buffer.position(datalen);
        buf.writeInt(datalen);
        return IoBuffer.wrap(jos.getByteBuffer());
    }

    protected byte[] encodeResult(TarsServantResponse response, String charsetName) {
        TarsServantRequest request = response.getRequest();
        if (TarsHelper.isPing(request.getFunctionName())) {
            return new byte[]{};
        }

        TarsOutputStream ajos = new TarsOutputStream();
        ajos.setServerEncoding(charsetName);

        int ret = response.getRet();
        Map<String, TarsMethodInfo> methodInfoMap = AnalystManager.getInstance().getMethodMapByName(request.getServantName());
        if (ret == TarsHelper.SERVERSUCCESS && methodInfoMap != null) {
            TarsMethodInfo methodInfo = methodInfoMap.get(request.getFunctionName());
            TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();
            if (returnInfo != null && returnInfo.getType() != Void.TYPE && response.getResult() != null) {
                try {
                    ajos.write(response.getResult(), methodInfo.getReturnInfo().getOrder());
                } catch (Exception e) {
                    System.err.println("server encodec response result:" + response.getResult() + " with ex:" + e);
                }
            }

            Object value = null;
            List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
            for (TarsMethodParameterInfo parameterInfo : parametersList) {
                if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                    value = request.getMethodParameters()[parameterInfo.getOrder() - 1];
                    if (value != null) {
                        try {
                            ajos.write(TarsHelper.getHolderValue(value), parameterInfo.getOrder());
                        } catch (Exception e) {
                            System.err.println("server encodec response holder:" + value + " with ex:" + e);
                        }
                    }
                }
            }
        }
        return ajos.toByteArray();
    }

    protected byte[] encodeWupResult(TarsServantResponse response, String charsetName) {
        TarsServantRequest request = response.getRequest();
        UniAttribute unaOut = new UniAttribute();
        unaOut.setEncodeName(charsetName);
        if (response.getVersion() == TarsHelper.VERSION3) {
            unaOut.useVersion3();
        }

        int ret = response.getRet();
        Map<String, TarsMethodInfo> methodInfoMap = AnalystManager.getInstance().getMethodMapByName(request.getServantName());
        if (ret == TarsHelper.SERVERSUCCESS && methodInfoMap != null) {
            TarsMethodInfo methodInfo = methodInfoMap.get(request.getFunctionName());
            TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();
            if (returnInfo != null && returnInfo.getType() != Void.TYPE && response.getResult() != null) {
                unaOut.put(TarsHelper.STAMP_STRING, response.getResult());
            }

            Object value = null;
            List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
            for (TarsMethodParameterInfo parameterInfo : parametersList) {
                if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                    value = request.getMethodParameters()[parameterInfo.getOrder() - 1];
                    if (value != null) {
                        try {
                            String holderName = TarsHelper.getHolderName(parameterInfo.getAnnotations());
                            if (!StringUtils.isEmpty(holderName)) {
                                unaOut.put(holderName, TarsHelper.getHolderValue(value));
                            }
                        } catch (Exception e) {
                            System.err.println("server encodec response holder:" + value + " with ex:" + e);
                        }
                    }
                }
            }
        }
        return unaOut.encode();
    }

    public IoBuffer encodeRequest(Request req, Session session) throws ProtocolException {
        TarsServantRequest request = (TarsServantRequest) req;
        request.setCharsetName(charsetName);
        TarsOutputStream os = new TarsOutputStream();
        os.setServerEncoding(charsetName);

        //os.getByteBuffer().putInt(0);
        os.getByteBuffer().writeInt(0);
        os.write(request.getVersion(), 1);
        os.write(request.getPacketType(), 2);
        os.write(request.getMessageType(), 3);
        os.write(request.getTicketNumber(), 4);
        os.write(request.getServantName(), 5);
        os.write(request.getFunctionName(), 6);
        os.write(encodeRequestParams(request, charsetName), 7);
        os.write(request.getTimeout(), 8);
        os.write(request.getContext(), 9);
        os.write(request.getStatus(), 10);

        //os.getByteBuffer().flip();
        //这里我觉得不需要进行读写反转吧
        //os.getByteBuffer().clear();

        //int length = os.getByteBuffer().remaining();
        int length = os.getByteBuffer().writableBytes();

        //os.getByteBuffer().duplicate().putInt(0, length);
        os.getByteBuffer().duplicate().writeInt(length);

        if (length > TarsHelper.PACKAGE_MAX_LENGTH || length <= 0) {
            throw new ProtocolException("the length header of the package must be between 0~10M bytes. data length:" + Integer.toHexString(length));
        }
        return IoBuffer.wrap(os.getByteBuffer());
    }

    protected byte[] encodeRequestParams(TarsServantRequest request, String charsetName) throws ProtocolException {
        TarsOutputStream os = new TarsOutputStream(0);
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




