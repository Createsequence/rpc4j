/**
 * @author huangchengxing
 */
module rpc4j.common {

    requires org.slf4j;
    requires lombok;
    requires org.checkerframework.checker.qual;
    requires com.google.common;

    exports io.github.createsequence.common.util;
    exports io.github.createsequence.common;
}