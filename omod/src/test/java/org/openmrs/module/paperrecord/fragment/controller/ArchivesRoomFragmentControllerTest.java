package org.openmrs.module.paperrecord.fragment.controller;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.paperrecord.PaperRecord;
import org.openmrs.module.paperrecord.PaperRecordRequest;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArchivesRoomFragmentControllerTest {

    private ArchivesRoomFragmentController controller;

    private UiUtils ui;

    private PaperRecordService paperRecordService;

    private EmrApiProperties emrApiProperties;

    private UiSessionContext uiSessionContext;

    private User authenicatedUser;

    private Person authenicatedUserPerson;

    private Location sessionLocation;

    private PatientIdentifierType patientIdentifierType = new PatientIdentifierType();

    private PatientIdentifierType paperRecordIdentifierType = new PatientIdentifierType();


    @Before
    public void setup() {

        controller = new ArchivesRoomFragmentController();
        ui = new TestUiUtils();

        paperRecordService = mock(PaperRecordService.class);
        emrApiProperties = mock(EmrApiProperties.class);
        uiSessionContext = mock(UiSessionContext.class);

        authenicatedUserPerson = new Person();
        authenicatedUser = new User();
        authenicatedUser.setPerson(authenicatedUserPerson);

        sessionLocation = new Location();

        when(uiSessionContext.getCurrentUser()).thenReturn(authenicatedUser);
        when(uiSessionContext.getSessionLocation()).thenReturn(sessionLocation);
    }

    @Test
    public void testControllerShouldReturnFailureResultIfNoMatchingRequestFound() throws Exception {

        when(paperRecordService.getAssignedPaperRecordRequestByIdentifier(eq("123"))).thenReturn(null);
        when(paperRecordService.getSentPaperRecordRequestByIdentifier(eq("123"))).thenReturn(null);

        FragmentActionResult result = controller.markPaperRecordRequestAsSent("123", paperRecordService, ui);

        assertThat(result, instanceOf(FailureResult.class));
        FailureResult failureResult = (FailureResult) result;
        assertThat(((FailureResult) result).getSingleError(), containsString("123"));

    }

    @Test
    public void testControllerShouldReturnFailureResultIfRecordAlreadySent() throws Exception {

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("123");

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(identifier);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setPaperRecord(paperRecord);

        when(paperRecordService.getAssignedPaperRecordRequestByIdentifier(eq("123"))).thenReturn(null);
        when(paperRecordService.getSentPaperRecordRequestByIdentifier(eq("123"))).thenReturn(Collections.singletonList(request));

        FragmentActionResult result = controller.markPaperRecordRequestAsSent("123", paperRecordService, ui);

        assertThat(result, instanceOf(FailureResult.class));
        FailureResult failureResult = (FailureResult) result;
        assertThat(failureResult.getSingleError(), containsString("123"));
        assertThat(failureResult.getSingleError(), containsString(location.getDisplayString()));
    }

    @Test
    public void testControllerShouldMarkRecordAsSent() throws Exception {

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("123");

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(identifier);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setPaperRecord(paperRecord);
        request.setDateCreated(new Date());
        request.updateStatus(PaperRecordRequest.Status.ASSIGNED);

        when(paperRecordService.getPendingPaperRecordRequestByIdentifier(eq("123"))).thenReturn(request);

        FragmentActionResult result = controller.markPaperRecordRequestAsSent("123", paperRecordService, ui);

        verify(paperRecordService).markPaperRecordRequestAsSent(request);
        assertThat(result, instanceOf(SuccessResult.class));
        SuccessResult successResult = (SuccessResult) result;
        assertThat(successResult.getMessage(), containsString("123"));
        assertThat(successResult.getMessage(), containsString("Test location"));
    }

    @Test
    public void testControllerShouldMarkRecordAsReturned() throws Exception {

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("123");

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(identifier);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setPaperRecord(paperRecord);
        request.setDateCreated(new Date());
        request.updateStatus(PaperRecordRequest.Status.SENT);

        when(paperRecordService.getSentPaperRecordRequestByIdentifier(eq("123"))).thenReturn(Collections.singletonList(request));

        FragmentActionResult result = controller.markPaperRecordRequestAsReturned("123", paperRecordService, uiSessionContext, ui);
        assertThat(result, instanceOf(SuccessResult.class));
        SuccessResult successResult = (SuccessResult) result;
        assertThat(successResult.getMessage(), containsString("123"));

    }

    @Test
    public void testControllerShouldMarkRecordAsCancelled() throws Exception {

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("123");

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(identifier);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setPaperRecord(paperRecord);
        request.setDateCreated(new Date());
        request.updateStatus(PaperRecordRequest.Status.OPEN);

        FragmentActionResult result = controller.markPaperRecordRequestAsCancelled(request, paperRecordService, ui);
        verify(paperRecordService).markPaperRecordRequestAsCancelled(request);
        assertThat(result, instanceOf(SuccessResult.class));
    }


    @Test
    public void testControllerShouldReturnOpenRequestsToPull() throws Exception {

        List<PaperRecordRequest> requests = createSamplePullPaperRecordRequestList();

        when(paperRecordService.getOpenPaperRecordRequestsToPull()).thenReturn(requests);
        when(paperRecordService.getMostRecentSentPaperRecordRequest(requests.get(0).getPaperRecord())).thenReturn(createSampleSentRequest());
        when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(patientIdentifierType);

        List<SimpleObject> results = controller.getOpenRecordsToPull(paperRecordService, emrApiProperties, ui);

        assertProperPullResultsList(results);
    }

    @Test
    public void testControllerShouldReturnOpenRequestsToCreate() throws Exception {

        List<PaperRecordRequest> requests = createSampleCreatePaperRecordRequestList();

        when(paperRecordService.getOpenPaperRecordRequestsToCreate()).thenReturn(requests);
        when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(patientIdentifierType);

        List<SimpleObject> results = controller.getOpenRecordsToCreate(paperRecordService, emrApiProperties, ui);

        assertProperCreateResultsList(results);
    }

    @Test
    public void testControllerShouldReturnAssignedRequestsToPull() throws Exception {

        List<PaperRecordRequest> requests = createSamplePullPaperRecordRequestList();

        when(paperRecordService.getAssignedPaperRecordRequestsToPull()).thenReturn(requests);
        when(paperRecordService.getMostRecentSentPaperRecordRequest(requests.get(0).getPaperRecord())).thenReturn(createSampleSentRequest());
        when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(patientIdentifierType);

        List<SimpleObject> results = controller.getAssignedRecordsToPull(paperRecordService, emrApiProperties, ui);

        assertProperPullResultsList(results);
    }

    @Test
    public void testControllerShouldReturnAssignedRequestsToCreate() throws Exception {

        List<PaperRecordRequest> requests = createSampleCreatePaperRecordRequestList();

        when(paperRecordService.getAssignedPaperRecordRequestsToCreate()).thenReturn(requests);
        when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(patientIdentifierType);

        List<SimpleObject> results = controller.getAssignedRecordsToCreate(paperRecordService, emrApiProperties, ui);

        assertProperCreateResultsList(results);
    }

    @Test
    public void testControllerShouldAssignRequests() throws Exception {

        List<PaperRecordRequest> requests = createSamplePullPaperRecordRequestList();

        FragmentActionResult result = controller.assignPullRequests(requests, paperRecordService, uiSessionContext, ui);

        assertThat(result, instanceOf(SuccessResult.class));
        verify(paperRecordService).assignRequests(eq(requests), eq(authenicatedUser.getPerson()), eq(sessionLocation));
    }

    private List<PaperRecordRequest> createSampleCreatePaperRecordRequestList() {

        Patient patient = new Patient();
        PersonName name = new PersonName();
        name.setFamilyName("Jones");
        name.setGivenName("Tom");
        patient.addName(name);

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("987");
        patientIdentifier.setIdentifierType(patientIdentifierType);
        patient.addIdentifier(patientIdentifier);

        PatientIdentifier paperRecordIdentifier = new PatientIdentifier();
        paperRecordIdentifier.setIdentifier("123");
        paperRecordIdentifier.setIdentifierType(paperRecordIdentifierType);
        patient.addIdentifier(paperRecordIdentifier);

        Patient patient2 = new Patient();
        name = new PersonName();
        name.setFamilyName("Wallace");
        name.setGivenName("Mike");
        patient2.addName(name);

        PatientIdentifier patientIdentifier2 = new PatientIdentifier();
        patientIdentifier2.setIdentifier("763");
        patientIdentifier2.setIdentifierType(patientIdentifierType);
        patient2.addIdentifier(patientIdentifier2);

        PatientIdentifier paperRecordIdentifier2 = new PatientIdentifier();
        paperRecordIdentifier2.setIdentifier("ABC");
        paperRecordIdentifier2.setIdentifierType(paperRecordIdentifierType);
        patient2.addIdentifier(paperRecordIdentifier2);

        Location location = new Location();
        location.setName("Test location");

        Location location2 = new Location();
        location2.setName("Another location");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 2, 22, 11, 10);

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(paperRecordIdentifier);
        paperRecord.updateStatus(PaperRecord.Status.PENDING_CREATION);

        PaperRecordRequest request = new PaperRecordRequest();
        request.setId(1);
        request.setPaperRecord(paperRecord);
        request.setRequestLocation(location);
        request.setDateCreated(calendar.getTime());
        request.updateStatus(PaperRecordRequest.Status.OPEN);

        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setPatientIdentifier(paperRecordIdentifier2);
        paperRecord2.updateStatus(PaperRecord.Status.PENDING_CREATION);

        PaperRecordRequest request2 = new PaperRecordRequest();
        request2.setId(2);
        request2.setPaperRecord(paperRecord2);
        request2.setRequestLocation(location2);
        calendar.set(2012,2,22,12, 11);
        request2.setDateCreated(calendar.getTime());
        request2.updateStatus(PaperRecordRequest.Status.ASSIGNED);

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(request);
        requests.add(request2);

        return requests;
    }

    private List<PaperRecordRequest> createSamplePullPaperRecordRequestList() {

        Patient patient = new Patient();
        PersonName name = new PersonName();
        name.setFamilyName("Jones");
        name.setGivenName("Tom");
        patient.addName(name);

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("987");
        patientIdentifier.setIdentifierType(patientIdentifierType);
        patient.addIdentifier(patientIdentifier);

        PatientIdentifier paperRecordIdentifier = new PatientIdentifier();
        paperRecordIdentifier.setIdentifier("123");
        paperRecordIdentifier.setIdentifierType(paperRecordIdentifierType);
        patient.addIdentifier(paperRecordIdentifier);

        Patient patient2 = new Patient();
        name = new PersonName();
        name.setFamilyName("Wallace");
        name.setGivenName("Mike");
        patient2.addName(name);

        PatientIdentifier patientIdentifier2 = new PatientIdentifier();
        patientIdentifier2.setIdentifier("763");
        patientIdentifier2.setIdentifierType(patientIdentifierType);
        patient2.addIdentifier(patientIdentifier2);

        PatientIdentifier paperRecordIdentifier2 = new PatientIdentifier();
        paperRecordIdentifier2.setIdentifier("ABC");
        paperRecordIdentifier2.setIdentifierType(paperRecordIdentifierType);
        patient2.addIdentifier(paperRecordIdentifier2);

        Location location = new Location();
        location.setName("Test location");

        Location location2 = new Location();
        location2.setName("Another location");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 2, 22, 11, 10);

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(paperRecordIdentifier);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);

        PaperRecordRequest request = new PaperRecordRequest();
        request.setId(1);
        request.setPaperRecord(paperRecord);
        request.setRequestLocation(location);
        request.setDateCreated(calendar.getTime());
        request.updateStatus(PaperRecordRequest.Status.OPEN);

        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setPatientIdentifier(paperRecordIdentifier2);
        paperRecord2.updateStatus(PaperRecord.Status.ACTIVE);

        PaperRecordRequest request2 = new PaperRecordRequest();
        request2.setId(2);
        request2.setPaperRecord(paperRecord2);
        request2.setRequestLocation(location2);
        calendar.set(2012,2,22,12, 11);
        request2.setDateCreated(calendar.getTime());
        request2.updateStatus(PaperRecordRequest.Status.ASSIGNED);

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(request);
        requests.add(request2);

        return requests;
    }


    private PaperRecordRequest createSampleSentRequest() {

        Patient patient = new Patient();
        PersonName name = new PersonName();
        name.setFamilyName("Jones");
        name.setGivenName("Tom");
        patient.addName(name);

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("987");
        patientIdentifier.setIdentifierType(patientIdentifierType);
        patient.addIdentifier(patientIdentifier);

        PatientIdentifier paperRecordIdentifier = new PatientIdentifier();
        paperRecordIdentifier.setIdentifier("123");
        paperRecordIdentifier.setIdentifierType(paperRecordIdentifierType);
        patient.addIdentifier(paperRecordIdentifier);

        Location location = new Location();
        location.setName("Previously sent location");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 2, 22, 11, 10);

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(paperRecordIdentifier);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);

        PaperRecordRequest request = new PaperRecordRequest();
        request.setId(3);
        request.setPaperRecord(paperRecord);
        request.setRequestLocation(location);
        request.setDateCreated(calendar.getTime());
        request.updateStatus(PaperRecordRequest.Status.SENT);

        return request;
    }

    private void assertProperCreateResultsList(List<SimpleObject> results) {

        assertThat(results.size(), is(2));

        SimpleObject result = results.get(0);
        assertThat((Integer) result.get("requestId"), is(1));
        assertThat((String) result.get("requestLocation"), is("Test location"));
        assertThat((String) result.get("identifier"), is("123"));
        assertThat((String) result.get("patient"), is("Tom Jones"));
        assertThat((String) result.get("patientIdentifier"), is("987"));
        assertThat((String) result.get("dateCreated"), is("11:10 22/03"));
        assertFalse(result.containsKey("dateLastSent"));
        assertFalse(result.containsKey("locationLastSent"));

        SimpleObject result2 = results.get(1);
        assertThat((Integer) result2.get("requestId"), is(2));
        assertThat((String) result2.get("requestLocation"), is("Another location"));
        assertThat((String) result2.get("identifier"), is("ABC"));
        assertThat((String) result2.get("patient"), is("Mike Wallace"));
        assertThat((String) result2.get("patientIdentifier"), is("763"));
        assertThat((String) result2.get("dateCreated"), is("12:11 22/03"));
        assertFalse(result.containsKey("dateLastSent"));
        assertFalse(result.containsKey("locationLastSent"));

    }

    private void assertProperPullResultsList(List<SimpleObject> results) {

        assertThat(results.size(), is(2));

        SimpleObject result = results.get(0);
        assertThat((Integer) result.get("requestId"), is(1));
        assertThat((String) result.get("requestLocation"), is("Test location"));
        assertThat((String) result.get("identifier"), is("123"));
        assertThat((String) result.get("patient"), is("Tom Jones"));
        assertThat((String) result.get("patientIdentifier"), is("987"));
        assertThat((String) result.get("dateCreated"), is("11:10 22/03"));
        assertThat((String) result.get("locationLastSent"), is("Previously sent location"));
        assertNotNull(result.get("dateLastSent"));

        SimpleObject result2 = results.get(1);
        assertThat((Integer) result2.get("requestId"), is(2));
        assertThat((String) result2.get("requestLocation"), is("Another location"));
       assertThat((String) result2.get("identifier"), is("ABC"));
        assertThat((String) result2.get("patient"), is("Mike Wallace"));
        assertThat((String) result2.get("patientIdentifier"), is("763"));
        assertThat((String) result2.get("dateCreated"), is("12:11 22/03"));
        assertFalse(result2.containsKey("dateLastSent"));
        assertFalse(result2.containsKey("locationLastSent"));

    }
}
