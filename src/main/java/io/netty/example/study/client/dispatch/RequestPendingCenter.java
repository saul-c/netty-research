package io.netty.example.study.client.dispatch;

import io.netty.example.study.common.OperationResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : chenglong.ma@shuyun.com
 * @date : 2020/4/7
 */
public class RequestPendingCenter {
    private Map<Long, OperationResultFuture> map = new ConcurrentHashMap<>();

    public void add(Long streamId, OperationResultFuture future) {
        this.map.put(streamId, future);
    }

    public void set(Long streamId, OperationResult operationResult) {
        OperationResultFuture future = this.map.get(streamId);
        if(future != null) {
            future.setSuccess(operationResult);
            this.map.remove(streamId);
        }
    }
}