package io.github.createsequence.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link Ordered}
 *
 * @author huangchengxing
 */
public class OrderedTest {

    @Test
    public void getSort() {
        Assert.assertEquals(Integer.MAX_VALUE, new Foo().getOrder());
    }

    private static class Foo implements Ordered {
    }
}
