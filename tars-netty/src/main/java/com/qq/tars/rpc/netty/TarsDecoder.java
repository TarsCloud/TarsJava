/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.qq.tars.rpc.netty;

import com.google.gson.JsonObject;
import com.qq.tars.client.rpc.ProtocolException;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.client.rpc.TicketFeature;
import com.qq.tars.common.support.ClassLoaderManager;
import com.qq.tars.common.support.Holder;
import com.qq.tars.common.util.CollectionUtils;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.JsonProvider;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.tars.support.TarsMethodParameterInfo;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.protocol.util.TarsUtil;
import com.qq.tars.rpc.protocol.Codec;
import com.qq.tars.rpc.protocol.ServantResponse;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import com.qq.tars.rpc.protocol.tup.UniAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TarsDecoder extends ByteToMessageDecoder implements Codec {
    private static final Logger logger = LoggerFactory.getLogger(TarsDecoder.class);
    private boolean isServer = false;

    public TarsDecoder(Charset charsetName, boolean isServer) {
        this.charsetName = charsetName;
        this.isServer = isServer;
    }

    public TarsDecoder(Charset charsetName) {
        this.charsetName = charsetName;
        this.isServer = false;
    }

    public Charset charsetName = Constants.DEFAULT_CHARSET;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        do {
            int saveReaderIndex = byteBuf.readerIndex();

            Object msg = null;
            if (isServer) {
                msg = decodeRequest(byteBuf);
            } else {

                msg = decodeResponse(byteBuf);
            }
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
            int messageType = jis.read(TarsHelper.STAMP_INT.intValue(), 3, true);
            int requestId = jis.read(TarsHelper.STAMP_INT.intValue(), 4, true);
            String servantName = jis.readString(5, true);
            String methodName = jis.readString(6, true);
            byte[] data = jis.read(TarsHelper.STAMP_BYTE_ARRAY, 7, true);
            int timeout = jis.read(TarsHelper.STAMP_INT.intValue(), 8, true);//超时时间
            Map<String, String> context = (Map<String, String>) jis.read(TarsHelper.STAMP_MAP, 9, true);//Map<String, String> context
            Map<String, String> status = (Map<String, String>) jis.read(TarsHelper.STAMP_MAP, 10, true);
            request.setVersion(version);
            request.setPacketType(packetType);
            request.setMessageType(messageType);
            request.setRequestId(requestId);
            request.setServantName(servantName);
            request.setFunctionName(methodName);
            request.setData(data);
            request.setTimeout(timeout);
            request.setContext(context);
            request.setStatus(status);
            request.setInputStream(jis);
            request.setCharsetName(charsetName.name());
            logger.debug("[Decoder][server] decode request is {}", request);
            decodeRequestBody(request);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
            request.setRet(TarsHelper.SERVERDECODEERR);
        }
        return request;
    }


    /***
     *
     * @param req
     */
    public void decodeRequestBody(Request req) {
        TarsServantRequest request = (TarsServantRequest) req;
        if (request.getRet() != TarsHelper.SERVERSUCCESS) {
            return;
        }
        if (TarsHelper.isPing(request.getFunctionName())) {
            return;
        }

        TarsInputStream jis = request.getInputStream();
        ClassLoader oldClassLoader = null;
        try {
            oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(resolveProtocolClassLoader());
            String methodName = request.getFunctionName();

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
            if (CollectionUtils.isNotEmpty(parametersList)) {
                Object[] parameters = new Object[parametersList.size()];
                int i = 0;
                if (TarsHelper.VERSION == request.getVersion()) {//request
                    parameters = decodeRequestBody(request.getData(), methodInfo);
                } else if (TarsHelper.VERSION2 == request.getVersion() || TarsHelper.VERSION3 == request.getVersion()) {
                    parameters = decodeRequestWupBody(request.getData(), request.getVersion(), request.getCharsetName(), methodInfo);
                } else if (TarsHelper.VERSIONJSON == request.getVersion()) {
                    parameters = decodeRequestJsonBody(request.getData(), request.getCharsetName(), methodInfo);
                } else {
                    request.setRet(TarsHelper.SERVERDECODEERR);
                    System.err.println("un supported protocol, ver=" + request.getVersion());
                }
                request.setMethodParameters(parameters);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (request.getRet() == TarsHelper.SERVERSUCCESS) {
                request.setRet(TarsHelper.SERVERDECODEERR);
            }
            System.err.println(TarsUtil.getHexdump(jis.toByteArray()));
        } finally {
            if (oldClassLoader != null) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
        return;
    }

    protected Object[] decodeRequestBody(byte[] data, TarsMethodInfo methodInfo) throws Exception {
        TarsInputStream jis = new TarsInputStream(data);
        List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
        Object[] parameters = new Object[parametersList.size()];
        int i = 0;
        jis.setServerEncoding(charsetName);//set decode charset name
        Object value = null;
        for (TarsMethodParameterInfo parameterInfo : parametersList) {
            if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                value = new Holder<>(jis.read(parameterInfo.getStamp(), parameterInfo.getOrder(), false));
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
        int length = channelBuffer.readInt() - 4;

        if (length > 10 * 1024 * 1024 || length <= 0) {
            throw new RuntimeException("the length header of the package must be between 0~10M bytes. data length:"
                    + Integer.toHexString(length));
        }
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
            return response;
        } else {
            throw new RuntimeException("server error!");
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
            results = decodeResponseBody(data, methodInfo);
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

    protected Object[] decodeResponseBody(byte[] data, TarsMethodInfo methodInfo) throws Exception {
        TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();
        List<Object> values = new ArrayList<Object>();
        TarsInputStream jis = new TarsInputStream(data);
        jis.setServerEncoding(charsetName);

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
        return this.charsetName;
    }

    protected Object[] decodeRequestWupBody(byte[] data, int version, String charset,
                                            TarsMethodInfo methodInfo) throws Exception {
        //wup request
        UniAttribute unaIn = new UniAttribute();
        unaIn.setEncodeName(charsetName);

        if (version == TarsHelper.VERSION2) {
            unaIn.decodeVersion2(data);
        } else if (version == TarsHelper.VERSION3) {
            unaIn.decodeVersion3(data);
        }

        List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
        Object[] parameters = new Object[parametersList.size()];

        int i = 0;
        Object value = null;
        for (TarsMethodParameterInfo parameterInfo : parametersList) {
            if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                String holderName = TarsHelper.getHolderName(parameterInfo.getAnnotations());
                if (unaIn.containsKey(holderName)) {
                    value = new Holder<>(unaIn.getByClass(holderName, parameterInfo.getStamp()));
                } else {
                    // new a response
                    value = new Holder<>(TarsHelper.getNewParameterStamp(parameterInfo.getType()));
                }
            } else {
                value = unaIn.getByClass(parameterInfo.getName(), parameterInfo.getStamp());
            }
            parameters[i++] = value;
        }

        return parameters;
    }

    protected Object[] decodeRequestJsonBody(byte[] data, String charset,
                                             TarsMethodInfo methodInfo) throws Exception {
        // 解析json串
        JsonObject jsonObject = JsonProvider.fromJson(new String(data, charset), JsonObject.class);

        // 按字段反序列化
        int i = 0;
        Object value = null;

        List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
        Object[] parameters = new Object[parametersList.size()];

        for (TarsMethodParameterInfo parameterInfo : parametersList) {
            if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                if (jsonObject.has(parameterInfo.getName())) {
                    String reqStr = jsonObject.get(parameterInfo.getName()).toString();
                    // System.out.println("holder has " + parameterInfo.getName() + ", str: " + reqStr);
                    value = new Holder<>(JsonProvider.fromJson(reqStr, parameterInfo.getStamp().getClass()));
                } else {
                    // System.out.println("holder has no " + parameterInfo.getName());
                    // new response, can not use cache
                    value = new Holder<>(TarsHelper.getNewParameterStamp(parameterInfo.getType()));
                }
            } else {
                if (jsonObject.has(parameterInfo.getName())) {
                    String reqStr = jsonObject.get(parameterInfo.getName()).toString();
                    // System.out.println("request has " + parameterInfo.getName() + ", str: " + reqStr);
                    value = JsonProvider.fromJson(reqStr, parameterInfo.getType());
                } else {
                    System.out.println("request has no " + parameterInfo.getName() + ", exception.");
                    throw new ProtocolException("no found parameter, the context[ROOT], "
                            + "serviceName[" + methodInfo.getServiceName()
                            + "], methodName[" + methodInfo.getMethodName()
                            + "], parameter[" + parameterInfo.getName() + "]");
                }
            }
            parameters[i++] = value;
        }

        return parameters;
    }

}
