package io.github.createsequence.common;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.createsequence.common.util.Asserts;

/**
 * 默认的组件注册表
 *
 * @author huangchengxing
 */
public class DefaultComponentManager implements ComponentManager {

    private final Table<Class<?>, String, Object> registeredComponents = HashBasedTable.create();

    /**
     * 注册组件
     *
     * @param componentType 组件类型
     * @param componentName 组件名称
     * @param component     组件
     */
    @Override
    public <C> void registerComponent(Class<C> componentType, String componentName, C component) {
        registeredComponents.put(componentType, componentName, component);
    }

    /**
     * 获取组件
     *
     * @param componentType 组件类型
     * @param componentName 组件名称
     * @param <C> 组件类型
     * @return 组件
     */
    @SuppressWarnings("unchecked")
    @Override
    public <C> C getComponent(Class<C> componentType, String componentName) {
        Object component = registeredComponents.get(componentType, componentName);
        Asserts.isNotNull(component, "找不到类型为[{}]的组件：{}", componentType, componentName);
        return (C) component;
    }
}
