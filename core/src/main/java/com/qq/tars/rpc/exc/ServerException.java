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

package com.qq.tars.rpc.exc;

import static com.qq.tars.protocol.util.TarsHelper.*;

@SuppressWarnings("serial")
public class ServerException extends TarsException {

    private static final long serialVersionUID = 5930969532642083140L;
    private int ret;

    public ServerException(int ret) {
        super("server error code " + ret);
        this.ret = ret;
    }

    public ServerException(int ret, String message) {
        super("server error code " + ret + "|" + message);
        this.ret = ret;
    }

    public int getRet() {
        return ret;
    }

    public static TarsException makeException(int ret) {
        return makeException(ret, "");
    }

    public static TarsException makeException(int ret, String message) {
        if (ret == SERVERDECODEERR) {
            return new ServerDecodeException(ret, message);
        } else if (ret == SERVERENCODEERR) {
            return new ServerEncodeException(ret, message);
        } else if (ret == SERVERNOFUNCERR) {
            return new ServerNoFuncException(ret, message);
        } else if (ret == SERVERNOSERVANTERR) {
            return new ServerNoServantException(ret, message);
        } else if (ret == SERVERQUEUETIMEOUT) {
            return new ServerQueueTimeoutException(ret, message);
        } else if (ret == SERVERRESETGRID) {
            return new ServerResetGridException(ret, message);
        } else if (ret == SERVEROVERLOAD) {
            return new ServerOverloadException(ret, message);
        } else if (ret == SERVERINTERFACEERR) {
            return new ServerImpException(ret, message);
        } else {
            return new ServerUnknownException(ret, message);
        }
    }
}
