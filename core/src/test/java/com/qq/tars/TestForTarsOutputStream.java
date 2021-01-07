package com.qq.tars;

import com.qq.tars.common.util.HexUtil;
import com.qq.tars.protocol.tars.TarsOutputStream;
import io.netty.buffer.ByteBuf;
import org.junit.Test;

import java.util.Arrays;

public class TestForTarsOutputStream {
    @Test
    public void maxcapcity() {
        TarsOutputStream os = new TarsOutputStream();
        long n = 0x1234567890012345L;
        os.write(n, 0);
        ByteBuf bs = os.getByteBuffer().duplicate();
        System.out.println(HexUtil.bytes2HexStr(bs.array()));
        System.out.println(Arrays.toString(os.toByteArray()));

    }
}
