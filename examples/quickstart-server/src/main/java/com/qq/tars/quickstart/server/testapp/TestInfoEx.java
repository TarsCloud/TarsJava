// **********************************************************************
// This file was generated by a TARS parser!
// TARS version 1.0.1.
// **********************************************************************

package com.qq.tars.quickstart.server.testapp;

import com.qq.tars.protocol.util.*;
import com.qq.tars.protocol.annotation.*;
import com.qq.tars.protocol.tars.*;
import com.qq.tars.protocol.tars.annotation.*;

@TarsStruct
public class TestInfoEx {

	@TarsStructProperty(order = 0, isRequire = false)
	public TestInfo bi = null;
	@TarsStructProperty(order = 1, isRequire = false)
	public java.util.List<TestInfo> vbi = null;
	@TarsStructProperty(order = 2, isRequire = false)
	public java.util.Map<String, TestInfo> mbi = null;

	public TestInfo getBi() {
		return bi;
	}

	public void setBi(TestInfo bi) {
		this.bi = bi;
	}

	public java.util.List<TestInfo> getVbi() {
		return vbi;
	}

	public void setVbi(java.util.List<TestInfo> vbi) {
		this.vbi = vbi;
	}

	public java.util.Map<String, TestInfo> getMbi() {
		return mbi;
	}

	public void setMbi(java.util.Map<String, TestInfo> mbi) {
		this.mbi = mbi;
	}

	public TestInfoEx() {
	}

	public TestInfoEx(TestInfo bi, java.util.List<TestInfo> vbi, java.util.Map<String, TestInfo> mbi) {
		this.bi = bi;
		this.vbi = vbi;
		this.mbi = mbi;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + TarsUtil.hashCode(bi);
		result = prime * result + TarsUtil.hashCode(vbi);
		result = prime * result + TarsUtil.hashCode(mbi);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TestInfoEx)) {
			return false;
		}
		TestInfoEx other = (TestInfoEx) obj;
		return (
			TarsUtil.equals(bi, other.bi) &&
			TarsUtil.equals(vbi, other.vbi) &&
			TarsUtil.equals(mbi, other.mbi) 
		);
	}

	public void writeTo(TarsOutputStream _os) {
		if (null != bi) {
			_os.write(bi, 0);
		}
		if (null != vbi) {
			_os.write(vbi, 1);
		}
		if (null != mbi) {
			_os.write(mbi, 2);
		}
	}

	static TestInfo cache_bi;
	static { 
		cache_bi = new TestInfo();
	}
	static java.util.List<TestInfo> cache_vbi;
	static { 
		cache_vbi = new java.util.ArrayList<TestInfo>();
		TestInfo var_7 = new TestInfo();
		cache_vbi.add(var_7);
	}
	static java.util.Map<String, TestInfo> cache_mbi;
	static { 
		cache_mbi = new java.util.HashMap<String, TestInfo>();
		String var_8 = "";
		TestInfo var_9 = new TestInfo();
		cache_mbi.put(var_8 ,var_9);
	}

	public void readFrom(TarsInputStream _is) {
		this.bi = (TestInfo) _is.read(cache_bi, 0, false);
		this.vbi = (java.util.List<TestInfo>) _is.read(cache_vbi, 1, false);
		this.mbi = (java.util.Map<String, TestInfo>) _is.read(cache_mbi, 2, false);
	}

}