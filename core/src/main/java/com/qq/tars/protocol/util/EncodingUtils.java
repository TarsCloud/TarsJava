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
package com.qq.tars.protocol.util;

public class EncodingUtils {
    public EncodingUtils() {
    }

    public static final boolean testBit(byte v, int position) {
        return testBit((int)v, position);
    }

    public static final boolean testBit(short v, int position) {
        return testBit((int)v, position);
    }

    public static final boolean testBit(int v, int position) {
        return (v & 1 << position) != 0;
    }

    public static final boolean testBit(long v, int position) {
        return (v & 1L << position) != 0L;
    }

    public static final byte clearBit(byte v, int position) {
        return (byte)clearBit((int)v, position);
    }

    public static final short clearBit(short v, int position) {
        return (short)clearBit((int)v, position);
    }

    public static final int clearBit(int v, int position) {
        return v & ~(1 << position);
    }

    public static final long clearBit(long v, int position) {
        return v & ~(1L << position);
    }

    public static final byte setBit(byte v, int position, boolean value) {
        return (byte)setBit((int)v, position, value);
    }

    public static final short setBit(short v, int position, boolean value) {
        return (short)setBit((int)v, position, value);
    }

    public static final int setBit(int v, int position, boolean value) {
        return value ? v | 1 << position : clearBit(v, position);
    }

    public static final long setBit(long v, int position, boolean value) {
        return value ? v | 1L << position : clearBit(v, position);
    }
}
