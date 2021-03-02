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

package com.qq.tars.protocol.tars;

import com.qq.tars.common.util.Constants;
import com.qq.tars.common.util.HexUtil;
import com.qq.tars.protocol.tars.exc.TarsEncodeException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TarsOutputStream {
    protected Charset sServerEncoding = Constants.DEFAULT_CHARSET;

    public int setServerEncoding(Charset se) {
        this.sServerEncoding = se;
        return 0;
    }

    private ByteBuf bs;

    public TarsOutputStream(ByteBuf bs) {
        this.bs = bs;
    }

    public TarsOutputStream(int capacity) {
        bs = Unpooled.buffer(capacity);
    }

    public TarsOutputStream() {
        this(128);
    }

    public ByteBuf getByteBuffer() {
        return bs;
    }

    public byte[] toByteArray() {
        //byte[] newBytes = new byte[bs.position()];
        byte[] newBytes = new byte[bs.readableBytes()];
        bs.getBytes(0, newBytes);
        return newBytes;
    }


    public void writeHead(byte type, int tag) {
        if (tag < 15) {
            byte b = (byte) ((tag << 4) | type);
            //bs.put(b);
            bs.writeByte(b);
        } else if (tag < 256) {
            byte b = (byte) ((15 << 4) | type);
            //bs.put(b);
            bs.writeByte(b);
            //bs.put((byte) tag);
            bs.writeByte((byte) tag);
        } else {
            throw new TarsEncodeException("tag is too large: " + tag);
        }
    }

    public void write(boolean b, int tag) {
        byte by = (byte) (b ? 0x01 : 0);
        write(by, tag);
    }

    public void write(byte b, int tag) {
        if (b == 0) {
            writeHead(TarsStructBase.ZERO_TAG, tag);
        } else {
            writeHead(TarsStructBase.BYTE, tag);
            //bs.put(b);
            bs.writeByte(b);
        }
    }

    public void write(short n, int tag) {
        if (n >= Byte.MIN_VALUE && n <= Byte.MAX_VALUE) {
            write((byte) n, tag);
        } else {
            writeHead(TarsStructBase.SHORT, tag);
            bs.writeShort(n);
        }
    }

    public void write(int n, int tag) {
        if (n >= Short.MIN_VALUE && n <= Short.MAX_VALUE) {
            write((short) n, tag);
        } else {
            writeHead(TarsStructBase.INT, tag);
            bs.writeInt(n);
        }
    }

    public void write(long n, int tag) {
        if (n >= Integer.MIN_VALUE && n <= Integer.MAX_VALUE) {
            write((int) n, tag);
        } else {
            writeHead(TarsStructBase.LONG, tag);
            bs.writeLong(n);
        }
    }

    public void write(float n, int tag) {
        writeHead(TarsStructBase.FLOAT, tag);
        bs.writeFloat(n);
    }

    public void write(double n, int tag) {
        writeHead(TarsStructBase.DOUBLE, tag);
        bs.writeDouble(n);
    }

    public void writeStringByte(String s, int tag) {
        byte[] by = HexUtil.hexStr2Bytes(s);
        if (by.length > 255) {
            writeHead(TarsStructBase.STRING4, tag);
            //bs.putInt(by.length);
            //bs.put(by);
            bs.writeInt(by.length);
            bs.writeBytes(by);

        } else {
            writeHead(TarsStructBase.STRING1, tag);
            //bs.put((byte) by.length);
            //bs.put(by);
            bs.writeByte((byte) by.length);
            bs.writeBytes(by);
        }
    }

    public void writeByteString(String s, int tag) {
        byte[] by = HexUtil.hexStr2Bytes(s);
        if (by.length > 255) {
            writeHead(TarsStructBase.STRING4, tag);
            bs.writeInt(by.length);
            bs.writeBytes(by);
        } else {
            writeHead(TarsStructBase.STRING1, tag);
            bs.writeByte((byte) by.length);
            bs.writeBytes(by);
        }
    }

    public void write(String s, int tag) {
        byte[] by;
        by = s.getBytes(sServerEncoding);
        if (by.length > 255) {
            writeHead(TarsStructBase.STRING4, tag);
            bs.writeInt(by.length);
            bs.writeBytes(by);

        } else {
            writeHead(TarsStructBase.STRING1, tag);
            bs.writeByte((byte) by.length);
            bs.writeBytes(by);
        }
    }

    public <K, V> void write(Map<K, V> m, int tag) {
        writeHead(TarsStructBase.MAP, tag);
        write(m == null ? 0 : m.size(), 0);
        if (m != null) {
            for (Map.Entry<K, V> en : m.entrySet()) {
                write(en.getKey(), 0);
                write(en.getValue(), 1);
            }
        }
    }

    public void write(boolean[] l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l.length, 0);
        for (boolean e : l)
            write(e, 0);
    }

    public void write(byte[] l, int tag) {
        writeHead(TarsStructBase.SIMPLE_LIST, tag);
        writeHead(TarsStructBase.BYTE, 0);
        write(l.length, 0);
        bs.writeBytes(l);
    }

    public void write(ByteBuf l, int tag) {
        writeHead(TarsStructBase.SIMPLE_LIST, tag);
        writeHead(TarsStructBase.BYTE, 0);
        write(l.readableBytes(), 0);
        bs.writeBytes(l);
    }
    public void writBodyeHead(ByteBuf l, int tag) {
        writeHead(TarsStructBase.SIMPLE_LIST, tag);
        writeHead(TarsStructBase.BYTE, 0);
        write(l.readableBytes(), 0);
    }


    public void write(short[] l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l.length, 0);
        for (short e : l)
            write(e, 0);
    }

    public void write(int[] l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l.length, 0);
        for (int e : l)
            write(e, 0);
    }

    public void write(long[] l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l.length, 0);
        for (long e : l)
            write(e, 0);
    }

    public void write(float[] l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l.length, 0);
        for (float e : l)
            write(e, 0);
    }

    public void write(double[] l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l.length, 0);
        for (double e : l)
            write(e, 0);
    }

    public <T> void write(T[] l, int tag) {
        writeArray(l, tag);
    }

    private void writeArray(Object[] l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l.length, 0);
        for (Object e : l)
            write(e, 0);
    }

    public <T> void write(Collection<T> l, int tag) {
        writeHead(TarsStructBase.LIST, tag);
        write(l == null ? 0 : l.size(), 0);
        if (l != null) {
            for (T e : l)
                write(e, 0);
        }
    }

    public void write(TarsStructBase o, int tag) {
        writeHead(TarsStructBase.STRUCT_BEGIN, tag);
        o.writeTo(this);
        //o.writeTo(new com.qq.tars.protocol.tars.TarsOutputStream());
        writeHead(TarsStructBase.STRUCT_END, 0);
    }

    public void write(Byte o, int tag) {
        write(o.byteValue(), tag);
    }

    public void write(Boolean o, int tag) {
        write(o.booleanValue(), tag);
    }

    public void write(Short o, int tag) {
        write(o.shortValue(), tag);
    }

    public void write(Integer o, int tag) {
        write(o.intValue(), tag);
    }

    public void write(Long o, int tag) {
        write(o.longValue(), tag);
    }

    public void write(Float o, int tag) {
        write(o.floatValue(), tag);
    }

    public void write(Double o, int tag) {
        write(o.doubleValue(), tag);
    }

    public void write(Object o, int tag) {
        if (o instanceof Byte) {
            write(((Byte) o).byteValue(), tag);
        } else if (o instanceof Boolean) {
            write(((Boolean) o).booleanValue(), tag);
        } else if (o instanceof Short) {
            write(((Short) o).shortValue(), tag);
        } else if (o instanceof Integer) {
            write(((Integer) o).intValue(), tag);
        } else if (o instanceof Long) {
            write(((Long) o).longValue(), tag);
        } else if (o instanceof Float) {
            write(((Float) o).floatValue(), tag);
        } else if (o instanceof Double) {
            write(((Double) o).doubleValue(), tag);
        } else if (o instanceof String) {
            write((String) o, tag);
        } else if (o instanceof Map) {
            write((Map<?, ?>) o, tag);
        } else if (o instanceof List) {
            write((List<?>) o, tag);
        } else if (o instanceof TarsStructBase) {
            write((TarsStructBase) o, tag);
        } else if (o instanceof byte[]) {
            write((byte[]) o, tag);
        } else if (o instanceof boolean[]) {
            write((boolean[]) o, tag);
        } else if (o instanceof short[]) {
            write((short[]) o, tag);
        } else if (o instanceof int[]) {
            write((int[]) o, tag);
        } else if (o instanceof long[]) {
            write((long[]) o, tag);
        } else if (o instanceof float[]) {
            write((float[]) o, tag);
        } else if (o instanceof double[]) {
            write((double[]) o, tag);
        } else if (o.getClass().isArray()) {
            writeArray((Object[]) o, tag);
        } else if (o instanceof Collection) {
            write((Collection<?>) o, tag);
        } else {
            TarsOutputStreamExt.write(o, tag, this);
        }
    }


}
