package io.github.createsequence.rpc4j.core.transport.netty.codec;

import io.github.createsequence.common.ComponentManager;
import io.github.createsequence.common.DefaultComponentManager;
import io.github.createsequence.rpc4j.core.client.Client;
import io.github.createsequence.rpc4j.core.client.ConnectionFactory;
import io.github.createsequence.rpc4j.core.client.DefaultClient;
import io.github.createsequence.rpc4j.core.client.loadbalance.LoadBalancer;
import io.github.createsequence.rpc4j.core.client.loadbalance.RandomLoadBalancer;
import io.github.createsequence.rpc4j.core.compress.CompressionType;
import io.github.createsequence.rpc4j.core.compress.Compressor;
import io.github.createsequence.rpc4j.core.compress.NoneCompressor;
import io.github.createsequence.rpc4j.core.discoverer.LocalServiceDiscoverer;
import io.github.createsequence.rpc4j.core.discoverer.ServiceDiscoverer;
import io.github.createsequence.rpc4j.core.serialize.FastjsonSerializer;
import io.github.createsequence.rpc4j.core.serialize.SerializationType;
import io.github.createsequence.rpc4j.core.serialize.Serializer;
import io.github.createsequence.rpc4j.core.server.DefaultServerProvider;
import io.github.createsequence.rpc4j.core.server.RequestHandler;
import io.github.createsequence.rpc4j.core.server.Server;
import io.github.createsequence.rpc4j.core.server.ServerProvider;
import io.github.createsequence.rpc4j.core.server.ServerProviderRequestHandler;
import io.github.createsequence.rpc4j.core.support.DefaultRequest;
import io.github.createsequence.rpc4j.core.support.Response;
import io.github.createsequence.rpc4j.core.transport.ProtocolVersion;
import io.github.createsequence.rpc4j.core.transport.netty.Rpc4jProtocolSupportInterceptor;
import io.github.createsequence.rpc4j.core.transport.netty.client.NettyClientConnectionFactory;
import io.github.createsequence.rpc4j.core.transport.netty.server.NettyServer;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * see {@link Rpc4jNettyEncoder} and {@link Rpc4jNettyDecoder}
 *
 * @author huangchengxing
 */
public class Rpc4jNettyCodecTest {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8086;
    private Server server;
    private Client client;

    @Before
    public void init() {
        ComponentManager componentManager = new DefaultComponentManager();
        componentManager.registerComponent(Compressor.class, CompressionType.NONE.getName(), new NoneCompressor());
        componentManager.registerComponent(Serializer.class, SerializationType.FASTJSON.getName(), new FastjsonSerializer());
        ProtocolVersion protocolVersion = ProtocolVersion.V1;
        ServiceDiscoverer serviceDiscoverer = new LocalServiceDiscoverer();

        Rpc4jProtocolSupportInterceptor rpc4jProtocolSupportInterceptor = new Rpc4jProtocolSupportInterceptor(
            protocolVersion, SerializationType.FASTJSON, CompressionType.NONE
        );

        // 服务端配置
        DefaultServerProvider serverProvider = new DefaultServerProvider();
        serverProvider.registerService(Service.class, new ServiceImpl());
        ServerProviderRequestHandler requestHandler = new ServerProviderRequestHandler(serverProvider);
        requestHandler.addInterceptor(rpc4jProtocolSupportInterceptor);
        this.server = new NettyServer(
            HOST, PORT, protocolVersion, requestHandler, componentManager
        );
        serviceDiscoverer.registerService(Service.class.getName(), InetSocketAddress.createUnresolved(HOST, PORT));

        // 客户端配置
        ConnectionFactory connectionFactory = new NettyClientConnectionFactory(protocolVersion, componentManager);
        LoadBalancer loadBalancer = new RandomLoadBalancer();
        DefaultClient defaultClient = new DefaultClient(connectionFactory, loadBalancer, serviceDiscoverer);
        defaultClient.addInterceptor(rpc4jProtocolSupportInterceptor);
        this.client = defaultClient;
    }

    @Test
    public void testServer() {
        server.start();
    }

    @SneakyThrows
    @Test
    public void testClient() {
        String requestId = UUID.randomUUID().toString();
        Method method = Service.class.getDeclaredMethod("hello", String.class);
        Response response = client.request(DefaultRequest.fromMethodInvocation(
            requestId, method, new Object[]{ "client" }
        ));
        System.out.println(response.getResult());
    }

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
