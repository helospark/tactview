package com.helospark.tactview.core.decoder.framecache;

import static java.lang.Integer.MIN_VALUE;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;

// Global variable goes against the architectural conventions, however this is 
// expected to be very widely used even in non-beans, so in this case
// practicality comes before architecture
@Component
@Order(value = MIN_VALUE)
public class GlobalMemoryManagerAccessor {
    public static MemoryManager memoryManager;

    public GlobalMemoryManagerAccessor(MemoryManager memoryManager) {
        GlobalMemoryManagerAccessor.memoryManager = memoryManager;
    }
}
