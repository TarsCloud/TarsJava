package com.qq.tars;

import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.quickstart.server.testapp.TestRecursive;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class TestTarsRecursive {

    @Test
    public void testRecursiveReproduce() {

        TestRecursive testRecursive = new TestRecursive();
        testRecursive.value = 1;
        TestRecursive testRecursive2 = new TestRecursive();
        testRecursive2.value =2;
        testRecursive.testRecursive = testRecursive2;
//        System.out.println(testRecursive.testRecursive.getValue());

        TarsOutputStream tafOutputStream = new TarsOutputStream();
        System.out.println("begin to write.");
        testRecursive.writeTo(tafOutputStream);
        System.out.println("write end.");

    }
}
