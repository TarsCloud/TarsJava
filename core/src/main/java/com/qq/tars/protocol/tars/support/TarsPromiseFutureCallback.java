package com.qq.tars.protocol.tars.support;


import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.cluster.ServantInvokerAliveChecker;
import com.qq.tars.client.rpc.tars.TarsInvoker;
import com.qq.tars.common.util.Constants;
import com.qq.tars.net.client.Callback;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.exc.ServerException;
import com.qq.tars.rpc.exc.TarsException;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.support.log.LoggerFactory;
import com.qq.tars.support.stat.InvokeStatHelper;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * jce的CompletableFuture的实现
 * @author Kerrigan
 */
public class TarsPromiseFutureCallback<V> implements Callback<TarsServantResponse> {
    private static final Logger logger = LoggerFactory.getClientLogger();


    private final String objName;
    private final String methodName;
    private final String remoteIp;
    private final int remotePort;
    private final long bornTime;
    private final TarsInvoker<?> invoker;
    private final ServantProxyConfig config;

    private final CompletableFuture<V> completableFuture;//future

    public TarsPromiseFutureCallback(ServantProxyConfig config, String methodName, String remoteIp, int remotePort,
                                     long bornTime, TarsInvoker<?> invoker, CompletableFuture<V> completableFuture) {
        this.config = config;
        this.objName = config.getSimpleObjectName();
        this.methodName = methodName;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.bornTime = bornTime;
        this.invoker = invoker;
        this.completableFuture = completableFuture;
    }

    public void onCompleted(TarsServantResponse response) {
        int ret = Constants.INVOKE_STATUS_SUCC;
        try {
            if (response.getCause() != null) {
                throw new TarsException(response.getCause());

            }
            if (response.getRet() != TarsHelper.SERVERSUCCESS) {
                throw ServerException.makeException(response.getRet());
            }
            TarsPromiseFutureCallback.this.completableFuture.complete((V) response.getResult());
        } catch (Throwable ex) {
            ret = Constants.INVOKE_STATUS_EXEC;
            logger.error("error occurred on callback completed", ex);
            onException(ex);
        } finally {
            invoker.setAvailable(ServantInvokerAliveChecker.isAlive(invoker.getUrl(), config, ret));
            InvokeStatHelper.getInstance().addProxyStat(objName)
                    .addInvokeTimeByClient(config.getMasterName(), config.getSlaveName(), config.getSlaveSetName(), config.getSlaveSetArea(),
                            config.getSlaveSetID(), methodName, remoteIp, remotePort, ret,
                            System.currentTimeMillis() - bornTime
                    );
        }
    }

    public void onException(Throwable e) {
        try {
            TarsPromiseFutureCallback.this.completableFuture.completeExceptionally(e);
        } catch (Throwable ex) {
            logger.error("error occurred on callback exception", ex);
        }
    }

    public void onExpired() {
        int ret = Constants.INVOKE_STATUS_TIMEOUT;
        try {
            TarsPromiseFutureCallback.this.completableFuture.completeExceptionally(ServerException.makeException(ret));
        } finally {
            invoker.setAvailable(ServantInvokerAliveChecker.isAlive(invoker.getUrl(), config, ret));
            InvokeStatHelper.getInstance().addProxyStat(objName)
                    .addInvokeTimeByClient(config.getMasterName(), config.getSlaveName(), config.getSlaveSetName(), config.getSlaveSetArea(),
                            config.getSlaveSetID(), methodName, remoteIp, remotePort, ret,
                            System.currentTimeMillis() - bornTime
                    );
        }
    }
}
