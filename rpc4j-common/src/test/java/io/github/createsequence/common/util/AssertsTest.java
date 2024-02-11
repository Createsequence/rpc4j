package io.github.createsequence.common.util;

import io.github.createsequence.common.exception.Rpc4jException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * test for {@link Asserts}
 *
 * @author huangchengxing
 */
@SuppressWarnings("all")
public class AssertsTest {

    @Test
    public void isNotEquals() {
        Object obj = new Object();
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEquals(obj, obj, () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEquals(obj, obj, "test"));
    }

    @Test
    public void isEquals() {
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEquals(new Object(), new Object(), () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEquals(new Object(), new Object(), "test"));
    }

    @Test
    public void isTrue() {
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isTrue(false, () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isTrue(false, "test"));
    }

    @Test
    public void isFalse() {
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isFalse(true, () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isFalse(true, "test"));
    }

    @Test
    public void isNull() {
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNull(new Object(), () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNull(new Object(), "test"));
    }

    @Test
    public void notNull() {
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotNull(null, () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotNull(null, "test"));
    }

    @Test
    public void isEmpty() {
        // object
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(new Object(), () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(new Object(), "test"));
        // array
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(new Object[1], () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(new Object[1], "test"));
        // collection
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(Collections.singletonList(1), () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(Collections.singletonList(1), "test"));
        // map
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(Collections.singletonMap(1, 1), () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty(Collections.singletonMap(1, 1), "test"));
        // string
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty("test", () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isEmpty("test", "test"));
    }

    @Test
    public void isNotEmpty() {
        // object
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(null, () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(null, "test"));
        // array
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(new Object[0], () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(new Object[0], "test"));
        // collection
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(CollectionUtils.newCollection(ArrayList::new), () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(CollectionUtils.newCollection(ArrayList::new), "test"));
        // map
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(Collections.emptyMap(), () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty(Collections.emptyMap(), "test"));
        // string
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty("", () -> new Rpc4jException("test")));
        Assert.assertThrows(Rpc4jException.class, () -> Asserts.isNotEmpty("", "test"));
    }

    @Test
    public void testNewInstance() {
        // Test with non-null component type and positive length
        Integer[] result1 = ArrayUtils.newInstance(Integer.class, 5);
        Assert.assertNotNull(result1);
        Assert.assertEquals(5, result1.length);

        // Test with non-null component type and zero length
        Integer[] result2 = ArrayUtils.newInstance(Integer.class, 0);
        Assert.assertNotNull(result2);
        Assert.assertEquals(0, result2.length);
    }

    @Test
    public void testToArray() {
        // Test with non-null collection and non-null component type
        List<String> list1 = Arrays.asList("a", "b", "c");
        String[] result1 = ArrayUtils.toArray(list1, String.class);
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, result1);

        // Test with null collection and non-null component type
        Collection<Integer> list2 = null;
        Integer[] result2 = ArrayUtils.toArray(list2, Integer.class);
        Assert.assertNotNull(result2);
        Assert.assertEquals(0, result2.length);
    }
}
