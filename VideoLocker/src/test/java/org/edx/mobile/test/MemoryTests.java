package org.openedu.www.test;

import tw.openedu.www.util.MemoryUtil;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

public class MemoryTests extends BaseTestCase {

    @Test
    public void testAvailable() throws Exception {
        long bytes = MemoryUtil.getAvailableExternalMemory(RuntimeEnvironment
                .application);
        print(
                "available = "
                        + MemoryUtil.format(RuntimeEnvironment
                                .application, bytes));
        
        bytes = MemoryUtil.getAvailableInternalMemory(RuntimeEnvironment
                .application);
        print(
                "available = "
                        + MemoryUtil.format(RuntimeEnvironment
                                .application, bytes));
    }
}
