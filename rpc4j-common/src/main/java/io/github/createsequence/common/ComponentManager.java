package io.github.createsequence.common;

/**
 * 组件管理器
 *
 * @author huangchengxing
 */
public interface ComponentManager {

    /**
     * 注册组件
     *
     * @param componentType 组件类型
     * @param componentName 组件名称
     * @param component 组件
     */
    <C> void registerComponent(Class<C> componentType, String componentName, C component);

    /**
     * 获取组件
     *
     * @param componentType 组件类型
     * @param componentName 组件名称
     * @param <C> 组件类型
     * @return 组件
     */
    <C> C getComponent(Class<C> componentType, String componentName);
}
