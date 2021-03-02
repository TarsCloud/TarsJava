package com.qq.tars.client.rpc;

/**
 * 传输层服务器。需要实现{@code bind}方法来绑定服务地址与端口号。
 *
 * @author kongyuanyuan
 */
public interface TransporterServer {
    /**
     * 执行绑定动作。
     */
    void bind();
}
