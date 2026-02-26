package com.qq.tars.server.core;

import org.junit.Test;

public class ServantAdapterTest {

    @Test
    public void testStopWithoutBind() {
        ServantAdapter adapter = new ServantAdapter(null);
        adapter.stop();
    }
}
