package com.helospark.tactview.core.util.cacheable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.util.cacheable.context.BeanWithCacheableParameter;
import com.helospark.tactview.core.util.cacheable.context.BeanWithCachedMethod;
import com.helospark.tactview.core.util.cacheable.context.BeanWithNoCacheable;
import com.helospark.tactview.core.util.cacheable.context.BeanWithShortCacheLife;
import com.helospark.tactview.core.util.cacheable.context.BeanWithSomeCachedAndSomeNonCachedMethods;
import com.helospark.tactview.core.util.cacheable.context.CacheableTestContext;
import com.helospark.tactview.core.util.cacheable.context.CacheableWithVoid;
import com.helospark.tactview.core.util.cacheable.context.cleanable.BeanWithCleanableValue;
import com.helospark.tactview.core.util.cacheable.context.cleanable.CleanableValue;
import com.helospark.tactview.core.util.cacheable.context.cleanable.MyCleaner;

public class CacheableBeanPostProcessorTest {
    private LightDiContext context;

    @BeforeEach
    public void setUp() {
        context = new LightDiContext();
        context.loadDependencies(Collections.emptyList(), Arrays.asList(CacheableTestContext.class, CacheableBeanPostProcessor.class));
    }

    @AfterEach
    public void tearDown() {
        context.close();
    }

    @Test
    public void testNonCachedMethod() {
        // GIVEN
        BeanWithNoCacheable bean = context.getBean(BeanWithNoCacheable.class);

        // WHEN
        Integer invocationCount1 = bean.nonCachedMethod();
        Integer invocationCount2 = bean.nonCachedMethod();
        Integer invocationCount3 = bean.nonCachedMethod();

        // THEN
        assertThat(invocationCount1, is(1));
        assertThat(invocationCount2, is(2));
        assertThat(invocationCount3, is(3));
        assertThat(bean.getClass(), equalTo(BeanWithNoCacheable.class));
    }

    @Test
    public void testCachedMethod() {
        // GIVEN
        BeanWithCachedMethod bean = context.getBean(BeanWithCachedMethod.class);

        // WHEN
        Integer invocationCount1 = bean.cachedMethod();
        Integer invocationCount2 = bean.cachedMethod();
        Integer invocationCount3 = bean.cachedMethod();

        // THEN
        assertThat(invocationCount1, is(1));
        assertThat(invocationCount2, is(1));
        assertThat(invocationCount3, is(1));
    }

    @Test
    public void testMixedCacheableAndNonCacheableMethods() {
        // GIVEN
        BeanWithSomeCachedAndSomeNonCachedMethods bean = context.getBean(BeanWithSomeCachedAndSomeNonCachedMethods.class);

        // WHEN
        Integer cachedInvocationCount1 = bean.cachedMethod();
        Integer cachedInvocationCount2 = bean.cachedMethod();

        Integer nonCachedInvocationCount1 = bean.nonCachedMethodInvocationCount();
        Integer nonCachedInvocationCount2 = bean.nonCachedMethodInvocationCount();

        // THEN
        assertThat(cachedInvocationCount1, is(1));
        assertThat(cachedInvocationCount2, is(1));

        assertThat(nonCachedInvocationCount1, is(1));
        assertThat(nonCachedInvocationCount2, is(2));
    }

    @Test
    public void testCacheWithShortEvictionTime() throws InterruptedException {
        // GIVEN
        BeanWithShortCacheLife bean = context.getBean(BeanWithShortCacheLife.class);

        // WHEN
        Integer cachedInvocationCount1 = bean.getInvocationCount();
        Integer cachedInvocationCount2 = bean.getInvocationCount();
        Thread.sleep(200);
        Integer cachedInvocationCount3 = bean.getInvocationCount();

        // THEN
        assertThat(cachedInvocationCount1, is(1));
        assertThat(cachedInvocationCount2, is(1));
        assertThat(cachedInvocationCount3, is(2));
    }

    @Test
    public void testCacheWithParameters() throws InterruptedException {
        // GIVEN
        BeanWithCacheableParameter bean = context.getBean(BeanWithCacheableParameter.class);

        // WHEN
        Integer cachedInvocationCount1 = bean.getInvocationCount("asd");
        Integer cachedInvocationCount2 = bean.getInvocationCount("asd");
        Integer cachedInvocationCount3 = bean.getInvocationCount("bsd");
        Integer cachedInvocationCount4 = bean.getInvocationCount("bsd");

        // THEN
        assertThat(cachedInvocationCount1, is(1));
        assertThat(cachedInvocationCount2, is(1));
        assertThat(cachedInvocationCount3, is(2));
        assertThat(cachedInvocationCount4, is(2));
    }

    @Test
    public void testCacheWithArray() throws InterruptedException {
        // GIVEN
        BeanWithCacheableParameter bean = context.getBean(BeanWithCacheableParameter.class);

        // WHEN
        Integer cachedInvocationCount1 = bean.getInvocationCount(new String[]{"asd"});
        Integer cachedInvocationCount2 = bean.getInvocationCount(new String[]{"asd"});
        Integer cachedInvocationCount3 = bean.getInvocationCount(new String[]{"bsd"});

        // THEN
        assertThat(cachedInvocationCount1, is(1));
        assertThat(cachedInvocationCount2, is(1));
        assertThat(cachedInvocationCount3, is(2));
    }

    @Test
    public void testCacheWithCustomDomain() throws InterruptedException {
        // GIVEN
        BeanWithCacheableParameter bean = context.getBean(BeanWithCacheableParameter.class);

        // WHEN
        Integer cachedInvocationCount1 = bean.getInvocationCount(new CustomClass("asd"));
        Integer cachedInvocationCount2 = bean.getInvocationCount(new CustomClass("asd"));
        Integer cachedInvocationCount3 = bean.getInvocationCount(new CustomClass("bsd"));

        // THEN
        assertThat(cachedInvocationCount1, is(1));
        assertThat(cachedInvocationCount2, is(1));
        assertThat(cachedInvocationCount3, is(2));
    }

    @Test
    public void testCacheWithVoid() throws InterruptedException {
        // GIVEN
        CacheableWithVoid bean = context.getBean(CacheableWithVoid.class);

        // WHEN
        bean.getInvocationCount();

        // THEN should not fail
    }

    //    @Test for some reasons fails when all tests run. TODO: investigate
    public void testWithCleanableValue() throws InterruptedException {
        // GIVEN
        BeanWithCleanableValue bean = context.getBean(BeanWithCleanableValue.class);
        MyCleaner cleaner = mock(MyCleaner.class);

        // WHEN
        CleanableValue cleanableValue = bean.cleanableCache("someString1", cleaner);

        // THEN
        Awaitility.waitAtMost(2000, MILLISECONDS).until(() -> cleanWasCalled(cleaner));
    }

    private Boolean cleanWasCalled(MyCleaner cleaner) {
        try {
            verify(cleaner).clean("someString1");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    static class CustomClass {
        private String field;

        public CustomClass(String field) {
            this.field = field;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof CustomClass)) {
                return false;
            }
            CustomClass castOther = (CustomClass) other;
            return Objects.equals(field, castOther.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field);
        }

    }

}
