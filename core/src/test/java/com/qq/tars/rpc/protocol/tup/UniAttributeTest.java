package com.qq.tars.rpc.protocol.tup;

import com.qq.tars.common.util.HexUtil;
import com.qq.tars.support.log.prx.LogInfo;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @description: add your desc
 * @author: walker
 * @create: 2020-02-18 19:38
 **/

public class UniAttributeTest {
    public LogInfo getLogInfo() {
        LogInfo logInfo = new LogInfo();
        logInfo.appname = "appname";
        logInfo.servername = "servName";
        logInfo.sFilename = "filename";
        return logInfo;
    }

    /**
     * reproduce the problem: decode data is null.
     * user the old version
     */
    @Test
    @Ignore
    public void reproduceDecodeProblem() {
        //tup version=2 encode
        String version2HexByteStr = encodeVersion2Fix();
        System.out.println(version2HexByteStr);

        //devocode tup version=2 now.
        LogInfo logInfo = decodeVersionV2Now(version2HexByteStr);
        printLogInfo(logInfo);
    }

    /**
     * reproduce the problem: encode tup version=2.
     * user the old version.
     * the tupProxy client will see：com.huya.taf.protocol.taf.exc.TafDecodeException: type mismatch
     */
    public void reproduceEncodeProblem() {
        //use tup version=2 encode now.
        String version2HexByteStr = encodeVersion2Now();

        //user the tupVersion=2 decode.
        LogInfo logInfo = decodeVersionV2Fix(version2HexByteStr);
        printLogInfo(logInfo);
    }

    /**
     * fix decode tup version=2 problem.
     */
    @Test
    public void fixDecodeProblem() {
        //version2 wup encode.
        String version2HexByteStr = encodeVersion2Fix();
        System.out.println(version2HexByteStr);

        LogInfo logInfo = decodeVersionV2Fix(version2HexByteStr);
        printLogInfo(logInfo);
    }

    @Test
    public void fixEncodeProblem() {
        String version2HexByteStr = encodeVersion2Fix();

        LogInfo logInfo = decodeVersionV2Fix(version2HexByteStr);
        printLogInfo(logInfo);
    }

    public String encodeVersion2Fix() {
        UniAttribute unaIn = new UniAttribute();
        unaIn.setNewDataNull();
        LogInfo logInfo = getLogInfo();
        unaIn.put("tReq", logInfo);
        byte[] byteArray = unaIn.encode();
        String byteStr = HexUtil.bytes2HexStr(byteArray);
        return byteStr;
    }

    public void printLogInfo(LogInfo logInfo) {
        System.out.println("logInfo.appName:" + logInfo.appname);
    }


    public String encodeVersion2Now() {
        LogInfo logInfo = getLogInfo();
        UniAttribute unaIn = new UniAttribute();
        unaIn.put("tReq", logInfo);
        byte[] byteArray = unaIn.encode();
        String byteStr = HexUtil.bytes2HexStr(byteArray);
        return byteStr;
    }

    private LogInfo decodeVersionV2Now(String hexBytes) {
        byte[] data = HexUtil.hexStr2Bytes(hexBytes);
        UniAttribute unaIn = new UniAttribute();
        unaIn.setEncodeName(StandardCharsets.UTF_8);
        unaIn.decodeVersion2(data);
        LogInfo logInfo = unaIn.getByClass("tReq", new LogInfo());
        return logInfo;
    }

    private LogInfo decodeVersionV2Fix(String hexBytes) {
        byte[] data = HexUtil.hexStr2Bytes(hexBytes);
        UniAttribute unaIn = new UniAttribute();
        unaIn.setNewDataNull();
        unaIn.setEncodeName(StandardCharsets.UTF_8);
        unaIn.decodeVersion2(data);
        LogInfo logInfo = unaIn.getByClass("tReq", new LogInfo());
        return logInfo;
    }
}
