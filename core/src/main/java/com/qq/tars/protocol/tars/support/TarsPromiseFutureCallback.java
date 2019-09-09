package com.qq.tars.protocol.tars.support;


import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.cluster.ServantnvokerAliveChecker;
import com.qq.tars.client.rpc.tars.TarsInvoker;
import com.qq.tars.client.util.ClientLogger;
import com.qq.tars.common.util.Constants;
import com.qq.tars.net.client.Callback;
import com.qq.tars.protocol.util.TarsHelper;
import com.qq.tars.rpc.exc.ServerException;
import com.qq.tars.rpc.exc.TarsException;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import com.qq.tars.support.stat.InvokeStatHelper;

import java.util.concurrent.CompletableFuture;

/**
 * jce的CompletableFuture的实现
 * @author Kerrigan
 */
public class TarsPromiseFutureCallback<V> implements Callback<TarsServantResponse> {

    private final String objName;
    private final String methodName;
    private final String remoteIp;
    private final int remotePort;
    private final long bornTime;
    private final TarsInvoker<?> invoker;
    private final ServantProxyConfig config;

    private final CompletableFuture<V> completableFuture;//上层传入的周期操作，仅在callback操作的时候，是会处理的。在回调听时候 。才会操作

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
                completableFuture.completeExceptionally(new TarsException(response.getCause()));
                throw new TarsException(response.getCause());

            }
            if (response.getRet() != TarsHelper.SERVERSUCCESS) {
                completableFuture.completeExceptionally(ServerException.makeException(response.getRet()));
                throw ServerException.makeException(response.getRet());
            }
            TarsPromiseFutureCallback.this.completableFuture.complete((V) response.getResult());
        } catch (Throwable ex) {
            ret = Constants.INVOKE_STATUS_EXEC;
            ClientLogger.getLogger().error("error occurred on callback completed", ex);
            completableFuture.completeExceptionally(ServerException.makeException(ret));
            onException(ex);
        } finally {
            //String masterName, String slaveName, String slaveSetName, String slaveSetArea, String slaveSetID, String methodName,
            //                                      String slaveIp, int slavePort, int result, long costTimeMill
            /// .addInvokeTimeByClient(config.getMasterName(), config.getSlaveName(), config.getSlaveSetName(), config.getSlaveSetArea(),
            //                                config.getSlaveSetID(), inv.getMethodName(), getUrl().getHost(), getUrl().getPort(), ret, System.currentTimeMillis() - begin)
            invoker.setAvailable(ServantnvokerAliveChecker.isAlive(invoker.getUrl(), config, ret));
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
            ClientLogger.getLogger().error("error occurred on callback exception", ex);
        }
    }

    public void onExpired() {
        int ret = Constants.INVOKE_STATUS_TIMEOUT;
        try {
            TarsPromiseFutureCallback.this.completableFuture.completeExceptionally(ServerException.makeException(ret));
        } finally {
            invoker.setAvailable(ServantnvokerAliveChecker.isAlive(invoker.getUrl(), config, ret));
            InvokeStatHelper.getInstance().addProxyStat(objName)
                    .addInvokeTimeByClient(config.getMasterName(), config.getSlaveName(), config.getSlaveSetName(), config.getSlaveSetArea(),
                            config.getSlaveSetID(), methodName, remoteIp, remotePort, ret,
                            System.currentTimeMillis() - bornTime
                    );
        }
    }
}
