package org.openmrs.module.paperrecord.merge;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.paperrecord.PaperRecordRequest;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class FixPaperRecordRequestsForMergeComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private AdtService service;

    @Autowired
    private PaperRecordService paperRecordService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PatientService patientService;

    @Before
    public void before() throws Exception {
        executeDataSet("retrospectiveCheckinComponentTestDataset.xml");
    }


    @Test
    public void shouldCancelPendingPaperRecordRequestsAfterMerge() {
        Location paperRecordLocation = locationService.getLocation(1);
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

        assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(0));

        List<PaperRecordRequest> paperRecordRequestsPreferred = paperRecordService.getPaperRecordRequestsByPatient(
                preferredPatient);
        assertThat(paperRecordRequestsPreferred.size(), is(2));

        for (PaperRecordRequest request : paperRecordRequestsPreferred) {
            assertThat(request.getStatus(), is(PaperRecordRequest.Status.CANCELLED));
        }

        assertThat(paperRecordService.getPaperRecordRequestsByPatient(notPreferredPatient).size(), is(0));
    }

    /*  @Test
  public void shoulMoveOpenPaperRecordRequestsToCreateToOpenRequestToPullAfterMerge() {
      PatientService patientService = Context.getPatientService();
      Location paperRecordLocation = locationService.getLocation(1);
      Location anotherLocation = locationService.getLocation(3);

      assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(0));

      Patient preferredPatient = patientService.getPatient(2);
      Patient notPreferredPatient = patientService.getPatient(8);

      // first, create a record request
      paperRecordService.requestPaperRecord(notPreferredPatient, paperRecordLocation, anotherLocation);

      assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(1));

      service.mergePatients(preferredPatient, notPreferredPatient);

      assertThat(paperRecordService.getOpenPaperRecordRequestsToPull().size(), is(1));

      List<PaperRecordRequest> requestList = paperRecordService.getPaperRecordRequestsByPatient(
          preferredPatient);
      assertThat(requestList.size(), is(1));
      assertThat(requestList,
          hasItem(new IsExpectedPaperRecordRequest(PaperRecordRequest.Status.OPEN)));
      assertThat(requestList.get(0).getIdentifier(),
          is(preferredPatient.getPatientIdentifier(emrApiProperties.getPaperRecordIdentifierType()).getIdentifier()));
  }

  @Test
  public void shoulMoveOpenPaperRecordRequestsToCreateToOpenRequestToPullAfterMergeOnNotPreferredWithPaperRecordIdentifier() {
      PatientService patientService = Context.getPatientService();
      Location paperRecordLocation = locationService.getLocation(1);
      Location anotherLocation = locationService.getLocation(3);

      assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(0));

      Patient preferredPatient = patientService.getPatient(8);
      Patient notPreferredPatient = patientService.getPatient(2);

      // first, create a record request
      paperRecordService.requestPaperRecord(preferredPatient, paperRecordLocation, anotherLocation);

      assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(1));

      service.mergePatients(preferredPatient, notPreferredPatient);

      assertThat(paperRecordService.getOpenPaperRecordRequestsToPull().size(), is(1));

      List<PaperRecordRequest> requestList = paperRecordService.getPaperRecordRequestsByPatient(
          preferredPatient);
      assertThat(requestList.size(), is(1));
      assertThat(requestList,
          hasItem(new IsExpectedPaperRecordRequest(PaperRecordRequest.Status.OPEN)));
      assertThat(requestList.get(0).getIdentifier(),
          is(preferredPatient.getPatientIdentifier(emrApiProperties.getPaperRecordIdentifierType()).getIdentifier()));
  }

    private class IsExpectedPaperRecordRequest extends ArgumentMatcher<PaperRecordRequest> {
        private PaperRecordRequest.Status status;

        public IsExpectedPaperRecordRequest(PaperRecordRequest.Status status) {
            this.status = status;
        }

        @Override
        public boolean matches(Object o) {
            PaperRecordRequest actual = (PaperRecordRequest) o;

            try {
                assertThat(actual.getStatus(), is(status));
                return true;
            } catch (AssertionError e) {
                return false;
            }
        }
    }
    */
}
