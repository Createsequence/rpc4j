package io.github.createsequence.rpc4j.core.transport.netty.client;

import io.github.createsequence.common.Rpc4jException;
import io.github.createsequence.common.util.Asserts;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 请求注册表，当客户端的IO线程发起一个请求后，
 * 将会将注册表注册任务，并阻塞等待任务线程异步的完成任务。
 * 当等待超时，或者任务线程完成任务后，将会唤醒IO线程。
 *
 * @author huangchengxing
 */
@Slf4j
public class RequestRegistry {

    private final ConcurrentMap<String, CompletableFuture<Object>> tasks = new ConcurrentHashMap<>();

    /**
     * 注册并返回一个任务，并且该任务：
     * <ul>
     *     <li>在发生异常或等待超时时，将会返回一个默认值；</li>
     *     <li>在任务完成后，总是将会从注册表中移除该任务；</li>
     * </ul>
     *
     * @param requestId 请求ID
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @param defaultValue 当等待超时或执行过程中抛出异常时，需要返回的默认值
     * @return 任务执行结果
     */
    public <T> CompletableFuture<Object> register(
        String requestId, long timeout, TimeUnit timeUnit, T defaultValue) {
        Asserts.isFalse(tasks.containsKey(requestId), "已有相同ID的请求正在等待完成: {}", requestId);
        CompletableFuture<Object> future = new CompletableFuture<>()
            .completeOnTimeout(defaultValue, timeout, timeUnit)
            .exceptionally(throwable -> {
                log.error("请求[{}]执行过程中发生异常！", requestId, throwable);
                return defaultValue;
            });
        future.thenRun(() -> tasks.remove(requestId));
        tasks.put(requestId, future);
        return future;
    }

    /**
     * 完成请求
     *
     * @param requestId 请求ID
     * @param result 请求结果，或执行过程中发生的异常
     */
    public void complete(String requestId, Object result) {
        CompletableFuture<Object> future = tasks.get(requestId);
        if (Objects.nonNull(future) && !future.isDone()) {
            future.complete(result);
        }
    }
}
