package io.github.createsequence.rpc4j.core.support.handler;

import io.github.createsequence.common.exception.Rpc4jException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 在调用前，检查上下文中是否具备在{@link Depends}注解中指定的必要属性
 *
 * @author huangchengxing
 */
public class RequiredAttributeCheckHandlerDelegate extends InvocationHandlerDelegate {

    private static final Checker EMPTY = new Checker(Map.of());

    public RequiredAttributeCheckHandlerDelegate(RpcInvocationHandler delegate) {
        super(delegate);
    }

    /**
     * 调用前处理
     *
     * @param rpcInvocation 调用参数
     * @return 调用参数
     */
    @Override
    protected RpcInvocation beforeInvoke(RpcInvocation rpcInvocation) {
        Checker checker = getChecker();
        if (checker != EMPTY) {
            checker.check(rpcInvocation);
        }
        return rpcInvocation;
    }

    /**
     * 获取依赖注解
     *
     * @return 依赖注解
     */
    @Nullable
    protected Depends retrieveDependsAnnotation() {
        RpcInvocationHandler delegate = getDelegate();
        return delegate.getClass().getAnnotation(Depends.class);
    }

    private Checker getChecker() {
        Depends annotation = retrieveDependsAnnotation();
        if (Objects.isNull(annotation)) {
            return EMPTY;
        }
        Map<String, Class<?>> requiredAttributes = Stream.of(annotation.value())
            .filter(Depends.Attr::required)
            .collect(Collectors.toMap(Depends.Attr::name, Depends.Attr::type));
        if (requiredAttributes.isEmpty()) {
            return EMPTY;
        }
        return new Checker(Map.copyOf(requiredAttributes));
    }

    private record Checker(Map<String, Class<?>> requiredAttributes) {
        public void check(RpcInvocation rpcInvocation) {
            requiredAttributes.forEach((name, type) -> {
                Object attribute = rpcInvocation.getAttribute(name);
                if (type.isInstance(attribute)) {
                    return;
                }
                throw new RequiredAttributeMissingException("上下文中缺少必要属性：[{}]({})", name, type.getName());
            });
        }
    }

    public static class RequiredAttributeMissingException extends Rpc4jException {
        public RequiredAttributeMissingException(String messageTemplate, Object... args) {
            super(messageTemplate, args);
        }
    }
}
