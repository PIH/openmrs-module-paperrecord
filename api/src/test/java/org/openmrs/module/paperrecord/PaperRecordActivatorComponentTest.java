package org.openmrs.module.paperrecord;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 *
 */
@Ignore("Test doesn't actually do anything yet")
public class PaperRecordActivatorComponentTest extends BaseModuleContextSensitiveTest {

    @Test
    public void testStartup() throws Exception {
        PaperRecordActivator activator = new PaperRecordActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.willStart();
        activator.started();
    }
}
