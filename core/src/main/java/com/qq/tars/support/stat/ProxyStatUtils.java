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

package com.qq.tars.support.stat;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class ProxyStatUtils {

    //public final static int MAX_MASTER_NAME_LEN = 127;
    private static final Object cacheLock = new Object();
    private static String cacheIP = null;

//    public static ProxyStatHead getHead(String masterName, String slaveName, String interfaceName, String masterIp,
//                                        String slaveIp, int slavePort, int returnValue, String slaveSetName, String slaveSetArea, String slaveSetID) {
//        if (mastersSetDivision == null || mastersSetDivision.length() == 0) {
//            masterName = masterName + "@" + ClientVersion.getVersion();
//        } else {
//            masterName = masterName + "." + mastersSetDivision + "@" + ClientVersion.getVersion();
//        }

//        String slaveSetName = "";
//        String slaveSetArea = "";
//        String slaveSetID = "";
//
//        if (slaveSetDivision != null && slaveSetDivision.length() > 0) {
//            String[] tmp = StringUtils.split(slaveSetDivision, ".");
//            if (tmp.length == 3) {
//                slaveSetName = tmp[0];
//                slaveSetArea = tmp[1];
//                slaveSetID = tmp[2];
//            }
//        }
//        return new ProxyStatHead(masterName, slaveName, interfaceName, masterIp, slaveIp, slavePort, returnValue, slaveSetName, slaveSetArea, slaveSetID, "");
//    }

//    private static String getShortModuleName(String moduleName) {
//        String shortModuleName = "";
//        if (moduleName != null) {
//            int pos = moduleName.indexOf('.');
//            if (pos >= 0) {
//                shortModuleName = moduleName.substring(pos + 1);
//            }
//        }
//        return shortModuleName;
//    }

//    private static String trimAndLimit(String str, int limit) {
//        String ret = "";
//        if (str != null) {
//            str = str.trim();
//            if (str.length() > limit) {
//                str = str.substring(0, limit);
//            }
//            ret = str;
//        }
//        return ret;
//    }

    public static String getLocalIP() {
        if (cacheIP == null) {
            synchronized (cacheLock) {
                if (cacheIP == null) {
                    try {
                        cacheIP = getLocalIPByNetworkInterface();
                    } catch (Exception e) {
                    }
                    if (cacheIP == null) {
                        try {
                            cacheIP = InetAddress.getLocalHost().getHostAddress();
                        } catch (UnknownHostException e) {
                            cacheIP = "0.0.0.0";
                        }
                    }
                }
            }
        }
        return cacheIP;
    }

    private static String getLocalIPByNetworkInterface() throws Exception {
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        while (enumeration.hasMoreElements()) {
            NetworkInterface networkInterface = enumeration.nextElement();
            if (networkInterface.isUp() && !networkInterface.isVirtual() && !networkInterface.isLoopback()) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        return null;
    }
}
