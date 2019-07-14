package com.helospark.tactview.core;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.helospark.tactview.core.util.StatefulCloneable;

public class ReflectionUtilTest {

    @Test
    public void testCopy() {
        // GIVEN
        SimpleTest simpleTest = new SimpleTest("immutable", new MyCloneable("asd"));
        SimpleTest result = new SimpleTest();

        // WHEN
        ReflectionUtil.copyOrCloneFieldFromTo(simpleTest, result);

        // THEN
        assertTrue(simpleTest.immutable == result.immutable);
        assertTrue(simpleTest.cloneable != result.cloneable);
        assertTrue(simpleTest.cloneable.equals(result.cloneable));
    }

    static class SimpleTest {
        static final String shouldNotClone = "";
        String immutable;
        MyCloneable cloneable;

        public SimpleTest() {
        }

        public SimpleTest(String string, MyCloneable myCloneable) {
            this.immutable = string;
            this.cloneable = myCloneable;
        }
    }

    static class MyCloneable implements StatefulCloneable<MyCloneable> {
        private List<String> object;

        public MyCloneable(String asd) {
            this.object = List.of(asd);
        }

        @Override
        public MyCloneable deepClone() {
            return new MyCloneable(object.get(0));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((object == null) ? 0 : object.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MyCloneable other = (MyCloneable) obj;
            if (object == null) {
                if (other.object != null)
                    return false;
            } else if (!object.equals(other.object))
                return false;
            return true;
        }

    }

}
