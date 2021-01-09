package com.qq.tars;

import com.google.common.collect.ImmutableList;
import com.qq.tars.protocol.tars.TarsOutputStream;
import org.junit.Test;

public class DecoderTest {


    @Test
    public void testForDecoder() {

        MonitorQueryReq request = new MonitorQueryReq();
        request.dateType = DateType.MINIUES.value();
        request.method = "query";
        request.setDataid("tars_stat");
        request.intervalTime = 1;
        request.setConditions(ImmutableList.of(new Condition("slave_name", 5, "tars.tarsstat")));
        request.indexs = ImmutableList.of("succ_count", "timeout_count", "exce_count", "total_time");
        request.setGroupby(ImmutableList.of("f_tflag"));
        request.startTime = 1606060800L;
        request.endTime = 1606147199L;


        TarsOutputStream tarsOutputStream = new TarsOutputStream();
        request.writeTo(tarsOutputStream);


        byte[] bytesArray = tarsOutputStream.toByteArray();

    }
}
