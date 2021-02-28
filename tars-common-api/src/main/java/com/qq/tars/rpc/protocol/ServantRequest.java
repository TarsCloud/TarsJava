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

package com.qq.tars.rpc.protocol;


import com.qq.tars.client.rpc.Request;

import java.util.HashMap;
import java.util.Map;

public abstract class ServantRequest implements Request {
    protected int requestId;
    protected String servantName;
    protected String functionName;

    protected transient Request.InvokeStatus status = null;
    private transient HashMap<String, String> distributedContext = new HashMap<>(8);
    private transient long bornTime;
    private transient long processTime;

    public ServantRequest(int requestid) {
        this.requestId = requestid;
        this.bornTime = System.currentTimeMillis();
        exportDistributedContext(distributedContext);

    }

    public String getServantName() {
        return servantName;
    }

    public void setServantName(String servantName) {
        this.servantName = servantName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }


    private void exportDistributedContext(Map<String, String> map) {
    }

    public HashMap<String, String> getDistributedContext() {
        return distributedContext;
    }

    public void setDistributedContext(HashMap<String, String> map) {
        if (map != null) this.distributedContext = map;
        else this.distributedContext.clear();
    }


    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void setInvokeStatus(Request.InvokeStatus status) {
        this.status = status;
    }

    public Request.InvokeStatus getInvokeStatus() {
        return this.status;
    }

    public long getBornTime() {
        return bornTime;
    }

    public void resetBornTime() {
        this.bornTime = System.currentTimeMillis();
    }

    public long getProcessTime() {
        return processTime;
    }

    public void setProcessTime(long processTime) {
        this.processTime = processTime;
    }


}
