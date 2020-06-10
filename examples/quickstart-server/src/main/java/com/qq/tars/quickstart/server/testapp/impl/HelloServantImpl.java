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

package com.qq.tars.quickstart.server.testapp.impl;

import ch.qos.logback.classic.Logger;
import com.qq.tars.common.support.Holder;
import com.qq.tars.protocol.tars.annotation.TarsMethodParameter;
import com.qq.tars.quickstart.server.testapp.HelloServant;
import com.qq.tars.quickstart.server.testapp.TestInfo;
import com.qq.tars.quickstart.server.testapp.TestInfoEx;
import com.qq.tars.support.log.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class HelloServantImpl implements HelloServant {

    private static Logger logger = LoggerFactory.getLogger("hello");

    public HelloServantImpl() {
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }

    @Override
    public String hello(int no, String name) {
        return String.format("hello no=%s, name=%s, time=%s", no, name, System.currentTimeMillis());
    }

    @Override
    public int helloJson(TestInfo tie, Holder<TestInfoEx> otie) {
        logger.info("ready to hello Json. s: {}, f: {}, d: {}", tie.s, tie.f, tie.d);

        if (otie.value == null) {
            logger.info("hello Json. holder value is null.");
            otie.value = new TestInfoEx();
        }

        otie.value.bi = tie;
        otie.value.bi.s += otie.value.bi.s;
        otie.value.bi.f += otie.value.bi.f;
        otie.value.bi.d += otie.value.bi.d;

        otie.value.mbi = new HashMap<>(2);
        otie.value.mbi.put("A", otie.value.bi);
        otie.value.mbi.put("B", otie.value.bi);

        otie.value.vbi = new ArrayList<>(2);
        otie.value.vbi.add(otie.value.bi);
        otie.value.vbi.add(otie.value.bi);

        logger.info("succ. to hello Json. s: {}, f: {}, d: {}", tie.s, tie.f, tie.d);

        return 0;
    }


}
