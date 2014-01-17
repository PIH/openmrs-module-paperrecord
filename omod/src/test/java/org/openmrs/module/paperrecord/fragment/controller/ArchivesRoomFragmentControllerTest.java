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
import static org.junit.Assert.assertNull;
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

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setIdentifier("123");

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

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setIdentifier("123");
        request.setDateCreated(new Date());
        request.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_PULL);

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

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setIdentifier("123");
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

        PaperRecordRequest request = new PaperRecordRequest();
        Location location = new Location();
        location.setName("Test location");
        request.setRequestLocation(location);
        request.setIdentifier("123");
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
        when(paperRecordService.getMostRecentSentPaperRecordRequestByPaperRecordIdentifier("123")).thenReturn(createSampleSentRequest());
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
        when(paperRecordService.getMostRecentSentPaperRecordRequestByPaperRecordIdentifier("123")).thenReturn(createSampleSentRequest());
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

        Patient patient2 = new Patient();
        name = new PersonName();
        name.setFamilyName("Wallace");
        name.setGivenName("Mike");
        patient2.addName(name);

        patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("763");
        patientIdentifier.setIdentifierType(patientIdentifierType);
        patient2.addIdentifier(patientIdentifier);

        Location location = new Location();
        location.setName("Test location");

        Location location2 = new Location();
        location2.setName("Another location");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 2, 22, 11, 10);

        PaperRecordRequest request = new PaperRecordRequest();
        request.setId(1);
        request.setRequestLocation(location);
        request.setDateCreated(calendar.getTime());
        request.setPatient(patient);
        request.updateStatus(PaperRecordRequest.Status.OPEN);

        PaperRecordRequest request2 = new PaperRecordRequest();
        request2.setId(2);
        request2.setIdentifier("ABC");
        request2.setRequestLocation(location2);
        calendar.set(2012,2,22,12, 11);
        request2.setDateCreated(calendar.getTime());
        request2.setPatient(patient2);
        request2.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_CREATE);

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

        Patient patient2 = new Patient();
        name = new PersonName();
        name.setFamilyName("Wallace");
        name.setGivenName("Mike");
        patient2.addName(name);

        patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("763");
        patientIdentifier.setIdentifierType(patientIdentifierType);
        patient2.addIdentifier(patientIdentifier);

        Location location = new Location();
        location.setName("Test location");

        Location location2 = new Location();
        location2.setName("Another location");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 2, 22, 11, 10);

        PaperRecordRequest request = new PaperRecordRequest();
        request.setId(1);
        request.setIdentifier("123");
        request.setRequestLocation(location);
        request.setDateCreated(calendar.getTime());
        request.setPatient(patient);
        request.updateStatus(PaperRecordRequest.Status.OPEN);

        PaperRecordRequest request2 = new PaperRecordRequest();
        request2.setId(2);
        request2.setIdentifier("ABC");
        request2.setRequestLocation(location2);
        calendar.set(2012,2,22,12, 11);
        request2.setDateCreated(calendar.getTime());
        request2.setPatient(patient2);
        request2.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_PULL);

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

        Location location = new Location();
        location.setName("Previously sent location");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, 2, 22, 11, 10);

        PaperRecordRequest request = new PaperRecordRequest();
        request.setId(3);
        request.setIdentifier("123");
        request.setRequestLocation(location);
        request.setDateCreated(calendar.getTime());
        request.setPatient(patient);
        request.updateStatus(PaperRecordRequest.Status.SENT);

        return request;
    }

    private void assertProperCreateResultsList(List<SimpleObject> results) {

        assertThat(results.size(), is(2));

        SimpleObject result = results.get(0);
        assertThat((Integer) result.get("requestId"), is(1));
        assertThat((String) result.get("requestLocation"), is("Test location"));
        assertNull(result.get("identifier"));
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
