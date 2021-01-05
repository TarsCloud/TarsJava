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

package com.qq.tars.common;

import jdk.nashorn.internal.ir.annotations.Immutable;

@Immutable
public final class ClientVersion {

//    public static final String major = "1";
//    public static final String minor = "6";
//    public static final String build = "1";

    public static final String CLIENT_VERSION = "2.0.0";

    public static String getVersion() {
        return CLIENT_VERSION;
    }
}
