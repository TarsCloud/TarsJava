package com.qq.tars.rpc.protocol.tup;

import com.qq.tars.common.util.HexUtil;
import com.qq.tars.support.log.prx.LogInfo;
import org.junit.Test;

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
		logInfo.sFilename="filename";
		return logInfo;
	}

	/**
	 * 重现-解码内容为null.
	 * 用改动前的版本测试
	 */
	@Test
	public void reproduceDecodeProblem() {
		//version2的wup编码
		String version2HexByteStr = encodeVersion2Fix();
		System.out.println(version2HexByteStr);

		//对应目前version2的解码工作
		LogInfo logInfo = decodeVersionV2Now(version2HexByteStr);
		printLogInfo(logInfo);
	}

	/**
	 * 重现-编码失败的问题.
	 * 用改动前的版本测试
	 * 解码会报：com.huya.taf.protocol.taf.exc.TafDecodeException: type mismatch
	 */
	@Test
	public void reproduceEncodeProblem() {
		//用目前的version=2的方法去编码
		String version2HexByteStr = encodeVersion2Now();

		//然后用C++那边正常的version=2的方法去解码.
		LogInfo logInfo = decodeVersionV2Fix(version2HexByteStr);
		printLogInfo(logInfo);
	}

	/**
	 * fix 解码问题
	 */
	@Test
	public void fixDecodeProblem() {
		//version2的wup编码
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
		unaIn.put("tReq",logInfo);
		byte[] byteArray = unaIn.encode();
		String byteStr = HexUtil.bytes2HexStr(byteArray);
		return byteStr;
	}

	public void printLogInfo(LogInfo logInfo) {
		System.out.println("logInfo.appName:"+ logInfo.appname);
	}




	public String encodeVersion2Now() {
		LogInfo logInfo = getLogInfo();
		UniAttribute unaIn = new UniAttribute();
		unaIn.put("tReq",logInfo);
		byte[] byteArray = unaIn.encode();
		String byteStr = HexUtil.bytes2HexStr(byteArray);
		return byteStr;
	}

	private LogInfo decodeVersionV2Now(String hexBytes) {
		byte[] data = HexUtil.hexStr2Bytes(hexBytes);
		UniAttribute unaIn = new UniAttribute();
		unaIn.setEncodeName("UTF-8");
		unaIn.decodeVersion2(data);
		LogInfo logInfo = unaIn.getByClass("tReq", new LogInfo());
		return logInfo;
	}

	private LogInfo decodeVersionV2Fix(String hexBytes) {
		byte[] data = HexUtil.hexStr2Bytes(hexBytes);
		UniAttribute unaIn = new UniAttribute();
		unaIn.setNewDataNull();
		unaIn.setEncodeName("UTF-8");
		unaIn.decodeVersion2(data);
		LogInfo logInfo = unaIn.getByClass("tReq", new LogInfo());
		return logInfo;
	}
}
