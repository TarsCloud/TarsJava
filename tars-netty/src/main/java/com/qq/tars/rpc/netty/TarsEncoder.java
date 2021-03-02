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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qq.tars.client.rpc.ProtocolException;
import com.qq.tars.client.rpc.Request;
import com.qq.tars.client.rpc.Response;
import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.JsonProvider;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.support.TarsMethodInfo;
import com.qq.tars.protocol.tars.support.TarsMethodParameterInfo;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.rpc.protocol.tars.support.AnalystManager;
import com.qq.tars.rpc.protocol.tup.UniAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class TarsEncoder extends MessageToByteEncoder<Object> {
    private static final Logger logger = LoggerFactory.getLogger(TarsEncoder.class);
    protected Charset charset = Constants.DEFAULT_CHARSET;

    public TarsEncoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf out) throws Exception {
        ByteBuf ioBuffer = null;
        try {
            if (o instanceof Request) {
                try {
                    ioBuffer = encodeRequest(channelHandlerContext.alloc(), (TarsServantRequest) o);
                    if (logger.isDebugEnabled())
                        logger.debug("[tars] write data size is  " + ioBuffer.duplicate().getInt(0));

                    int length = ioBuffer.getInt(0);
                    if (length > ioBuffer.readableBytes()) {
                        throw new IndexOutOfBoundsException();
                    }
                    out.ensureWritable(ioBuffer.readableBytes());
                    out.writeBytes(ioBuffer);
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            } else if (o instanceof Response) {
                ioBuffer = encodeResponse(channelHandlerContext.alloc(), (Response) o);
                if (logger.isDebugEnabled())
                    logger.debug("[tars] write data size is  " + ioBuffer.duplicate().getInt(0));
                int length = ioBuffer.getInt(0);
                if (length > ioBuffer.readableBytes()) {
                    throw new IndexOutOfBoundsException();
                }
                out.writeBytes(ioBuffer);
            } else {
                throw new ProtocolException("[tars] cannot support type");
            }
        } finally {
            if (ioBuffer != null) {
                ReferenceCountUtil.release(ioBuffer);
            }
        }
    }


    public ByteBuf encodeRequest(ByteBufAllocator allocator, Request req) throws ProtocolException {
        final TarsServantRequest request = (TarsServantRequest) req;
        CompositeByteBuf compositeByteBuf = allocator.compositeBuffer();
        ByteBuf headBuffer = allocator.buffer();
        ByteBuf bodyBuffer = allocator.buffer();
        ByteBuf endBuffer = allocator.buffer();
        int length = 0;
        try {
            request.setCharsetName(this.charset.name());
            TarsOutputStream os = new TarsOutputStream(headBuffer);
            os.setServerEncoding(this.charset);
            os.getByteBuffer().writeInt(0);
            os.write(request.getVersion(), 1);
            os.write(request.getPacketType(), 2);
            os.write(request.getMessageType(), 3);
            os.write(request.getRequestId(), 4);
            os.write(request.getServantName(), 5);
            os.write(request.getFunctionName(), 6);
            encodeRequestParams(bodyBuffer, request);
            os.writBodyeHead(bodyBuffer, 7);//
            TarsOutputStream endStream = new TarsOutputStream(endBuffer);
            endStream.write(request.getTimeout(), 8);
            endStream.write(request.getContext(), 9);
            endStream.write(request.getStatus(), 10);
            length = os.getByteBuffer().readableBytes() + bodyBuffer.readableBytes() + endStream.getByteBuffer().readableBytes();
            os.getByteBuffer().setInt(0, length);
            compositeByteBuf.addComponents(true, headBuffer, bodyBuffer, endBuffer);

        } catch (Exception e) {
            //when encode exception
            ReferenceCountUtil.release(headBuffer);
            ReferenceCountUtil.release(bodyBuffer);
            ReferenceCountUtil.release(endBuffer);
            if (length > TarsHelper.PACKAGE_MAX_LENGTH || length <= 0) {
                throw new ProtocolException("the length header of the package must be between 0~10M bytes. data length:" + Integer.toHexString(length));
            }
        }
        return compositeByteBuf;
    }

    protected void encodeRequestParams(ByteBuf byteBuf, TarsServantRequest request) throws ProtocolException {
        TarsOutputStream os = new TarsOutputStream(byteBuf);
        os.setServerEncoding(this.charset);
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
    }


    public ByteBuf encodeResponse(ByteBufAllocator allocator, Response resp) throws ProtocolException {
        TarsServantResponse response = (TarsServantResponse) resp;
        if (response.getPacketType() == TarsHelper.ONEWAY) {
            return null;
        }
        CompositeByteBuf compositeByteBuf = allocator.compositeBuffer();
        ByteBuf headBuffer = allocator.buffer();
        ByteBuf bodyBuffer = allocator.buffer();
        ByteBuf endBuffer = allocator.buffer();
        try {
            TarsOutputStream jos = new TarsOutputStream(headBuffer);
            jos.setServerEncoding(charset);
            try {
                jos.getByteBuffer().writeInt(0);
                jos.write(response.getVersion(), 1);
                jos.write(response.getPacketType(), 2);

                if (response.getVersion() == TarsHelper.VERSION) {
                    jos.write(response.getRequestId(), 3);
                    jos.write(response.getMessageType(), 4);
                    jos.write(response.getRet(), 5);
                    encodeResult(bodyBuffer, response);
                    jos.writBodyeHead(bodyBuffer, 6);
                    TarsOutputStream tarsOutputStream = new TarsOutputStream(endBuffer);
                    if (response.getStatus() != null) {
                        tarsOutputStream.write(response.getStatus(), 7);
                    }
                    if (response.getRet() != TarsHelper.SERVERSUCCESS) {
                        tarsOutputStream.write(StringUtils.isEmpty(response.getRemark()) ? "" : response.getRemark(), 8);
                    }
                } else if (TarsHelper.VERSION2 == response.getVersion() || TarsHelper.VERSION3 == response.getVersion()) {
                    jos.write(response.getMessageType(), 3);
                    jos.write(response.getRequestId(), 4);
                    String servantName = response.getRequest().getServantName();
                    jos.write(servantName, 5);
                    jos.write(response.getRequest().getFunctionName(), 6);
                    jos.write(encodeWupResult(response), 7);
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
                    jos.write(encodeJsonResult(response), 6);
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
            final int dataLength = headBuffer.readableBytes() + bodyBuffer.readableBytes() + endBuffer.readableBytes();
            headBuffer.setInt(0, dataLength);//write real data length
            compositeByteBuf.addComponents(true, headBuffer, bodyBuffer, endBuffer);
        } catch (Exception e) {
            //when encode exception
            ReferenceCountUtil.release(headBuffer);
            ReferenceCountUtil.release(bodyBuffer);
            ReferenceCountUtil.release(endBuffer);
            System.err.println("un supported protocol, ver=" + response.getVersion());
        }
        return compositeByteBuf;
    }

    protected void encodeResult(ByteBuf bytebuf, TarsServantResponse response) {
        TarsServantRequest request = (TarsServantRequest) response.getRequest();
        if (TarsHelper.isPing(request.getFunctionName())) {
            return;
        }
        TarsOutputStream ajos = new TarsOutputStream(bytebuf);
        ajos.setServerEncoding(charset);
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
    }

    protected byte[] encodeWupResult(TarsServantResponse response) {
        TarsServantRequest request = (TarsServantRequest) response.getRequest();
        UniAttribute unaOut = new UniAttribute();
        unaOut.setEncodeName(charset);
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

    protected byte[] encodeJsonResult(TarsServantResponse response) {
        TarsServantRequest request = (TarsServantRequest) response.getRequest();
        if (TarsHelper.isPing(request.getFunctionName())) {
            return new byte[]{};
        }
        JsonObject object = new JsonObject();
        int ret = response.getRet();
        Map<String, TarsMethodInfo> methodInfoMap = AnalystManager.getInstance().getMethodMapByName(request.getServantName());
        if (ret == TarsHelper.SERVERSUCCESS && methodInfoMap != null) {
            TarsMethodInfo methodInfo = methodInfoMap.get(request.getFunctionName());
            TarsMethodParameterInfo returnInfo = methodInfo.getReturnInfo();
            if (returnInfo != null && returnInfo.getType() != Void.TYPE && response.getResult() != null) {
                try {
                    JsonElement jsonElement = JsonProvider.toJsonTree(response.getResult());
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
                            JsonElement jsonElement = JsonProvider.toJsonTree(TarsHelper.getHolderValue(value));
                            object.add(parameterInfo.getName(), jsonElement);
                        } catch (Exception e) {
                            System.err.println("server encode json holder :" + value + ", with ex:" + e);
                        }
                    }
                }
            }
        }
        String result = object.toString();
        return result.getBytes(charset);
    }

}




