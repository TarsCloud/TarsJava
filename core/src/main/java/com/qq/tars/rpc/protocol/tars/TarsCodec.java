/**
 * Tencent is pleased to support the open source community by making Tars available.
 * <p>
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 * <p>
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * https://opensource.org/licenses/BSD-3-Clause
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.rpc.protocol.tars;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qq.tars.common.support.ClassLoaderManager;
import com.qq.tars.common.support.Holder;
import com.qq.tars.common.util.CollectionUtils;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.JSON;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.net.core.IoBuffer;
import com.qq.tars.net.core.Request;
import com.qq.tars.net.core.Response;
import com.qq.tars.net.core.Session;
import com.qq.tars.net.protocol.ProtocolException;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.tars.support.TarsMethodParameterInfo;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.protocol.util.TarsUtil;
import com.qq.tars.rpc.protocol.Codec;
import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.ServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import com.qq.tars.rpc.protocol.tup.UniAttribute;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TarsCodec extends Codec {

    public TarsCodec(String charsetName) {
        super(charsetName);
    }

    public IoBuffer encodeResponse(Response resp, Session session) throws ProtocolException {
        TarsServantResponse response = (TarsServantResponse) resp;
        if (response.getPacketType() == TarsHelper.ONEWAY) {
            return null;
        }

        TarsOutputStream jos = new TarsOutputStream();
        jos.setServerEncoding(charsetName);
        try {
            jos.getByteBuffer().putInt(0);
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
                jos.write(response.getRequestId(), 4);
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
            } else if (response.getVersion() == TarsHelper.VERSIONJSON) {
                jos.write(response.getRequestId(), 3);
                jos.write(response.getMessageType(), 4);
                jos.write(response.getRet(), 5);
                jos.write(encodeJsonResult(response, charsetName), 6);
                if (response.getStatus() != null) {
                    jos.write(response.getStatus(), 7);
                }
                if (response.getRet() != TarsHelper.SERVERSUCCESS) {
                    jos.write(StringUtils.isEmpty(response.getRemark()) ? "" : response.getRemark(), 8);
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
        ByteBuffer buffer = jos.getByteBuffer();
        int datalen = buffer.position();
        buffer.position(0);
        buffer.putInt(datalen);
        buffer.position(datalen);
        return IoBuffer.wrap(jos.toByteArray());
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

    protected byte[] encodeJsonResult(TarsServantResponse response, String charsetName) {
        TarsServantRequest request = response.getRequest();
        if (TarsHelper.isPing(request.getFunctionName())) {
            return new byte[]{};
        }

        // 服务端接口响应
        JsonObject object = new JsonObject();

        int ret = response.getRet();
        Map<String, TarsMethodInfo> methodInfoMap = AnalystManager.getInstance().getMethodMapByName(request.getServantName());
        if (ret == TarsHelper.SERVERSUCCESS && methodInfoMap != null) {
            TarsMethodInfo methodInfo = methodInfoMap.get(request.getFunctionName());
            TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();
            if (returnInfo != null && returnInfo.getType() != Void.TYPE && response.getResult() != null) {
                try {
                    JsonElement jsonElement = JSON.toJsonTree(response.getResult());
                    // System.out.println("requestId: " + request.getRequestId() + ", charset: " + request.getCharsetName() + ", ret: " + jsonElement.toString());
                    object.add("tars_ret", jsonElement);
                } catch (Exception e) {
                    System.err.println("server encode json ret :" + response.getResult() + ", with ex:" + e);
                }
            }

            Object value = null;
            List<TarsMethodParameterInfo> parametersList = methodInfo.getParametersList();
            for (TarsMethodParameterInfo parameterInfo : parametersList) {
                if (TarsHelper.isHolder(parameterInfo.getAnnotations())) {
                    value = request.getMethodParameters()[parameterInfo.getOrder() - 1];
                    if (value != null) {
                        try {
                            JsonElement jsonElement = JSON.toJsonTree(TarsHelper.getHolderValue(value));
                            // System.out.println("requestId: " + request.getRequestId() + ", charset: " + request.getCharsetName() + ", holder: " + jsonElement.toString());
                            object.add(parameterInfo.getName(), jsonElement);
                        } catch (Exception e) {
                            System.err.println("server encode json holder :" + value + ", with ex:" + e);
                        }
                    }
                }
            }
        }

        String result = object.toString();
        try {
            byte[] data = result.getBytes(charsetName);
            return data;
        } catch (UnsupportedEncodingException e) {
            System.err.println("server encode json encode :" + result + ", with charset:" + charsetName
                    + ", with ex: " + e);
            return new byte[]{};
        }
    }

    protected byte[] encodeWupResult(TarsServantResponse response, String charsetName) {
        TarsServantRequest request = response.getRequest();
        UniAttribute unaOut = new UniAttribute();
        unaOut.setEncodeName(charsetName);
        if (response.getVersion() == TarsHelper.VERSION3) {
            unaOut.useVersion3();
        } else if (response.getVersion() == TarsHelper.VERSION2) {
            unaOut.setNewDataNull();
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

        os.getByteBuffer().putInt(0);
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

        os.getByteBuffer().flip();

        int length = os.getByteBuffer().remaining();

        os.getByteBuffer().duplicate().putInt(0, length);
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

    public Request decodeRequest(IoBuffer buffer, Session session) throws ProtocolException {
        if (buffer.remaining() < 4) {
            return null;
        }
        int length = buffer.getInt() - TarsHelper.HEAD_SIZE;
        if (length > TarsHelper.PACKAGE_MAX_LENGTH || length <= 0) {
            throw new ProtocolException("the length header of the package must be between 0~10M bytes. data length:" + Integer.toHexString(length));
        }
        if (buffer.remaining() < length) {
            return null;
        }

        byte[] reads = new byte[length];
        buffer.get(reads);
        TarsInputStream jis = new TarsInputStream(reads);
        TarsServantRequest request = new TarsServantRequest(session);
        try {
            short version = jis.read(TarsHelper.STAMP_SHORT.shortValue(), 1, true);
            byte packetType = jis.read(TarsHelper.STAMP_BYTE.byteValue(), 2, true);
            int messageType = jis.read(TarsHelper.STAMP_INT.intValue(), 3, true);
            int requestId = jis.read(TarsHelper.STAMP_INT.intValue(), 4, true);
            String servantName = jis.readString(5, true);
            String methodName = jis.readString(6, true);
            byte[] data = jis.read(TarsHelper.STAMP_BYTE_ARRAY, 7, true);//数据
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
            request.setCharsetName(charsetName);
        } catch (Exception e) {
            System.err.println(e);
            request.setRet(TarsHelper.SERVERDECODEERR);
        }
        return request;
    }

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
                    parameters = decodeRequestBody(request.getData(), request.getCharsetName(), methodInfo);
                } else if (TarsHelper.VERSION2 == request.getVersion() || TarsHelper.VERSION3 == request.getVersion()) {
                    parameters = decodeRequestWupBody(request.getData(), request.getVersion(), request.getCharsetName(), methodInfo);
                } else if (TarsHelper.VERSIONJSON == request.getVersion()) {
                    // System.out.println("requestId: " + request.getRequestId() + ", charset: " + request.getCharsetName() + ", data: " + new String(data, request.getCharsetName()));
                    parameters = decodeRequestJsonBody(request.getData(), request.getCharsetName(), methodInfo);
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
                Object response = jis.read(parameterInfo.getStamp(), parameterInfo.getOrder(), false);
                if (response != null) {
                    value = new Holder<>(response);
                } else {
                    // new a response value
                    value = new Holder<>(TarsHelper.getNewParameterStamp(parameterInfo.getType()));
                }
            } else {
                value = jis.read(parameterInfo.getStamp(), parameterInfo.getOrder(), false);
            }
            parameters[i++] = value;
        }

        return parameters;
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
        JsonObject jsonObject = JSON.fromJson(new String(data, charset), JsonObject.class);

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
                    value = new Holder<>(JSON.fromJson(reqStr, parameterInfo.getType()));
                } else {
                    // System.out.println("holder has no " + parameterInfo.getName());
                    // new response, can not use cache
                    value = new Holder<>(TarsHelper.getNewParameterStamp(parameterInfo.getType()));
                }
            } else {
                if (jsonObject.has(parameterInfo.getName())) {
                    String reqStr = jsonObject.get(parameterInfo.getName()).toString();
                    // System.out.println("request has " + parameterInfo.getName() + ", str: " + reqStr);
                    value = JSON.fromJson(reqStr, parameterInfo.getType());
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

    public Response decodeResponse(IoBuffer buffer, Session session) throws ProtocolException {
        if (buffer.remaining() < TarsHelper.HEAD_SIZE) {
            return null;
        }

        int length = buffer.getInt() - TarsHelper.HEAD_SIZE;
        if (length > TarsHelper.PACKAGE_MAX_LENGTH || length <= 0) {
            throw new ProtocolException("the length header of the package must be between 0~10M bytes. data length:" + Integer.toHexString(length));
        }
        if (buffer.remaining() < length) {
            return null;
        }

        byte[] bytes = new byte[length];
        buffer.get(bytes);

        TarsServantResponse response = new TarsServantResponse(session);
        response.setCharsetName(charsetName);

        TarsInputStream is = new TarsInputStream(bytes);
        is.setServerEncoding(charsetName);

        response.setVersion(is.read((short) 0, 1, true));
        response.setPacketType(is.read((byte) 0, 2, true));
        response.setRequestId(is.read((int) 0, 3, true));
        response.setMessageType(is.read((int) 0, 4, true));
        response.setRet(is.read((int) 0, 5, true));
        if (response.getRet() == TarsHelper.SERVERSUCCESS) {
            response.setInputStream(is);
        } else {
            response.setRemark(is.read(TarsHelper.STAMP_STRING, 8, true));
        }

        return response;
    }

    public void decodeResponseBody(ServantResponse resp) throws ProtocolException {
        TarsServantResponse response = (TarsServantResponse) resp;

        TarsServantRequest request = response.getRequest();
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

        if (returnInfo != null && Void.TYPE != returnInfo.getType() && returnInfo.getType() != CompletableFuture.class) {
            values.add(jis.read(returnInfo.getStamp(), returnInfo.getOrder(), true));
            // dont decode  return Object when  function return use  CompletableFuture<void>
        } else if (returnInfo != null && returnInfo.getType() == CompletableFuture.class && returnInfo.getInnerType() != null && returnInfo.getInnerType() != Void.TYPE) {
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
        TarsServantRequest request = response.getRequest();

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

    public String getProtocol() {
        return Constants.TARS_PROTOCOL;
    }
}
