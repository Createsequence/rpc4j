package io.github.createsequence.rpc4j.core.compress;

/**
 * 不做任何压缩
 *
 * @author huangchengxing
 */
public class NoneCompressor implements Compressor {

    /**
     * 压缩
     *
     * @param bytes 字节数组
     * @return 压缩后的字节数组
     */
    @Override
    public byte[] compress(byte[] bytes) {
        return bytes;
    }

    /**
     * 解压缩字节数组
     *
     * @param bytes 已压缩的字节数组
     * @return 解压缩后的字节数组
     */
    @Override
    public byte[] decompress(byte[] bytes) {
        return bytes;
    }
}
