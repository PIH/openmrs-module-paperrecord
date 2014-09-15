package org.openmrs.module.paperrecord.merge;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.paperrecord.PaperRecordActivator;
import org.openmrs.module.paperrecord.PaperRecordProperties;
import org.openmrs.module.paperrecord.PaperRecordRequest;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class FixPaperRecordsForMergeComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private AdtService service;

    @Autowired
    private PaperRecordService paperRecordService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PaperRecordProperties paperRecordProperties;

    private IdentifierSourceService mockIdentifierSourceService;

    private Location paperRecordLocation;

    @Before
    public void before() throws Exception {
        executeDataSet("retrospectiveCheckinComponentTestDataset.xml");

        // stub out the identifier service
        mockIdentifierSourceService = mock(IdentifierSourceService.class);
        paperRecordService.setIdentifierSourceService(mockIdentifierSourceService);
        paperRecordLocation = locationService.getLocation(1);

        when(mockIdentifierSourceService.generateIdentifier(paperRecordProperties.getPaperRecordIdentifierType(), paperRecordLocation, "generating a new paper record identifier number"))
                .thenReturn("A00001", "A00002", "A00003");

        // paper record merge action is wired in the activator
        new PaperRecordActivator().started();
    }


    @Test
    public void shouldCancelPendingPaperRecordRequestsAfterMerge() {
        Location someLocation = locationService.getLocation(2);
        Location anotherLocation = locationService.getLocation(3);

        assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(0));

        Patient preferredPatient = patientService.getPatient(7);
        Patient notPreferredPatient = patientService.getPatient(8);

        // first, create a couple record requests
        paperRecordService.requestPaperRecord(preferredPatient, paperRecordLocation, someLocation);
        paperRecordService.requestPaperRecord(notPreferredPatient, paperRecordLocation, anotherLocation);

        assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(2));

        service.mergePatients(preferredPatient, notPreferredPatient);

        // open requests should be cancelled
        assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(0));

        List<PaperRecordRequest> paperRecordRequestsPreferred = paperRecordService.getPaperRecordRequestsByPatient(
                preferredPatient);
        assertThat(paperRecordRequestsPreferred.size(), is(1));
        assertThat(paperRecordRequestsPreferred.get(0).getStatus(), is(PaperRecordRequest.Status.CANCELLED));

        List<PaperRecordRequest> paperRecordRequestsNotPreferred = paperRecordService.getPaperRecordRequestsByPatient(
                preferredPatient);
        assertThat(paperRecordRequestsNotPreferred.size(), is(1));
        assertThat(paperRecordRequestsNotPreferred.get(0).getStatus(), is(PaperRecordRequest.Status.CANCELLED));
    }


}
