package org.laurentforet.beans;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author lforet
 */
public class BeanWalkerTest {

    @Test
    public void testNull() {
        assertTrue(
                new BeanWalker(Functions.toStringFunction())
                .walk(null).getResults().isEmpty());
    }

    @Test
    public void testStringObject() {
        assertFalse(
                new BeanWalker<>(Functions.toStringFunction())
                .walk("String").getResults().isEmpty());
    }

    @Test
    public void testInterface() {
        BeanWalker<A, String> beanWalker =
                new BeanWalker<>(new AToStringFunction()).walk(new AImpl());
        assertEquals(beanWalker.getResults().size(), 1);
    }

    @Test
    public void testCollection() {
        List<A> as = new ArrayList<>();
        as.add(new AImpl());
        as.add(new AImpl());
        as.add(new AImpl());

        BeanWalker<A, String> beanWalker =
                new BeanWalker<>(new AToStringFunction()).walk(as);
        assertEquals(beanWalker.getResults().size(), 3);
    }

    @Test
    public void testWrapper() {
        A a = new AImpl();
        AWrapperA wrapper = new AWrapperA();
        wrapper.addA(a);
        BeanWalker<A, String> beanWalker =
                new BeanWalker<>(new AToStringFunction()).walk(wrapper);
        assertEquals(beanWalker.getResults().size(), 2);
    }

    @Test
    public void testWrapperNestedAndFormATree() {
        AWrapperA a = new AWrapperA();
        AWrapperA a1 = new AWrapperA();
        AWrapperA a11 = new AWrapperA();
        AWrapperA a12 = new AWrapperA();
        AWrapperA a2 = new AWrapperA();
        a.addA(a1);
        a1.addA(a11);
        a1.addA(a12);
        a.addA(a2);
        BeanWalker<A, String> beanWalker =
                new BeanWalker<>(new AToStringFunction()).walk(a);
        assertEquals(beanWalker.getResults().size(), 5);
    }

    @Test
    public void testCycle() {
        AWrapperA a = new AWrapperA();
        AWrapperA b = new AWrapperA();
        a.addA(b);
        b.addA(a);
        BeanWalker<A, String> beanWalker =
                new BeanWalker<>(new AToStringFunction()).walk(a);
        assertEquals(beanWalker.getResults().size(), 2);
    }

    @Test
    public void testLarge() {
        AWrapperA a = new AWrapperA();
        AWrapperA[] a1s = new AWrapperA[100];
        for (int i = 0; i < a1s.length; i++) {
            a1s[i] = new AWrapperA();
           
            AWrapperA[] a11s = new AWrapperA[1000];
            for (int j = 0; j < a11s.length; j++) {
                a11s[j] = new AWrapperA();
            }
            a1s[i].addAll(Arrays.asList(a11s));
        }
        a.addAll(Arrays.asList(a1s));
        BeanWalker<A, String> beanWalker =
                new BeanWalker<>(new AToStringFunction()).walk(Arrays.asList(a));
        assertEquals(beanWalker.getResults().size(), 101001);
    }
    @Test
    public void testDeep() {
        //TODO
    }
    public interface A {
    }

    public class AImpl implements A {
    }

    public class AWrapperA implements A {

        private Collection<A> someAs = new ArrayList<>();

        public void addA(A a) {
            someAs.add(a);
        }

        public void addAll(Collection<? extends A> as) {
            someAs.addAll(as);
        }

        public Collection<A> getSomeAs() {
            return someAs;
        }
    }

    static class AToStringFunction implements Function<A, String> {

        public AToStringFunction() {
        }

        public String apply(A input) {
            return input.toString();
        }
    }
}
