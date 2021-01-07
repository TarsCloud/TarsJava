package com.qq.tars.client.rpc;

import com.qq.tars.common.support.ClassLoaderManager;
import com.qq.tars.common.support.Holder;
import com.qq.tars.common.util.CommonUtils;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.tars.support.TarsMethodParameterInfo;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.protocol.util.TarsUtil;
import com.qq.tars.rpc.protocol.Codec;
import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.ServantResponse;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import com.qq.tars.rpc.protocol.tup.UniAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TarsDecoder extends ByteToMessageDecoder implements Codec {
    public TarsDecoder(String charsetName) {
        this.charsetName = charsetName;
    }

    public String charsetName = Constants.default_charset_name;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        do {
            int saveReaderIndex = byteBuf.readerIndex();
            Object msg = decodeResponse(byteBuf);
            if (msg == null) {
                byteBuf.readerIndex(saveReaderIndex);
                break;
            } else {
                //is it possible to go here ?
                if (saveReaderIndex == byteBuf.readerIndex()) {
                    throw new IOException("Decode without read data.");
                }
                if (msg != null) {
                    list.add(msg);
                }
            }
        } while (byteBuf.isReadable());
    }

    public Request decodeRequest(ByteBuf buffer) throws ProtocolException {
        if (buffer.readableBytes() < TarsHelper.HEAD_SIZE) {
            return null;
        }
        int length = buffer.readInt() - TarsHelper.HEAD_SIZE;
        if (length > TarsHelper.PACKAGE_MAX_LENGTH || length <= 0) {
            throw new ProtocolException("the length header of the package must be between 0~10M bytes. data length:" + Integer.toHexString(length));
        }
        if (buffer.readableBytes() < length) {
            return null;
        }

        TarsInputStream jis = new TarsInputStream(buffer);
        TarsServantRequest request = new TarsServantRequest();
        try {
            short version = jis.read(TarsHelper.STAMP_SHORT.shortValue(), 1, true);
            byte packetType = jis.read(TarsHelper.STAMP_BYTE.byteValue(), 2, true);
            final int messageType = jis.read(TarsHelper.STAMP_INT.intValue(), 3, true);
            final int requestId = jis.read(TarsHelper.STAMP_INT.intValue(), 4, true);
            String servantName = jis.readString(5, true);
            String methodName = jis.readString(6, true);
            request.setVersion(version);
            request.setPacketType(packetType);
            request.setMessageType(messageType);
            request.setRequestId(requestId);
            request.setServantName(servantName);
            request.setFunctionName(methodName);
            request.setInputStream(jis);
            request.setCharsetName(charsetName);
        } catch (Exception e) {
            System.err.println(e);
            request.setRet(TarsHelper.SERVERDECODEERR);
        }
        return request;
    }


    /**
     * @param req
     **/
    public ServantRequest decodeRequestBody(ServantRequest req) {
        TarsServantRequest request = (TarsServantRequest) req;
        if (request.getRet() != TarsHelper.SERVERSUCCESS) {
            return request;
        }
        if (TarsHelper.isPing(request.getFunctionName())) {
            return request;
        }

        TarsInputStream jis = request.getInputStream();
        ClassLoader oldClassLoader = null;
        try {
            oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(resolveProtocolClassLoader());
            String methodName = request.getFunctionName();
            byte[] data = jis.read(TarsHelper.STAMP_BYTE_ARRAY, 7, true);//数据
            int timeout = jis.read(TarsHelper.STAMP_INT.intValue(), 8, true);//超时时间
            Map<String, Object> context = (Map<String, Object>) jis.read(TarsHelper.STAMP_MAP, 9, true);//Map<String, String> context
            Map<String, String> status = (Map<String, String>) jis.read(TarsHelper.STAMP_MAP, 10, true);

            request.setTimeout(timeout);
            request.setContext(context);
            request.setStatus(status);

            String servantName = request.getServantName();
            Map<String, TarsMethodInfo> methodInfoMap = AnalystManager.getInstance().getMethodMapByName(servantName);

            if (methodInfoMap == null || methodInfoMap.isEmpty()) {
                request.setRet(TarsHelper.SERVERNOSERVANTERR);
                throw new ProtocolException("no found methodInfo, the context[ROOT], serviceName[" + servantName + "], methodName[" + methodName + "]");
            }

            TarsMethodInfo methodInfo = methodInfoMap.get(methodName);
            if (methodInfo == null) {
                request.setRet(TarsHelper.SERVERNOFUNCERR);
                throw new ProtocolException("no found methodInfo, the context[ROOT], serviceName[" + servantName + "], methodName[" + methodName + "]");
            }

            request.setMethodInfo(methodInfo);
            List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
            if (!CommonUtils.isEmptyCollection(parametersList)) {
                Object[] parameters = new Object[parametersList.size()];
                int i = 0;
                if (TarsHelper.VERSION == request.getVersion()) {//request
                    parameters = decodeRequestBody(data, request.getCharsetName(), methodInfo);
                } else if (TarsHelper.VERSION2 == request.getVersion() || TarsHelper.VERSION3 == request.getVersion()) {
                    //wup request
                    UniAttribute unaIn = new UniAttribute();
                    unaIn.setEncodeName(request.getCharsetName());

                    if (request.getVersion() == TarsHelper.VERSION2) {
                        unaIn.decodeVersion2(data);
                    } else if (request.getVersion() == TarsHelper.VERSION3) {
                        unaIn.decodeVersion3(data);
                    }

                    Object value = null;
                    for (TarsMethodParameterInfo parameterInfo : parametersList) {
                        if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                            String holderName = TarsHelper.getHolderName(parameterInfo.getAnnotations());
                            if (!StringUtils.isEmpty(holderName)) {
                                value = new Holder<>(unaIn.getByClass(holderName, parameterInfo.getStamp()));
                            } else {
                                value = new Holder<>();
                            }
                        } else {
                            value = unaIn.getByClass(parameterInfo.getName(), parameterInfo.getStamp());
                        }
                        parameters[i++] = value;
                    }
                } else {
                    request.setRet(TarsHelper.SERVERDECODEERR);
                    System.err.println("un supported protocol, ver=" + request.getVersion());
                }
                request.setMethodParameters(parameters);
            }
        } catch (Throwable ex) {
            if (request.getRet() == TarsHelper.SERVERSUCCESS) {
                request.setRet(TarsHelper.SERVERDECODEERR);
            }
            System.err.println(TarsUtil.getHexdump(jis.getBs()));
        } finally {
            if (oldClassLoader != null) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
        return request;
    }

    protected Object[] decodeRequestBody(byte[] data, String charset, TarsMethodInfo methodInfo) throws Exception {
        TarsInputStream jis = new TarsInputStream(data);
        List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
        Object[] parameters = new Object[parametersList.size()];
        int i = 0;

        jis.setServerEncoding(charset);//set decode charset name
        Object value = null;
        for (TarsMethodParameterInfo parameterInfo : parametersList) {
            if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                value = new Holder<Object>(jis.read(parameterInfo.getStamp(), parameterInfo.getOrder(), false));
            } else {
                value = jis.read(parameterInfo.getStamp(), parameterInfo.getOrder(), false);
            }
            parameters[i++] = value;
        }
        return parameters;
    }

    public Response decodeResponse(ByteBuf channelBuffer) throws ProtocolException {

        if (channelBuffer.readableBytes() < 4) {
            return null;
        }
        // 减去长度占用的4字节
        int length = channelBuffer.readInt() - 4;

        if (length > 10 * 1024 * 1024 || length <= 0) {
            throw new RuntimeException("the length header of the package must be between 0~10M bytes. data length:"
                    + Integer.toHexString(length));
        }
        // 数据包还没有收全
        if (channelBuffer.readableBytes() < length) {
            return null;
        }

        byte[] bytes = new byte[length];
        channelBuffer.readBytes(bytes);

        TarsServantResponse response = new TarsServantResponse();
        response.setCharsetName(charsetName);
        TarsInputStream is = new TarsInputStream(bytes);
        is.setServerEncoding(charsetName);
        response.setVersion(is.read((short) 0, 1, true));
        response.setPacketType(is.read((byte) 0, 2, true));
        response.setRequestId(is.read(0, 3, true));
        response.setMessageType(is.read(0, 4, true));
        response.setRet(is.read(0, 5, true));
        if (response.getRet() == 0) {
            response.setInputStream(is);
            decodeResponseBody(response);
            System.out.println("result is " + response.getResult());
            return response;
        } else {
            throw new RuntimeException("server  error1");
        }
    }

    public void decodeResponseBody(ServantResponse resp) throws ProtocolException {
        TarsServantResponse response = (TarsServantResponse) resp;
        TarsServantRequest request = (TarsServantRequest) TicketFeature.getFeature(resp.getRequestId()).getRequest();
        if (request.isAsync()) {
            return;
        }
        TarsInputStream is = response.getInputStream();

        byte[] data = is.read(new byte[]{}, 6, true);
        TarsInputStream jis = new TarsInputStream(data);
        jis.setServerEncoding(response.getCharsetName());

        TarsMethodInfo methodInfo = request.getMethodInfo();
        TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();

        Object[] results;
        try {
            results = decodeResponseBody(data, response.getCharsetName(), methodInfo);
        } catch (Exception e) {
            throw new ProtocolException(e);
        }
        int i = 0;
        if (returnInfo != null && Void.TYPE != returnInfo.getType()) {
            response.setResult(results[i++]);
        }

        List<TarsMethodParameterInfo> list = methodInfo.getParametersList();
        for (TarsMethodParameterInfo info : list) {
            if (!TarsHelper.isHolder(info.getAnnotations())) {
                continue;
            }
            try {
                TarsHelper.setHolderValue(request.getMethodParameters()[info.getOrder() - 1], results[i++]);
            } catch (Exception e) {
                throw new ProtocolException(e);
            }
        }
        response.setStatus((HashMap<String, String>) is.read(TarsHelper.STAMP_MAP, 7, false));
    }

    protected Object[] decodeResponseBody(byte[] data, String charset, TarsMethodInfo methodInfo) throws Exception {
        TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();
        List<Object> values = new ArrayList<Object>();

        TarsInputStream jis = new TarsInputStream(data);
        jis.setServerEncoding(charset);

        if (returnInfo != null && Void.TYPE != returnInfo.getType()) {
            values.add(jis.read(returnInfo.getStamp(), returnInfo.getOrder(), true));
        }
        List<TarsMethodParameterInfo> list = methodInfo.getParametersList();
        for (TarsMethodParameterInfo info : list) {
            if (!TarsHelper.isHolder(info.getAnnotations())) {
                continue;
            }
            try {
                values.add(jis.read(info.getStamp(), info.getOrder(), true));
            } catch (Exception e) {
                throw new ProtocolException(e);
            }
        }
        return values.toArray();
    }

    public Object[] decodeCallbackArgs(TarsServantResponse response) throws ProtocolException {
        byte[] data = response.getInputStream().read(new byte[]{}, 6, true);

        TarsServantRequest request = (TarsServantRequest) response.getRequest();

        TarsMethodInfo methodInfo = null;
        Map<Method, TarsMethodInfo> map = AnalystManager.getInstance().getMethodMap(request.getApi());
        for (Iterator<Map.Entry<Method, TarsMethodInfo>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Method, TarsMethodInfo> entry = it.next();
            if (entry.getKey().getName().equals(request.getFunctionName())) {
                methodInfo = entry.getValue();
            }
        }

        try {
            return decodeCallbackArgs(data, response.getCharsetName(), methodInfo);
        } catch (Exception e) {
            throw new ProtocolException(e);
        }
    }

    protected Object[] decodeCallbackArgs(byte[] data, String charset, TarsMethodInfo methodInfo) throws ProtocolException, NoSuchMethodException, Exception {
        TarsInputStream jis = new TarsInputStream(data);
        jis.setServerEncoding(charset);

        List<Object> list = new ArrayList<Object>();
        TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();
        if (returnInfo != null && Void.TYPE != returnInfo.getType()) {
            list.add(jis.read(returnInfo.getStamp(), returnInfo.getOrder(), true));
        }

        List<TarsMethodParameterInfo> parameterInfoList = methodInfo.getParametersList();
        for (TarsMethodParameterInfo info : parameterInfoList) {
            if (TarsHelper.isContext(info.getAnnotations()) || TarsHelper.isCallback(info.getAnnotations())) {
                continue;
            }

            if (TarsHelper.isHolder(info.getAnnotations())) {
                list.add(jis.read(info.getStamp(), info.getOrder(), false));
            }
        }

        return list.toArray();
    }

    protected ClassLoader resolveProtocolClassLoader() {
        ClassLoader classLoader = ClassLoaderManager.getInstance().getClassLoader("");
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    @Override
    public void encode(Channel channel, ByteBuf channelBuffer, Object message) throws IOException {

    }

    @Override
    public Object decode(Channel channel, ByteBuf buffer) throws IOException {
        return null;
    }

    public String getProtocol() {
        return Constants.TARS_PROTOCOL;
    }

    @Override
    public Charset getCharset() {
        return null;
    }


}
