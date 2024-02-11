package io.github.createsequence.rpc4j.core.transport;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.DefaultComponentManager;
import io.github.createsequence.rpc4j.core.compress.Compressor;
import io.github.createsequence.rpc4j.core.compress.NoneCompressor;
import io.github.createsequence.rpc4j.core.discoverer.LocalServiceDiscoverer;
import io.github.createsequence.rpc4j.core.loadbalance.LoadBalancer;
import io.github.createsequence.rpc4j.core.loadbalance.RandomLoadBalancer;
import io.github.createsequence.rpc4j.core.serialize.FastjsonSerializer;
import io.github.createsequence.rpc4j.core.serialize.Serializer;
import io.github.createsequence.rpc4j.core.support.service.Reference;
import io.github.createsequence.rpc4j.core.support.service.Rpc4jNettyServiceManager;
import io.github.createsequence.rpc4j.core.support.service.ServiceProvider;
import io.github.createsequence.rpc4j.core.support.service.ServiceRegistry;
import io.github.createsequence.rpc4j.core.transport.codec.Rpc4jNettyDecoder;
import io.github.createsequence.rpc4j.core.transport.codec.Rpc4jNettyEncoder;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * see {@link Rpc4jNettyEncoder} and {@link Rpc4jNettyDecoder}
 *
 * @author huangchengxing
 */
public class Rpc4jNettyCodecTest {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8086;
    private ServiceProvider serviceProvider;
    private ServiceRegistry serviceRegistry;

    @Before
    public void init() {
        ComponentManager componentManager = new DefaultComponentManager();
        componentManager.registerComponent(Compressor.class, Rpc4jProtocol.CompressionType.NONE.getName(), new NoneCompressor());
        componentManager.registerComponent(Serializer.class, Rpc4jProtocol.SerializationType.FASTJSON.getName(), new FastjsonSerializer());
        componentManager.registerComponent(LoadBalancer.class, RandomLoadBalancer.class.getSimpleName(), new RandomLoadBalancer());
        Rpc4jNettyServiceManager manager = new Rpc4jNettyServiceManager(
            componentManager, new LocalServiceDiscoverer(), HOST, PORT
        );
        serviceProvider = manager;
        serviceRegistry = manager;
    }


    @SneakyThrows
    @Test
    public void testClient() {
        new Thread(() -> serviceRegistry.start(HOST, PORT)).start();
        serviceRegistry.export(Service.class, new ServiceImpl());
        Service service = serviceProvider.refer(Service.class);
        String result = service.hello("rpc4j");
        Assert.assertEquals("hello rpc4j", result);
    }

    @Reference(address = {
        @Reference.Address(host = HOST, port = PORT)
    })
    private interface Service {
        String hello(String name);
    }

    private static class ServiceImpl implements Service {
        @Override
        public String hello(String name) {
            return "hello " + name;
        }
    }
}
