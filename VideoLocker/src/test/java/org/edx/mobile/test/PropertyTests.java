package org.openedu.www.test;

import tw.openedu.www.util.PropertyUtil;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

public class PropertyTests extends BaseTestCase {

    @Test
    public void testGetDisplayVersionName() throws Exception {
        String name = PropertyUtil.getManifestVersionName(RuntimeEnvironment.application);
        assertTrue("failed to read versionName, found=" + name,
                name != null && !name.isEmpty());
    }
}
