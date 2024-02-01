package io.github.createsequence.rpc4j.core.compress;

/**
 * 压缩器
 *
 * @author huangchengxing
 */
public interface Compressor {

    /**
     * 压缩
     *
     * @param bytes 字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩字节数组
     *
     * @param bytes 已压缩的字节数组
     * @return 解压缩后的字节数组
     */
    byte[] decompress(byte[] bytes);
}
