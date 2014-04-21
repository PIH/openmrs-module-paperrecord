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
        activator.started();
    }

    @Test
    public void testShutdown() throws Exception {
        PaperRecordActivator activator = new PaperRecordActivator();
        activator.started();
        activator.stopped();
    }
}
