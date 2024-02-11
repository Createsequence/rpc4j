/**
 * @author huangchengxing
 */
module rpc4j.core {
    
    requires static lombok;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires com.alibaba.fastjson2;
    requires io.netty.all;
    requires rpc4j.common;

    opens io.github.createsequence.rpc4j.core.transport;
}