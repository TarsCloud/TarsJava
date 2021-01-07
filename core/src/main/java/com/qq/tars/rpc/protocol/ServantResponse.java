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


import com.qq.tars.client.rpc.Response;

public abstract class ServantResponse implements Response {
    protected transient boolean asyncMode = false;
    private volatile boolean committed = false;

    private int requestId;

    public ServantResponse(int requestId) {
        this.requestId = requestId;
    }

    public boolean isAsyncMode() {
        return asyncMode;
    }


    public abstract int getRequestId();

    public void asyncCallStart() {
        this.asyncMode = true;
    }

    public void asyncCallEnd() {
        if (!this.asyncMode) throw new IllegalStateException("The response is not async mode.");
        ensureNotCommitted();
    }

    public synchronized void ensureNotCommitted() {
        if (committed) throw new IllegalStateException("Not allowed after response has committed.");
        this.committed = true;
    }
}
