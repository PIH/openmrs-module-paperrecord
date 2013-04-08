package org.openmrs.module.paperrecord.merge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtServiceImpl;
import org.openmrs.module.emrapi.merge.PatientMergeAction;
import org.openmrs.module.paperrecord.IsExpectedRequest;
import org.openmrs.module.paperrecord.PaperRecordProperties;
import org.openmrs.module.paperrecord.PaperRecordRequest;
import org.openmrs.module.paperrecord.PaperRecordService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.paperrecord.PaperRecordRequest.Status;

/**
 *
 */
public class FixPaperRecordRequestsForMergeTest {

    private AdtServiceImpl service;

    VisitService mockVisitService;
    PaperRecordService mockPaperRecordService;
    EncounterService mockEncounterService;
    ProviderService mockProviderService;
    PatientService mockPatientService;
    EmrApiProperties emrApiProperties;
    PaperRecordProperties paperRecordProperties;

    private Person personForCurrentUser;
    private Provider providerForCurrentUser;

    private PatientIdentifierType paperRecordIdentifierType;


    @Before
    public void setup() throws Exception {
        personForCurrentUser = new Person();
        personForCurrentUser.addName(new PersonName("Current", "User", "Person"));

        User authenticatedUser = new User();
        authenticatedUser.setPerson(personForCurrentUser);

        UserContext userContext = mock(UserContext.class);
        when(userContext.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Context.setUserContext(userContext);

        providerForCurrentUser = new Provider();
        providerForCurrentUser.setPerson(personForCurrentUser);
        mockProviderService = mock(ProviderService.class);
        when(mockProviderService.getProvidersByPerson(personForCurrentUser, false)).thenReturn(Collections.singletonList(providerForCurrentUser));

        mockVisitService = mock(VisitService.class);
        mockEncounterService = mock(EncounterService.class);
        mockPatientService = mock(PatientService.class);
        mockPaperRecordService = mock(PaperRecordService.class);

        paperRecordIdentifierType = new PatientIdentifierType();

        emrApiProperties = mock(EmrApiProperties.class);
        when(emrApiProperties.getVisitExpireHours()).thenReturn(10);

        paperRecordProperties = mock(PaperRecordProperties.class);
        when(paperRecordProperties.getPaperRecordIdentifierType()).thenReturn(paperRecordIdentifierType);

        FixPaperRecordRequestsForMerge fixPaperRecordRequestsForMerge = new FixPaperRecordRequestsForMerge();
        fixPaperRecordRequestsForMerge.setPaperRecordService(mockPaperRecordService);
        fixPaperRecordRequestsForMerge.setPaperRecordProperties(paperRecordProperties);

        service = new AdtServiceImpl();
        service.setPatientService(mockPatientService);
        service.setVisitService(mockVisitService);
        service.setEncounterService(mockEncounterService);
        service.setProviderService(mockProviderService);
        service.setEmrApiProperties(emrApiProperties);
        service.setPatientMergeActions(Arrays.<PatientMergeAction>asList(fixPaperRecordRequestsForMerge));
    }


    @After
    public void tearDown() throws Exception {
        // This test is not context-sensitive, but it may be run between two other context-sensitive tests, and our setting up a
        // mock UserContext breaks things in that case.
        Context.clearUserContext();
    }


    @Test
    public void testThatMergingTwoPatientsWithMedicalRecordIdentifierAtSameLocationMarksPaperRecordsForMerge() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location someLocation = new Location();
        Location anotherLocation = new Location();

        PatientIdentifier preferredIdentifier = new PatientIdentifier("123", paperRecordIdentifierType, someLocation);
        PatientIdentifier anotherPreferredIdentifier = new PatientIdentifier("789", paperRecordIdentifierType, anotherLocation); // this is a "fake out" one
        PatientIdentifier notPreferredIdentifier = new PatientIdentifier("456", paperRecordIdentifierType, someLocation);

        preferred.addIdentifier(anotherPreferredIdentifier);
        preferred.addIdentifier(preferredIdentifier);
        notPreferred.addIdentifier(notPreferredIdentifier);

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordsForMerge(preferredIdentifier, notPreferredIdentifier);

        // make sure a merge request is not created for the record at the other location
        verify(mockPaperRecordService, never()).markPaperRecordsForMerge(anotherPreferredIdentifier, notPreferredIdentifier);
    }

    @Test
    public void mergingPatientsShouldCancelOpenRequestForPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(preferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService).requestPaperRecord(preferred, recordLocation, requestLocation);

    }

    @Test
    public void mergingPatientsShouldCancelAssignedToCreateRequestForPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(preferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService).requestPaperRecord(preferred, recordLocation, requestLocation);
    }

    @Test
    public void mergingPatientsShouldCancelAssignedToPullRequestForPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(preferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService).requestPaperRecord(preferred, recordLocation, requestLocation);
    }

    @Test
    public void mergingPatientsShouldNotCancelSentRequestForPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(preferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.SENT);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService, never()).requestPaperRecord(preferred, recordLocation, requestLocation);

    }

    @Test
    public void mergingPatientsShouldNotCancelReturnedRequestForPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(preferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.RETURNED);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService, never()).requestPaperRecord(preferred, recordLocation, requestLocation);

    }

    @Test
    public void mergingPatientsShouldCancelOpenRequestNotForPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(notPreferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService).requestPaperRecord(preferred, recordLocation, requestLocation);

    }

    @Test
    public void mergingPatientsShouldCancelAssignedToCreateRequestForNotPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(notPreferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService).requestPaperRecord(preferred, recordLocation, requestLocation);
    }

    @Test
    public void mergingPatientsShouldCancelAssignedToPullRequestForNotPreferredPatientAndReissue() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(notPreferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService).requestPaperRecord(preferred, recordLocation, requestLocation);
    }

    @Test
    public void mergingPatientsShouldNotCancelSentRequestForNotPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(notPreferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.SENT);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(paperRecordRequest);
        //verify(mockPaperRecordService, never()).requestPaperRecord(preferred, recordLocation, requestLocation);

    }

    @Test
    public void mergingPatientsShouldNotCancelReturnedRequestSendForNotPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setIdentifier("ABC");
        paperRecordRequest.setPatient(notPreferred);
        paperRecordRequest.setRecordLocation(recordLocation);
        paperRecordRequest.setRequestLocation(requestLocation);
        paperRecordRequest.updateStatus(Status.RETURNED);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(paperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(paperRecordRequest);

        // TODO: if we ever add back in the "reissue" functionality
        //verify(mockPaperRecordService, never()).requestPaperRecord(preferred, recordLocation, requestLocation);
    }

    @Test
    public void mergingPatientsShouldMoveAllPaperRecordRequestsToPreferredPatient() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location recordLocation = new Location();
        Location requestLocation = new Location();

        PaperRecordRequest preferredRequest = new PaperRecordRequest();
        preferredRequest.setIdentifier("ABC");
        preferredRequest.setPatient(preferred);
        preferredRequest.setRecordLocation(recordLocation);
        preferredRequest.setRequestLocation(requestLocation);
        preferredRequest.updateStatus(Status.SENT);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredRequest));

        PaperRecordRequest nonPreferredRequest = new PaperRecordRequest();
        nonPreferredRequest.setIdentifier("DEF");
        nonPreferredRequest.setPatient(notPreferred);
        nonPreferredRequest.setRecordLocation(recordLocation);
        nonPreferredRequest.setRequestLocation(requestLocation);
        nonPreferredRequest.updateStatus(Status.SENT);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(nonPreferredRequest));

        // we are expecting that the non-preferred request is saved, but that the patient on this request is now the preferred patient
        PaperRecordRequest expectedRequestToSave = new PaperRecordRequest();
        expectedRequestToSave.setIdentifier("DEF");
        expectedRequestToSave.setPatient(preferred);
        expectedRequestToSave.setRecordLocation(recordLocation);
        expectedRequestToSave.setRequestLocation(requestLocation);
        expectedRequestToSave.updateStatus(Status.SENT);

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).savePaperRecordRequest(argThat(new IsExpectedRequest(expectedRequestToSave)));
    }

    // TODO: all these tests are to test improved handling of pending paper record requests when merging two patient records
    // TODO: right now we are just keeping it simple and cancelling and reissuing all requests


   /* @Test
    public void mergingAPreferredPatientWithAnOpenPullChartRequestWithANotPreferredPatientWithAssignedCreateChartRequestWillDropCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location someLocation = new Location();

        PatientIdentifier preferredIdentifier =  new PatientIdentifier("ABC1", paperRecordIdentifierType, someLocation);

        preferred.addIdentifier(preferredIdentifier);

        PaperRecordRequest pullPaperRecordRequest = new PaperRecordRequest();
        pullPaperRecordRequest.setIdentifier("ABC1");
        pullPaperRecordRequest.setPatient(preferred);
        pullPaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(pullPaperRecordRequest));

        PaperRecordRequest createPaperRecordRequest = new PaperRecordRequest();
        createPaperRecordRequest.setIdentifier("ABC2");
        createPaperRecordRequest.setPatient(notPreferred);
        createPaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(createPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(createPaperRecordRequest);

    }


    @Test
    public void mergingAPreferredPatientWithAssignedPullChartRequestWithANotPreferredPatientWithAssignedCreateChartRequestWillDropCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location someLocation = new Location();

        PatientIdentifier preferredIdentifier =  new PatientIdentifier("ABC1", paperRecordIdentifierType, someLocation);

        preferred.addIdentifier(preferredIdentifier);

        PaperRecordRequest pullPaperRecordRequest = new PaperRecordRequest();
        pullPaperRecordRequest.setIdentifier("ABC1");
        pullPaperRecordRequest.setPatient(preferred);
        pullPaperRecordRequest.updateStatus(ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(pullPaperRecordRequest));

        PaperRecordRequest createPaperRecordRequest = new PaperRecordRequest();
        createPaperRecordRequest.setIdentifier("ABC2");
        createPaperRecordRequest.setPatient(notPreferred);
        createPaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(createPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(createPaperRecordRequest);

    }


    @Test
    public void mergingANotPreferredPatientWithAssignedPullChartRequestWithAPreferredPatientWithAOpenCreateChartRequestWillDropCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location someLocation = new Location();

        PatientIdentifier preferredIdentifier =  new PatientIdentifier("123", paperRecordIdentifierType, someLocation);

        preferred.addIdentifier(preferredIdentifier);

        PaperRecordRequest pullPaperRecordRequest = new PaperRecordRequest();
        pullPaperRecordRequest.setIdentifier("123");
        pullPaperRecordRequest.setPatient(notPreferred);
        pullPaperRecordRequest.updateStatus(ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(pullPaperRecordRequest));

        PaperRecordRequest createPaperRecordRequest = new PaperRecordRequest();
        createPaperRecordRequest.setPatient(preferred);
        createPaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(createPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(createPaperRecordRequest);

    }

    @Test
    public void mergingANotPreferredPatientWithOpenPullChartRequestWithAPreferredPatientWithAOpenCreateChartRequestWillDropCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location someLocation = new Location();

        PatientIdentifier preferredIdentifier =  new PatientIdentifier("123", paperRecordIdentifierType, someLocation);

        preferred.addIdentifier(preferredIdentifier);

        PaperRecordRequest pullPaperRecordRequest = new PaperRecordRequest();
        pullPaperRecordRequest.setIdentifier("123");
        pullPaperRecordRequest.setPatient(notPreferred);
        pullPaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(pullPaperRecordRequest));

        PaperRecordRequest createPaperRecordRequest = new PaperRecordRequest();
        createPaperRecordRequest.setPatient(preferred);
        createPaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(createPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(createPaperRecordRequest);

    }

    @Test
    public void mergingANotPreferredPatientWithAnAssignedPullChartRequestWithAPreferredPatientWithAnAssignedToCreateChartRequestWillDropCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location someLocation = new Location();

        PatientIdentifier preferredIdentifier =  new PatientIdentifier("ABC1", paperRecordIdentifierType, someLocation);

        preferred.addIdentifier(preferredIdentifier);

        PaperRecordRequest pullPaperRecordRequest = new PaperRecordRequest();
        pullPaperRecordRequest.setIdentifier("ABC1");
        pullPaperRecordRequest.setPatient(notPreferred);
        pullPaperRecordRequest.updateStatus(ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(pullPaperRecordRequest));

        PaperRecordRequest createPaperRecordRequest = new PaperRecordRequest();
        createPaperRecordRequest.setIdentifier("ABC2");
        createPaperRecordRequest.setPatient(preferred);
        createPaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(createPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(createPaperRecordRequest);

    }


    @Test
    public void mergingTwoPatientsWithOpenCreatePaperRecordRequestWillLeaveOnlyOneCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PaperRecordRequest preferredCreatePaperRecordRequest = new PaperRecordRequest();
        preferredCreatePaperRecordRequest.setPatient(preferred);
        preferredCreatePaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredCreatePaperRecordRequest));

        PaperRecordRequest notPreferredCreatePaperRecordRequest = new PaperRecordRequest();
        notPreferredCreatePaperRecordRequest.setPatient(notPreferred);
        notPreferredCreatePaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredCreatePaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(notPreferredCreatePaperRecordRequest);
        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(preferredCreatePaperRecordRequest);
    }

    @Test
    public void mergingPatientWithOpenCreatePaperRecordRequestAndAssignedToCreatePaperRequestCreateWillLeaveOnlyAssignedCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PaperRecordRequest preferredCreatePaperRecordRequest = new PaperRecordRequest();
        preferredCreatePaperRecordRequest.setPatient(preferred);
        preferredCreatePaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredCreatePaperRecordRequest));

        PaperRecordRequest notPreferredCreatePaperRecordRequest = new PaperRecordRequest();
        notPreferredCreatePaperRecordRequest.setIdentifier("ABC1");
        notPreferredCreatePaperRecordRequest.setPatient(notPreferred);
        notPreferredCreatePaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredCreatePaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(preferredCreatePaperRecordRequest);
        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(notPreferredCreatePaperRecordRequest);
    }


    @Test
    public void mergingTwoPatientsWithOpenPullPaperRecordRequestWillLeaveOnlyOnePullRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PaperRecordRequest preferredPullPaperRecordRequest = new PaperRecordRequest();
        preferredPullPaperRecordRequest.setIdentifier("ABC1");
        preferredPullPaperRecordRequest.setPatient(preferred);
        preferredPullPaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredPullPaperRecordRequest));

        PaperRecordRequest notPreferredPullPaperRecordRequest = new PaperRecordRequest();
        notPreferredPullPaperRecordRequest.setIdentifier("ABC2");
        notPreferredPullPaperRecordRequest.setPatient(notPreferred);
        notPreferredPullPaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredPullPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(notPreferredPullPaperRecordRequest);
        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(preferredPullPaperRecordRequest);
    }

    @Test
    public void mergingPatientsWithOpenPullPaperRecordRequestWithPatientWithAssignedPullRequestWillLeaveOnlyAssignedPullRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PaperRecordRequest preferredPullPaperRecordRequest = new PaperRecordRequest();
        preferredPullPaperRecordRequest.setIdentifier("ABC1");
        preferredPullPaperRecordRequest.setPatient(preferred);
        preferredPullPaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredPullPaperRecordRequest));

        PaperRecordRequest notPreferredPullPaperRecordRequest = new PaperRecordRequest();
        notPreferredPullPaperRecordRequest.setIdentifier("ABC2");
        notPreferredPullPaperRecordRequest.setPatient(notPreferred);
        notPreferredPullPaperRecordRequest.updateStatus(ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredPullPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(preferredPullPaperRecordRequest);
        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(notPreferredPullPaperRecordRequest);
    }


    @Test
    public void mergingTwoPatientsWithAssignedToPullPaperRecordRequestWillLeaveOnlyOnePullRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PaperRecordRequest preferredPullPaperRecordRequest = new PaperRecordRequest();
        preferredPullPaperRecordRequest.setIdentifier("ABC1");
        preferredPullPaperRecordRequest.setPatient(preferred);
        preferredPullPaperRecordRequest.updateStatus(ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredPullPaperRecordRequest));

        PaperRecordRequest notPreferredPullPaperRecordRequest = new PaperRecordRequest();
        notPreferredPullPaperRecordRequest.setIdentifier("ABC2");
        notPreferredPullPaperRecordRequest.setPatient(notPreferred);
        notPreferredPullPaperRecordRequest.updateStatus(ASSIGNED_TO_PULL);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredPullPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(notPreferredPullPaperRecordRequest);
        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(preferredPullPaperRecordRequest);
    }

    @Test
    public void mergingTwoPatientsWithAssignedToCreatePaperRecordRequestWillLeaveOnlyOneCreateRequest() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PaperRecordRequest preferredPullPaperRecordRequest = new PaperRecordRequest();
        preferredPullPaperRecordRequest.setIdentifier("ABC1");
        preferredPullPaperRecordRequest.setPatient(preferred);
        preferredPullPaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredPullPaperRecordRequest));

        PaperRecordRequest notPreferredPullPaperRecordRequest = new PaperRecordRequest();
        notPreferredPullPaperRecordRequest.setIdentifier("ABC2");
        notPreferredPullPaperRecordRequest.setPatient(notPreferred);
        notPreferredPullPaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredPullPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordRequestAsCancelled(notPreferredPullPaperRecordRequest);
        verify(mockPaperRecordService, never()).markPaperRecordRequestAsCancelled(preferredPullPaperRecordRequest);
    }

    @Test
    public void mergingAPatientWithOpenCreatePaperRecordRequestWithAPatientWithPaperRecordIdentifierWillMoveRequestToPull() {

        Patient preferred = new Patient();
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifierType(paperRecordIdentifierType);
        identifier.setIdentifier("ABC");
        preferred.addIdentifier(identifier);
        Patient notPreferred = new Patient();

        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.<PaperRecordRequest>emptyList());

        PaperRecordRequest notPreferredCreatePaperRecordRequest = new PaperRecordRequest();
        notPreferredCreatePaperRecordRequest.setPatient(notPreferred);
        notPreferredCreatePaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredCreatePaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        IsExpectedRequest expectedRequest = new IsExpectedRequest(preferred, OPEN, "ABC");
        verify(mockPaperRecordService, times(1)).savePaperRecordRequest(argThat(expectedRequest));
    }

    @Test
    public void mergingAPatientWithOpenCreatePaperRecordRequestWithANotPreferredPatientWithPaperRecordIdentifierWillMoveRequestToPull() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifierType(paperRecordIdentifierType);
        identifier.setIdentifier("ABC");
        notPreferred.addIdentifier(identifier);

        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.<PaperRecordRequest>emptyList());

        PaperRecordRequest preferredCreatePaperRecordRequest = new PaperRecordRequest();
        preferredCreatePaperRecordRequest.setPatient(preferred);
        preferredCreatePaperRecordRequest.updateStatus(OPEN);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredCreatePaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        preferredCreatePaperRecordRequest.setIdentifier("ABC");
        preferredCreatePaperRecordRequest.updateStatus(ASSIGNED_TO_PULL);

        IsExpectedRequest expectedRequest = new IsExpectedRequest(preferred, ASSIGNED_TO_PULL, "ABC");
        verify(mockPaperRecordService, times(1)).savePaperRecordRequest(argThat(expectedRequest));
    }

    @Test
    public void mergingTwoPatientsWithPaperRecordRequestsShouldNotChangeDossierNumberOfRequest() {
        Location someLocation = new Location();

        Patient preferred = new Patient();
        PatientIdentifier preferredIdentifier = new PatientIdentifier("ABC1", paperRecordIdentifierType, someLocation);
        preferred.addIdentifier(preferredIdentifier);

        Patient notPreferred = new Patient();
        PatientIdentifier notPreferredIdentifier = new PatientIdentifier("ABC2", paperRecordIdentifierType, someLocation);
        notPreferred.addIdentifier(notPreferredIdentifier);

        PaperRecordRequest preferredPaperRecordRequest = new PaperRecordRequest();
        preferredPaperRecordRequest.setPatient(preferred);
        preferredPaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        preferredPaperRecordRequest.setIdentifier("ABC1");
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(preferred)).thenReturn(Collections.singletonList(preferredPaperRecordRequest));

        PaperRecordRequest notPreferredPaperRecordRequest = new PaperRecordRequest();
        notPreferredPaperRecordRequest.setPatient(preferred);
        notPreferredPaperRecordRequest.setIdentifier("ABC2");
        notPreferredPaperRecordRequest.updateStatus(ASSIGNED_TO_CREATE);
        when(mockPaperRecordService.getPaperRecordRequestsByPatient(notPreferred)).thenReturn(Collections.singletonList(notPreferredPaperRecordRequest));

        service.mergePatients(preferred, notPreferred);

        IsExpectedRequest expectedNotPreferredRequest = new IsExpectedRequest(preferred,
            ASSIGNED_TO_CREATE, "ABC2");
        verify(mockPaperRecordService, times(1)).savePaperRecordRequest(argThat(expectedNotPreferredRequest));

        verify(mockPaperRecordService).markPaperRecordsForMerge(preferredIdentifier, notPreferredIdentifier);
    }*/

}
