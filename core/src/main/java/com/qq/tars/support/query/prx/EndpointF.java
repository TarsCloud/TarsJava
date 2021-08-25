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
// **********************************************************************
// This file was generated by a TARS parser!
// TARS version 1.0.1.
// **********************************************************************

package com.qq.tars.support.query.prx;

import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.annotation.TarsStruct;
import com.qq.tars.protocol.tars.annotation.TarsStructProperty;
import com.qq.tars.protocol.util.EncodingUtils;
import com.qq.tars.protocol.util.TarsUtil;

/**
 * Port information
 */
@TarsStruct
public class EndpointF implements Comparable<EndpointF> {

	@TarsStructProperty(order = 0, isRequire = true)
	public String host = "";
	@TarsStructProperty(order = 1, isRequire = true)
	public int port = 0;
	@TarsStructProperty(order = 2, isRequire = true)
	public int timeout = 0;
	@TarsStructProperty(order = 3, isRequire = true)
	public int istcp = 0;
	@TarsStructProperty(order = 4, isRequire = true)
	public int grid = 0;
	@TarsStructProperty(order = 5, isRequire = false)
	public int groupworkid = 0;
	@TarsStructProperty(order = 6, isRequire = false)
	public int grouprealid = 0;
	@TarsStructProperty(order = 7, isRequire = false)
	public String setId = "";
	@TarsStructProperty(order = 8, isRequire = false)
	public int qos = 0;
	@TarsStructProperty(order = 9, isRequire = false)
	public int bakFlag = 0;
	@TarsStructProperty(order = 11, isRequire = false)
	public int weight = 0;
	@TarsStructProperty(order = 12, isRequire = false)
	public int weightType = 0;
	@TarsStructProperty(order = 13, isRequire = false)
	public String subset = "";

	private static final int __PORT_ISSET_ID = 0;
	private static final int __TIMEOUT_ISSET_ID = 1;
	private static final int __ISTCP_ISSET_ID = 2;
	private static final int __GRID_ISSET_ID = 3;
	private static final int __GROUPWORKID_ISSET_ID = 4;
	private static final int __GROUPREALID_ISSET_ID = 5;
	private static final int __QOS_ISSET_ID = 6;
	private static final int __BAKFLAG_ISSET_ID = 7;
	private static final int __WEIGHT_ISSET_ID = 8;
	private static final int __WEIGHTTYPE_ISSET_ID = 9;
	private short __isset_bitfield = 0;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/** Returns true if field host is set (has been assigned a value) and false otherwise */
	public boolean isSetHost() {
		return this.host != null;
	}

	public void setHostIsSet(boolean value) {
		if (!value) {
			this.host = null;
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
		setPortIsSet(true);
	}

	/** Returns true if field port is set (has been assigned a value) and false otherwise */
	public boolean isSetPort() {
		return EncodingUtils.testBit(__isset_bitfield, __PORT_ISSET_ID);
	}

	public void setPortIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PORT_ISSET_ID, value);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
		setTimeoutIsSet(true);
	}

	/** Returns true if field timeout is set (has been assigned a value) and false otherwise */
	public boolean isSetTimeout() {
		return EncodingUtils.testBit(__isset_bitfield, __TIMEOUT_ISSET_ID);
	}

	public void setTimeoutIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIMEOUT_ISSET_ID, value);
	}

	public int getIstcp() {
		return istcp;
	}

	public void setIstcp(int istcp) {
		this.istcp = istcp;
		setIstcpIsSet(true);
	}

	/** Returns true if field istcp is set (has been assigned a value) and false otherwise */
	public boolean isSetIstcp() {
		return EncodingUtils.testBit(__isset_bitfield, __ISTCP_ISSET_ID);
	}

	public void setIstcpIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ISTCP_ISSET_ID, value);
	}

	public int getGrid() {
		return grid;
	}

	public void setGrid(int grid) {
		this.grid = grid;
		setGridIsSet(true);
	}

	/** Returns true if field grid is set (has been assigned a value) and false otherwise */
	public boolean isSetGrid() {
		return EncodingUtils.testBit(__isset_bitfield, __GRID_ISSET_ID);
	}

	public void setGridIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __GRID_ISSET_ID, value);
	}

	public int getGroupworkid() {
		return groupworkid;
	}

	public void setGroupworkid(int groupworkid) {
		this.groupworkid = groupworkid;
		setGroupworkidIsSet(true);
	}

	/** Returns true if field groupworkid is set (has been assigned a value) and false otherwise */
	public boolean isSetGroupworkid() {
		return EncodingUtils.testBit(__isset_bitfield, __GROUPWORKID_ISSET_ID);
	}

	public void setGroupworkidIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __GROUPWORKID_ISSET_ID, value);
	}

	public int getGrouprealid() {
		return grouprealid;
	}

	public void setGrouprealid(int grouprealid) {
		this.grouprealid = grouprealid;
		setGrouprealidIsSet(true);
	}

	/** Returns true if field grouprealid is set (has been assigned a value) and false otherwise */
	public boolean isSetGrouprealid() {
		return EncodingUtils.testBit(__isset_bitfield, __GROUPREALID_ISSET_ID);
	}

	public void setGrouprealidIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __GROUPREALID_ISSET_ID, value);
	}

	public String getSetId() {
		return setId;
	}

	public void setSetId(String setId) {
		this.setId = setId;
	}

	/** Returns true if field setId is set (has been assigned a value) and false otherwise */
	public boolean isSetSetId() {
		return this.setId != null;
	}

	public void setSetIdIsSet(boolean value) {
		if (!value) {
			this.setId = null;
		}
	}

	public int getQos() {
		return qos;
	}

	public void setQos(int qos) {
		this.qos = qos;
		setQosIsSet(true);
	}

	/** Returns true if field qos is set (has been assigned a value) and false otherwise */
	public boolean isSetQos() {
		return EncodingUtils.testBit(__isset_bitfield, __QOS_ISSET_ID);
	}

	public void setQosIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __QOS_ISSET_ID, value);
	}

	public int getBakFlag() {
		return bakFlag;
	}

	public void setBakFlag(int bakFlag) {
		this.bakFlag = bakFlag;
		setBakFlagIsSet(true);
	}

	/** Returns true if field bakFlag is set (has been assigned a value) and false otherwise */
	public boolean isSetBakFlag() {
		return EncodingUtils.testBit(__isset_bitfield, __BAKFLAG_ISSET_ID);
	}

	public void setBakFlagIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __BAKFLAG_ISSET_ID, value);
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
		setWeightIsSet(true);
	}

	/** Returns true if field weight is set (has been assigned a value) and false otherwise */
	public boolean isSetWeight() {
		return EncodingUtils.testBit(__isset_bitfield, __WEIGHT_ISSET_ID);
	}

	public void setWeightIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __WEIGHT_ISSET_ID, value);
	}

	public int getWeightType() {
		return weightType;
	}

	public void setWeightType(int weightType) {
		this.weightType = weightType;
		setWeightTypeIsSet(true);
	}

	/** Returns true if field weightType is set (has been assigned a value) and false otherwise */
	public boolean isSetWeightType() {
		return EncodingUtils.testBit(__isset_bitfield, __WEIGHTTYPE_ISSET_ID);
	}

	public void setWeightTypeIsSet(boolean value) {
		__isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __WEIGHTTYPE_ISSET_ID, value);
	}

	public String getSubset() {
		return subset;
	}

	public void setSubset(String subset) {
		this.subset = subset;
	}

	/** Returns true if field subset is set (has been assigned a value) and false otherwise */
	public boolean isSetSubset() {
		return this.subset != null;
	}

	public void setSubsetIsSet(boolean value) {
		if (!value) {
			this.subset = null;
		}
	}

	public EndpointF() {
	}

	public EndpointF(String host, int port, int timeout, int istcp, int grid, int groupworkid, int grouprealid, String setId, int qos, int bakFlag, int weight, int weightType, String subset) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		this.istcp = istcp;
		this.grid = grid;
		this.groupworkid = groupworkid;
		this.grouprealid = grouprealid;
		this.setId = setId;
		this.qos = qos;
		this.bakFlag = bakFlag;
		this.weight = weight;
		this.weightType = weightType;
		this.subset = subset;
	}

	@Override
	public int compareTo(EndpointF o) {
		int c = 0;
		if((c = TarsUtil.compareTo(host, o.host)) != 0 ) {
			return c;
		}
		if((c = TarsUtil.compareTo(port, o.port)) != 0 ) {
			return c;
		}
		if((c = TarsUtil.compareTo(timeout, o.timeout)) != 0 ) {
			return c;
		}
		if((c = TarsUtil.compareTo(istcp, o.istcp)) != 0 ) {
			return c;
		}
		if((c = TarsUtil.compareTo(grid, o.grid)) != 0 ) {
			return c;
		}
		if((c = TarsUtil.compareTo(qos, o.qos)) != 0 ) {
			return c;
		}
		if((c = TarsUtil.compareTo(weight, o.weight)) != 0 ) {
			return c;
		}
		if((c = TarsUtil.compareTo(weightType, o.weightType)) != 0 ) {
			return c;
		}
		return 0;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + TarsUtil.hashCode(host);
		result = prime * result + TarsUtil.hashCode(port);
		result = prime * result + TarsUtil.hashCode(timeout);
		result = prime * result + TarsUtil.hashCode(istcp);
		result = prime * result + TarsUtil.hashCode(grid);
		result = prime * result + TarsUtil.hashCode(qos);
		result = prime * result + TarsUtil.hashCode(weight);
		result = prime * result + TarsUtil.hashCode(weightType);
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
		if (!(obj instanceof EndpointF)) {
			return false;
		}
		EndpointF other = (EndpointF) obj;
		return (
			TarsUtil.equals(host, other.host) &&
			TarsUtil.equals(port, other.port) &&
			TarsUtil.equals(timeout, other.timeout) &&
			TarsUtil.equals(istcp, other.istcp) &&
			TarsUtil.equals(grid, other.grid) &&
			TarsUtil.equals(qos, other.qos) &&
			TarsUtil.equals(weight, other.weight) &&
			TarsUtil.equals(weightType, other.weightType)
		);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("EndpointF(");
		boolean first = true;

		sb.append("host:");
		if (this.host == null) {
			sb.append("null");
		} else {
			sb.append(this.host);
		}
		first = false;
		if (!first) sb.append(", ");
		sb.append("port:");
		sb.append(this.port);
		first = false;
		if (!first) sb.append(", ");
		sb.append("timeout:");
		sb.append(this.timeout);
		first = false;
		if (!first) sb.append(", ");
		sb.append("istcp:");
		sb.append(this.istcp);
		first = false;
		if (!first) sb.append(", ");
		sb.append("grid:");
		sb.append(this.grid);
		first = false;
		if (isSetGroupworkid()) {
			if (!first) sb.append(", ");
			sb.append("groupworkid:");
			sb.append(this.groupworkid);
			first = false;
		}
		if (isSetGrouprealid()) {
			if (!first) sb.append(", ");
			sb.append("grouprealid:");
			sb.append(this.grouprealid);
			first = false;
		}
		if (isSetSetId()) {
			if (!first) sb.append(", ");
			sb.append("setId:");
			if (this.setId == null) {
				sb.append("null");
			} else {
				sb.append(this.setId);
			}
			first = false;
		}
		if (isSetQos()) {
			if (!first) sb.append(", ");
			sb.append("qos:");
			sb.append(this.qos);
			first = false;
		}
		if (isSetBakFlag()) {
			if (!first) sb.append(", ");
			sb.append("bakFlag:");
			sb.append(this.bakFlag);
			first = false;
		}
		if (isSetWeight()) {
			if (!first) sb.append(", ");
			sb.append("weight:");
			sb.append(this.weight);
			first = false;
		}
		if (isSetWeightType()) {
			if (!first) sb.append(", ");
			sb.append("weightType:");
			sb.append(this.weightType);
			first = false;
		}
		if (isSetSubset()) {
			if (!first) sb.append(", ");
			sb.append("subset:");
			if (this.subset == null) {
				sb.append("null");
			} else {
				sb.append(this.subset);
			}
			first = false;
		}
		sb.append(")");
		return sb.toString();
	}

	public void writeTo(TarsOutputStream _os) {
		_os.write(host, 0);
		_os.write(port, 1);
		_os.write(timeout, 2);
		_os.write(istcp, 3);
		_os.write(grid, 4);
		_os.write(groupworkid, 5);
		_os.write(grouprealid, 6);
		if (null != setId) {
			_os.write(setId, 7);
		}
		_os.write(qos, 8);
		_os.write(bakFlag, 9);
		_os.write(weight, 11);
		_os.write(weightType, 12);
		if (null != subset) {
			_os.write(subset, 13);
		}
	}


	public void readFrom(TarsInputStream _is) {
		this.host = _is.readString(0, true);
		this.port = _is.read(port, 1, true);
		this.timeout = _is.read(timeout, 2, true);
		this.istcp = _is.read(istcp, 3, true);
		this.grid = _is.read(grid, 4, true);
		this.groupworkid = _is.read(groupworkid, 5, false);
		this.grouprealid = _is.read(grouprealid, 6, false);
		this.setId = _is.readString(7, false);
		this.qos = _is.read(qos, 8, false);
		this.bakFlag = _is.read(bakFlag, 9, false);
		this.weight = _is.read(weight, 11, false);
		this.weightType = _is.read(weightType, 12, false);
		this.subset = _is.readString(13, false);
	}

}