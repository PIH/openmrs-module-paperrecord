package org.openmrs.module.paperrecord;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CreatePaperRecordSynchronizationTest extends BaseModuleContextSensitiveTest {

    public static final int NUM_THREADS = 25;

    @Autowired
    private PaperRecordService paperRecordService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private LocationService locationService;

    @Before
    public void beforeAllTests() throws Exception {
        executeDataSet("paperRecordTestDataset.xml");
        // we seem to need to do this so that the dataset is avaiable to all the threads we create
        getConnection().commit();
    }


    @Test
    public void shouldNotCreateMultiplePaperRecords() throws Exception {

        // note that I have confirmed that this does fail if run without the synchronized lock around the patient

        // I split the synchronization tests up into two different classes (CreatePaperRecordSynchronizationTest and RequestPaperRecordSynchronizationTest)
        // because for some reason when the tests were in the same class one always seemed to pass, even if the methods weren't within sync blocks

        // sanity check
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        assertThat(paperRecordService.getPaperRecords(patient, medicalRecordLocation).size(), is(0));

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < NUM_THREADS; ++i) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Context.openSession();

                        authenticate();

                        Patient patient = patientService.getPatient(2);
                        Location medicalRecordLocation = locationService.getLocation(1);

                        paperRecordService.createPaperRecord(patient, medicalRecordLocation);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        Context.closeSession();
                    }
                }
            });
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // pass
            }
        }

        // only one paper record should have been created
        assertThat(paperRecordService.getPaperRecords(patient, medicalRecordLocation).size(), is(1));

    }

}
