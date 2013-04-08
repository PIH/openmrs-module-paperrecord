package org.openmrs.module.paperrecord;

import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 *
 */
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
