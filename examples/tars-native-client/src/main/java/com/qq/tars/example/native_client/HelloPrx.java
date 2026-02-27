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
package com.qq.tars.example.native_client;

import com.qq.tars.protocol.annotation.Servant;
import com.qq.tars.protocol.tars.annotation.TarsMethodParameter;

/**
 * Hello服务代理接口 - 用于GraalVM Native Image演示
 */
@Servant
public interface HelloPrx {

    /**
     * 简单的hello方法
     * @param name 名称
     * @return 问候语
     */
    String hello(@TarsMethodParameter(name = "name") String name);

    /**
     * 加法运算
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之和
     */
    int add(@TarsMethodParameter(name = "a") int a, @TarsMethodParameter(name = "b") int b);
}
