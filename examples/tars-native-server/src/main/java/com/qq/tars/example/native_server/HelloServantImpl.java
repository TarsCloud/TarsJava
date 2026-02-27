/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package com.qq.tars.example.native_server;

/**
 * Hello服务实现 - 用于GraalVM Native Image演示
 */
public class HelloServantImpl implements HelloServant {

    @Override
    public String hello(String name) {
        return "Hello, " + name + "! (from native server)";
    }

    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
