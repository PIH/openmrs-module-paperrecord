/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.paperrecord;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.printer.Printer;
import org.openmrs.module.emrapi.printer.PrinterService;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.paperrecord.PaperRecordRequest.Status;
import org.openmrs.module.paperrecord.db.PaperRecordMergeRequestDAO;
import org.openmrs.module.paperrecord.db.PaperRecordRequestDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.paperrecord.PaperRecordRequest.ASSIGNED_STATUSES;
import static org.openmrs.module.paperrecord.PaperRecordRequest.PENDING_STATUSES;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class PaperRecordServiceTest {

    private PaperRecordServiceImpl paperRecordService;

    private PaperRecordRequestDAO mockPaperRecordDAO;

    private PaperRecordMergeRequestDAO mockPaperRecordMergeRequestDAO;

    private IdentifierSourceService mockIdentifierSourceService;

    private PatientService mockPatientService;

    private PrinterService mockPrinterService;

    private EmrApiProperties mockEmrApiProperties;

    private PaperRecordProperties mockPaperRecordProperties;

    private PaperRecordLabelTemplate mockPaperRecordLabelTemplate;

    private IdCardLabelTemplate mockIdCardLabelTemplate;

    private User authenticatedUser;

    private PatientIdentifierType paperRecordIdentifierType;

    private PatientIdentifierType primaryIdentifierType;

    @Before
    public void setup() {
        mockStatic(Context.class);

        authenticatedUser = new User();
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);

        mockPaperRecordDAO = mock(PaperRecordRequestDAO.class);
        mockPaperRecordMergeRequestDAO = mock(PaperRecordMergeRequestDAO.class);
        mockIdentifierSourceService = mock(IdentifierSourceService.class);
        mockPatientService = mock(PatientService.class);
        mockPrinterService = mock(PrinterService.class);
        mockEmrApiProperties = mock(EmrApiProperties.class);
        mockPaperRecordProperties = mock(PaperRecordProperties.class);
        mockPaperRecordLabelTemplate = mock(PaperRecordLabelTemplate.class);
        mockIdCardLabelTemplate = mock(IdCardLabelTemplate.class);

        paperRecordIdentifierType = new PatientIdentifierType();
        paperRecordIdentifierType.setId(2);
        when(mockPaperRecordProperties.getPaperRecordIdentifierType()).thenReturn(paperRecordIdentifierType);

        primaryIdentifierType = new PatientIdentifierType();
        primaryIdentifierType.setId(3);
        when(mockEmrApiProperties.getPrimaryIdentifierType()).thenReturn(primaryIdentifierType);

        paperRecordService = new PaperRecordServiceStub(paperRecordIdentifierType);
        paperRecordService.setPaperRecordRequestDAO(mockPaperRecordDAO);
        paperRecordService.setPaperRecordMergeRequestDAO(mockPaperRecordMergeRequestDAO);
        paperRecordService.setIdentifierSourceService(mockIdentifierSourceService);
        paperRecordService.setPatientService(mockPatientService);
        paperRecordService.setPrinterService(mockPrinterService);
        paperRecordService.setEmrApiProperties(mockEmrApiProperties);
        paperRecordService.setPaperRecordProperties(mockPaperRecordProperties);
        paperRecordService.setPaperRecordLabelTemplate(mockPaperRecordLabelTemplate);
        paperRecordService.setIdCardLabelTemplate(mockIdCardLabelTemplate);

        // so we handle the hack in PaperRecordServiceImpl to make sure assignRequestsInternal is transactional
        when(Context.getService(PaperRecordService.class)).thenReturn(paperRecordService);

    }

    @Test
    public void testPaperRecordExistsWithIdentifierShouldReturnTrueIfPaperMedicalRecordExists() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");

        when(mockPatientService.getPatientIdentifiers("ABCZYX", Collections.singletonList(paperRecordIdentifierType),
                Collections.singletonList(medicalRecordLocation), null, null))
                .thenReturn(Collections.singletonList(identifier));

        assertTrue(paperRecordService.paperRecordExistsWithIdentifier("ABCZYX", medicalRecordLocation));
    }

    @Test
    public void testPaperRecordExistsWithIdentifierShouldReturnFalseIfPaperMedicalRecordDoesNotExist() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        when(mockPatientService.getPatientIdentifiers("ABCZYX", Collections.singletonList(paperRecordIdentifierType),
                Collections.singletonList(medicalRecordLocation), null, null))
                .thenReturn(new ArrayList<PatientIdentifier>());

        assertFalse(paperRecordService.paperRecordExistsWithIdentifier("ABCZYX", medicalRecordLocation));
    }

    @Test
    public void testPaperRecordExistsForPatientWithIdentifierShouldReturnTrueIfPaperMedicalRecordExists() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient = new Patient();
        patient.setId(2);

        PatientIdentifier primaryIdentifier = createIdentifier(medicalRecordLocation, "ABC123");
        primaryIdentifier.setIdentifierType(primaryIdentifierType);
        patient.addIdentifier(primaryIdentifier);

        PatientIdentifier paperRecordIdentifier = createIdentifier(medicalRecordLocation, "456123");
        patient.addIdentifier(paperRecordIdentifier);

        when(mockPatientService.getPatients(null, "ABC123", Collections.singletonList(primaryIdentifierType), true)).thenReturn(Collections.singletonList(patient));
        when(mockPatientService.getPatientIdentifiers(null, Collections.singletonList(paperRecordIdentifierType),
                Collections.singletonList(medicalRecordLocation), Collections.singletonList(patient), null)).thenReturn(Collections.singletonList(paperRecordIdentifier));

        assertTrue(paperRecordService.paperRecordExistsForPatientWithIdentifier("ABC123", medicalRecordLocation));
    }

    @Test
    public void testPaperRecordExistsForPatientWithIdentifierShouldReturnFalseIfPaperMedicalRecordDoesntExist() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient = new Patient();
        patient.setId(2);

        PatientIdentifier primaryIdentifier = createIdentifier(medicalRecordLocation, "ABC123");
        primaryIdentifier.setIdentifierType(primaryIdentifierType);
        patient.addIdentifier(primaryIdentifier);

        when(mockPatientService.getPatients(null, "ABC123", Collections.singletonList(primaryIdentifierType), true)).thenReturn(Collections.singletonList(patient));

        assertFalse(paperRecordService.paperRecordExistsForPatientWithIdentifier("ABC123", medicalRecordLocation));
    }

    @Test
    public void testPaperRecordExistsForPatientWithIdentifierShouldNotFailIfNoPatientWithPrimaryIdentifier() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient = new Patient();
        patient.setId(2);

        assertFalse(paperRecordService.paperRecordExistsForPatientWithIdentifier("ABC123", medicalRecordLocation));
    }

    @Test
    public void testRequestPaperRecord() throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        PatientIdentifier identifer = createIdentifier(medicalRecordLocation, "ABCZYX");
        patient.addIdentifier(identifer);

        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);

        PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        expectedRequestMatcher.matches(returnedRequest);
    }

    private Location createMedicalRecordLocation() {
        return createLocation(3, "Mirebalais");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestPaperRecordShouldThrowExceptionIfPatientNull() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Location requestLocation = createLocation(4, "Outpatient Clinic");

        paperRecordService.requestPaperRecord(null, medicalRecordLocation, requestLocation);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestPaperRecordShouldThrowExceptionIfRecordLocationNull() throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Location requestLocation = createLocation(4, "Outpatient Clinic");

        paperRecordService.requestPaperRecord(patient, null, requestLocation);

    }


    @Test(expected = IllegalArgumentException.class)
    public void testRequestPaperRecordShouldThrowExceptionIfRequestLocationNull() throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, null);

    }


    @Test
    public void testRequestPaperRecordForPatientWithMultipleIdentifiersOfSameTypeAtDifferentLocations() throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location otherLocation = createLocation(5, "Cange");
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        PatientIdentifier wrongIdentifer = createIdentifier(otherLocation, "ZYXCBA");
        patient.addIdentifier(wrongIdentifer);

        PatientIdentifier identifer = createIdentifier(medicalRecordLocation, "ABCZYX");
        patient.addIdentifier(identifer);

        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);

        PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        expectedRequestMatcher.matches(returnedRequest);
    }

    @Test
    public void testRequestPaperRecordWhenPatientHasNoValidIdentifier() throws Exception {

        MessageSourceService messageSourceService = mock(MessageSourceService.class);
        when(messageSourceService.getMessage("emr.missingPaperRecordIdentifierCode")).thenReturn("UNKNOWN");
        ((PaperRecordServiceImpl) paperRecordService).setMessageSourceService(messageSourceService);

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();

        Location requestLocation = createLocation(4, "Outpatient Clinic");

        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, null);

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);

        PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        expectedRequestMatcher.matches(returnedRequest);
    }

    @Test
    public void testAssignRequestsWithoutIdentifiers() throws Exception {

        when(mockIdCardLabelTemplate.generateLabel(any(Patient.class))).thenReturn("some data");

        Person assignTo = new Person(15);

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(buildPaperRecordRequestWithoutIdentifier());
        requests.add(buildPaperRecordRequestWithoutIdentifier());
        requests.add(buildPaperRecordRequestWithoutIdentifier());

        paperRecordService.assignRequests(requests, assignTo, null);

        verify(mockPaperRecordDAO, times(3)).saveOrUpdate(argThat(new IsAssignedTo(assignTo, PaperRecordRequest.Status.ASSIGNED_TO_CREATE)));
    }

    @Test
    public void testAssignRequestsWithIdentifiersShouldReturnErrors() throws Exception {
        Person assignTo = new Person(15);

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(buildPaperRecordRequestWithIdentifier());
        requests.add(buildPaperRecordRequestWithIdentifier());
        requests.add(buildPaperRecordRequestWithIdentifier());

        Map<String, List<String>> response = paperRecordService.assignRequests(requests, assignTo, null);

        assertThat(response.get("success").size(), is(3));

        verify(mockPaperRecordDAO, times(3)).saveOrUpdate(argThat(new IsAssignedTo(assignTo, PaperRecordRequest.Status.ASSIGNED_TO_PULL)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testAssignRequestsShouldFailIfRequestsNull() throws Exception {

        Person assignTo = new Person(15);
        paperRecordService.assignRequests(null, assignTo, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssignRequestsShouldFailIfAssigneeNull() throws Exception {

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(buildPaperRecordRequestWithoutIdentifier());
        requests.add(buildPaperRecordRequestWithoutIdentifier());
        requests.add(buildPaperRecordRequestWithoutIdentifier());

        paperRecordService.assignRequests(requests, null, null);
    }

    @Test
    public void testAssignRequestsShouldReturnErrorIfPatientHasValidIdentifierEvenIfRequestDoesNot() throws Exception {
        Person assignTo = new Person(15);

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(buildPaperRecordRequestWithoutIdentifier());

        // add an identifier to this patient
        Patient patient = requests.get(0).getPatient();
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("ABC");
        patientIdentifier.setIdentifierType(paperRecordIdentifierType);
        patientIdentifier.setLocation(requests.get(0).getRecordLocation());
        patient.addIdentifier(patientIdentifier);

        Map<String, List<String>> response = paperRecordService.assignRequests(requests, assignTo, null);

        assertThat(response.get("error").size(), is(1));

        verify(mockPaperRecordDAO, never()).saveOrUpdate(argThat(new IsAssignedTo(assignTo, PaperRecordRequest.Status.ASSIGNED_TO_PULL, "ABC")));
    }

    @Test
    public void whenPatientDoesNotHaveAnPaperMedicalRecordIdentifierShouldCreateAnPaperMedicalRecordNumberAndAssignToHim() {
        String paperMedicalRecordNumberAsExpected = "A000001";
        when(mockIdentifierSourceService.generateIdentifier(paperRecordIdentifierType, "generating a new dossier number")).thenReturn(paperMedicalRecordNumberAsExpected);

        Patient patient = new Patient();

        PatientIdentifier identifier = new PatientIdentifier(paperMedicalRecordNumberAsExpected, paperRecordIdentifierType, createMedicalRecordLocation());

        String paperMedicalRecordNumber = paperRecordService.createPaperMedicalRecordNumber(patient, createMedicalRecordLocation()).getIdentifier();

        // cannot compare using one identifier because the equals is not implemented correctly
        verify(mockPatientService).savePatientIdentifier(any(PatientIdentifier.class));

        assertEquals(paperMedicalRecordNumberAsExpected, paperMedicalRecordNumber);
    }


    @Test
    public void whenDuplicateRequestIsMadeNoNewRequestShouldBeGenerated() throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        // generate an existing paper record request
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "");
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                eq(patient), eq(medicalRecordLocation), argThat(new NullString()), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        // the returned request should be the existing request
        PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        expectedRequestMatcher.matches(request);

    }

    @Test
    public void whenDuplicateRequestIsMadeLocationShouldBeUpdated() throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");
        Location newRequestLocation = createLocation(5, "ER");

        // generate an existing paper record request
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABC123");
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());

        // expected request is the same, but with the new location
        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABC123");
        expectedRequest.setId(10);
        expectedRequest.setRequestLocation(newRequestLocation);
        expectedRequest.setDateCreated(new Date());

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                eq(patient), eq(medicalRecordLocation), argThat(new NullString()), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        // the returned request should be the existing request
        PaperRecordRequest returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, newRequestLocation);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        expectedRequestMatcher.matches(request);

    }

    @Test
    public void getAssignedPaperRecordRequestByIdentifierShouldRetrieveRequestByPaperRecordIdentifier() {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        // generate an existing paper record request
        String identifier = "ABC123";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());
        request.updateStatus(Status.ASSIGNED_TO_PULL);

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(ASSIGNED_STATUSES)),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        PaperRecordRequest returnedRequest = paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier);
        expectedRequestMatcher.matches(returnedRequest);

    }

    @Test
    public void getAssignedPaperRecordRequestByIdentifierShouldRetrieveRequestByPatientIdentifier() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        Patient patient = new Patient();
        patient.setId(15);

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifierType(primaryIdentifierType);
        patientIdentifier.setIdentifier("Patient_ID");
        patientIdentifier.setLocation(medicalRecordLocation);
        patient.addIdentifier(patientIdentifier);

        // generate an existing paper record request
        String identifier = "ABC123";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());
        request.updateStatus(Status.ASSIGNED_TO_PULL);

        when(mockPatientService.getPatients(null, "Patient_ID", Collections.singletonList(primaryIdentifierType), true)).thenReturn(Collections.singletonList(patient));
        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(ASSIGNED_STATUSES)),
                eq(patient), argThat(new NullLocation()), argThat(new NullString()), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        PaperRecordRequest returnedRequest = paperRecordService.getAssignedPaperRecordRequestByIdentifier("Patient_ID");
        expectedRequestMatcher.matches(returnedRequest);

    }

    @Test
    public void getAssignedPaperRecordRequestByIdentifierShouldReturnNullIfNoActiveRequestWithThatIdentifier() {
        String identifier = "ABC123";
        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(ASSIGNED_STATUSES)),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(null);
        assertNull(paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier));
    }

    @Test
    public void getAssignedPaperRecordRequestByIdentifierShouldReturnNullIfBlankIdentifierPassed() {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        // generate an existing paper record request
        String identifier = "";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(ASSIGNED_STATUSES)),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));

        PaperRecordRequest returnedRequest = paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier);
        assertNull(returnedRequest);
    }

    @Test(expected = IllegalStateException.class)
    public void getAssignedPaperRecordRequestByIdentifierShouldThrowIllegalStateExceptionIfMultipleActiveRequestsFound() {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        // generate an existing paper record request
        String identifier = "ABC123";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());

        PaperRecordRequest anotherRequest = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(11);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(ASSIGNED_STATUSES)),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean())))
                .thenReturn(Arrays.asList(request, anotherRequest));
        paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier);
    }

    @Test
    public void getSentPaperRecordRequestByIdentifierShouldRetrieveRequestByPaperRecordIdentifier() {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");
        Location newRequestLocation = createLocation(5, "ER");

        // generate an existing paper record request
        String identifier = "ABC123";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());
        request.updateStatus(Status.SENT);

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        List<PaperRecordRequest> returnedRequests = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier);
        expectedRequestMatcher.matches(returnedRequests.get(0));

    }


    @Test
    public void getSentPaperRecordRequestByIdentifierShouldReturnNullIfNoActiveRequestWithThatIdentifier() {
        String identifier = "ABC123";
        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(null);
        assertNull(paperRecordService.getSentPaperRecordRequestByIdentifier(identifier));
    }

    @Test
    public void getSentPaperRecordRequestByIdentifierShouldRetrieveRequestByPatientIdentifier() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        Patient patient = new Patient();
        patient.setId(15);

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifierType(primaryIdentifierType);
        patientIdentifier.setIdentifier("Patient_ID");
        patientIdentifier.setLocation(medicalRecordLocation);
        patient.addIdentifier(patientIdentifier);


        // generate an existing paper record request
        String identifier = "ABC123";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());
        request.updateStatus(Status.SENT);

        when(mockPatientService.getPatients(null, "Patient_ID", Collections.singletonList(primaryIdentifierType), true)).thenReturn(Collections.singletonList(patient));
        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                eq(patient), argThat(new NullLocation()), argThat(new NullString()), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        List<PaperRecordRequest> returnedRequests = paperRecordService.getSentPaperRecordRequestByIdentifier("Patient_ID");
        assertTrue(expectedRequestMatcher.matches(returnedRequests.get(0)));

    }

    @Test
    public void getSentPaperRecordRequestByIdentifierShouldRetrieveRequestByPaperRecordIdentifierShouldReturnEmptyListIfBlankIdentifier() {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");
        Location newRequestLocation = createLocation(5, "ER");

        // generate an existing paper record request
        String identifier = "";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());
        request.updateStatus(Status.SENT);

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        List<PaperRecordRequest> returnedRequests = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier);
        assertThat(returnedRequests.size(), is(0));
    }

    @Test
    public void getMostRecentSentPaperRecordRequestByIdentifierShouldRetrieveMostRecentSentRequest() {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        // generate a few existing paper record request
        String identifier = "ABC123";
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        request.setId(10);
        request.setRequestLocation(requestLocation);
        request.setDateCreated(new Date());
        request.updateStatus(Status.SENT);

        PaperRecordRequest anotherRequest = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        anotherRequest.setId(11);
        anotherRequest.setRequestLocation(requestLocation);
        anotherRequest.setDateCreated(new Date());
        anotherRequest.updateStatus(Status.SENT);

        PaperRecordRequest yetAnotherRequest = createPaperRecordRequest(patient, medicalRecordLocation, identifier);
        yetAnotherRequest.setId(12);
        yetAnotherRequest.setRequestLocation(requestLocation);
        yetAnotherRequest.setDateCreated(new Date());
        yetAnotherRequest.updateStatus(Status.SENT);

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean())))
                .thenReturn(Arrays.asList(request, anotherRequest, yetAnotherRequest));

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(yetAnotherRequest);

        PaperRecordRequest returnedRequest = paperRecordService.getMostRecentSentPaperRecordRequestByIdentifier(identifier);
        expectedRequestMatcher.matches(returnedRequest);
    }

    @Test
    public void getMostRecentSentPaperRecordRequestByIdentifierShouldReturnNullIfNoSentRequests() {

        String identifier = "ABC123";

        when(mockPaperRecordDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), argThat(new NullLocation()), eq(identifier), argThat(new NullBoolean())))
                .thenReturn(null);


        assertNull(paperRecordService.getMostRecentSentPaperRecordRequestByIdentifier(identifier));
    }

    @Test
    public void testMarkRequestAsSentShouldMarkRequestAsSent() throws Exception {
        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();

        PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
        patient.addIdentifier(identifier);

        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
        request.setDateCreated(new Date());

        paperRecordService.markPaperRecordRequestAsSent(request);

        assertThat(request.getStatus(), is(PaperRecordRequest.Status.SENT));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
    }

    @Test
    public void shouldMarkRequestAsCancelled() throws Exception {
        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();

        PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
        patient.addIdentifier(identifier);

        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
        request.setDateCreated(new Date());

        paperRecordService.markPaperRecordRequestAsCancelled(request);

        assertThat(request.getStatus(), is(Status.CANCELLED));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
    }

    @Test
    public void testMarkPaperRecordRequestsAsReturnedShouldMarkPaperRecordRequestsAsReturned()
            throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Patient anotherPatient = new Patient();
        anotherPatient.setId(16);

        Location medicalRecordLocation = createMedicalRecordLocation();

        PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
        patient.addIdentifier(identifier);

        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
        request.setDateCreated(new Date());
        request.updateStatus(Status.SENT);

        paperRecordService.markPaperRecordRequestAsReturned(request);

        assertThat(request.getStatus(), is(Status.RETURNED));
    }


    @Test
    public void testMarkPapersRecordForMergeShouldCreatePaperRecordMergeRequest() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);

        IsExpectedMergeRequest expectedMergeRequestMatcher = new IsExpectedMergeRequest(createExpectedMergeRequest(patient1,
                patient2, identifier1.getIdentifier(), identifier2.getIdentifier(), medicalRecordLocation));

        verify(mockPaperRecordMergeRequestDAO).saveOrUpdate(argThat(expectedMergeRequestMatcher));
        verify(mockPatientService).voidPatientIdentifier(identifier2, "voided during paper record merge");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkPaperRecordsForMergeShouldFailIfLocationsDiffer() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location anotherLocation = new Location();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PatientIdentifier identifier2 = createIdentifier(anotherLocation, "EFG");

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkPaperRecordsForMergeShouldFailIfFirstIdentifierNotProperType() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();
        PatientIdentifierType someOtherIdentifierType = new PatientIdentifierType();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        identifier1.setIdentifierType(someOtherIdentifierType);
        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMarkPaperRecordsForMergeShouldFailIfSecondIdentifierNotProperType() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();
        PatientIdentifierType someOtherIdentifierType = new PatientIdentifierType();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        identifier2.setIdentifierType(someOtherIdentifierType);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        paperRecordService.markPaperRecordsForMerge(identifier1, identifier2);
    }

    @Test
    public void testMarkPaperRecordsAsMergedShouldMarkPaperRecordsAsMerged() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
        mergeRequest.setPreferredPatient(patient1);
        mergeRequest.setNotPreferredPatient(patient2);
        mergeRequest.setPreferredIdentifier(identifier1.getIdentifier());
        mergeRequest.setNotPreferredIdentifier(identifier2.getIdentifier());
        mergeRequest.setDateCreated(new Date());
        paperRecordService.markPaperRecordsAsMerged(mergeRequest);

        assertThat(mergeRequest.getStatus(), is(PaperRecordMergeRequest.Status.MERGED));
        IsExpectedMergeRequest expectedMergeRequestMatcher = new IsExpectedMergeRequest(mergeRequest);
        verify(mockPaperRecordMergeRequestDAO).saveOrUpdate(argThat(expectedMergeRequestMatcher));
    }

    @Test
    public void testMarkPaperRecordsAsMergedShouldMergeExistingPendingPaperRecordRequests() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location location1 = new Location(1);
        Location location2 = new Location(2);

        Patient patient = new Patient();
        Patient notPreferredPatient = new Patient();

        // create the merge request
        PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
        mergeRequest.setPreferredPatient(patient);
        mergeRequest.setNotPreferredPatient(notPreferredPatient);
        mergeRequest.setPreferredIdentifier("ABC");
        mergeRequest.setNotPreferredIdentifier("XYZ");
        mergeRequest.setRecordLocation(medicalRecordLocation);
        mergeRequest.setDateCreated(new Date());

        // create some existing paper record requests (all should be for the preferred patient at this point)
        PaperRecordRequest request1 = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                location1, Status.OPEN);
        request1.setDateCreated(new Date());
        PaperRecordRequest request2 = createPaperRecordRequest(patient, medicalRecordLocation, "ABC",
                location2, Status.ASSIGNED_TO_PULL);
        request2.setDateCreated(new Date());

        when(mockPaperRecordDAO.findPaperRecordRequests(PENDING_STATUSES, null, medicalRecordLocation, "XYZ", null))
                .thenReturn(Collections.singletonList(request1));

        when(mockPaperRecordDAO.findPaperRecordRequests(PENDING_STATUSES, null, medicalRecordLocation, "ABC", null))
                .thenReturn(Collections.singletonList(request2));

        paperRecordService.markPaperRecordsAsMerged(mergeRequest);

        // the "winning" request should be the request with the preferred identifier, but should be updated with
        // the more recent request location
        PaperRecordRequest expectedWinningRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABC",
                location2, Status.ASSIGNED_TO_PULL);
        expectedWinningRequest.setId(request2.getId());

        PaperRecordRequest expectedLosingRequest = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                location1, Status.CANCELLED);
        expectedLosingRequest.setId(request1.getId());

        ArgumentCaptor<PaperRecordRequest> paperRecordRequestArgumentCaptor = ArgumentCaptor.forClass(PaperRecordRequest.class);
        verify(mockPaperRecordDAO, times(2)).saveOrUpdate(paperRecordRequestArgumentCaptor.capture());

        IsExpectedRequest expectedWinningRequestMatcher = new IsExpectedRequest(expectedWinningRequest);
        assertThat(paperRecordRequestArgumentCaptor.getAllValues().get(0), Is.is(expectedWinningRequestMatcher));

        IsExpectedRequest expectedLosingRequestMatcher = new IsExpectedRequest(expectedLosingRequest);
        assertThat(paperRecordRequestArgumentCaptor.getAllValues().get(1), Is.is(expectedLosingRequestMatcher));
    }

    @Test
    public void testMarkPaperRecordsAsMergedShouldUpdatePendingRecordRequestForNotPreferredRecord() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location location1 = new Location(1);

        Patient patient = new Patient();
        Patient notPreferredPatient = new Patient();

        // create the merge request
        PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
        mergeRequest.setPreferredPatient(patient);
        mergeRequest.setNotPreferredPatient(notPreferredPatient);
        mergeRequest.setPreferredIdentifier("ABC");
        mergeRequest.setNotPreferredIdentifier("XYZ");
        mergeRequest.setRecordLocation(medicalRecordLocation);
        mergeRequest.setDateCreated(new Date());

        // create some existing paper record request for the "non-preferred" identifier
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                location1, Status.OPEN);
        request.setId(1);
        request.setDateCreated(new Date());

        when(mockPaperRecordDAO.findPaperRecordRequests(PENDING_STATUSES, null, medicalRecordLocation, "XYZ", null))
                .thenReturn(Collections.singletonList(request));

        paperRecordService.markPaperRecordsAsMerged(mergeRequest);

        // the request should be updated with the preferred identifier
        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABC",
                location1, Status.OPEN);
        expectedRequest.setId(request.getId());

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
    }

    @Test
    public void testMarkPaperRecordsShouldMarkAnyNotPreferredRecordRequestsInSentStateAsReturned() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location location1 = new Location(1);

        Patient patient = new Patient();
        Patient notPreferredPatient = new Patient();

        // create the merge request
        PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
        mergeRequest.setPreferredPatient(patient);
        mergeRequest.setNotPreferredPatient(notPreferredPatient);
        mergeRequest.setPreferredIdentifier("ABC");
        mergeRequest.setNotPreferredIdentifier("XYZ");
        mergeRequest.setRecordLocation(medicalRecordLocation);
        mergeRequest.setDateCreated(new Date());

        // create some existing paper record request for the "non-preferred" identifier
        PaperRecordRequest request = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                location1, Status.SENT);
        request.setId(1);
        request.setDateCreated(new Date());

        when(mockPaperRecordDAO.findPaperRecordRequests(Collections.singletonList(Status.SENT),
                null, medicalRecordLocation, "XYZ", null)).thenReturn(Collections.singletonList(request));

        paperRecordService.markPaperRecordsAsMerged(mergeRequest);

        // the request should be marked as returned with the preferred identifier
        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "XYZ",
                location1, Status.RETURNED);
        expectedRequest.setId(request.getId());

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);
        verify(mockPaperRecordDAO).saveOrUpdate(argThat(expectedRequestMatcher));
    }

    @Test
    public void testPrintPaperRecordLabelShouldPrintSingleLabel() throws Exception {

        Location location = new Location(1);
        Patient patient = new Patient(1);

        when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");

        PaperRecordRequest request = new PaperRecordRequest();
        request.setPatient(patient);
        request.setIdentifier("ABC");

        paperRecordService.printPaperRecordLabel(request, location);

        verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8");

    }

    @Test
    public void testPrintPaperRecordLabelsShouldPrintThreeLabelIfCountSetToThree() throws Exception {

        Location location = new Location(1);
        Patient patient = new Patient(1);

        when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");

        PaperRecordRequest request = new PaperRecordRequest();
        request.setPatient(patient);
        request.setIdentifier("ABC");

        paperRecordService.printPaperRecordLabels(request, location, 3);

        verify(mockPrinterService).printViaSocket("data\nlines\ndata\n" +
                "lines\ndata\nlines\n", Printer.Type.LABEL, location, "UTF-8");

    }

    @Test
    public void testPrintPaperRecordLabelByPatientShouldPrintSingleLabel() throws Exception {

        Patient patient = new Patient(1);
        Location location = new Location(1);

        PatientIdentifier paperRecordIdentifier = new PatientIdentifier();
        paperRecordIdentifier.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier.setIdentifier("ABC");
        paperRecordIdentifier.setLocation(location);
        patient.addIdentifier(paperRecordIdentifier);

        when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");

        paperRecordService.printPaperRecordLabels(patient, location, 1);

        verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8");
    }

    @Test
    public void testPrintIdLabelByPatientShouldPrintSingleLabel() throws Exception {

        Patient patient = new Patient(1);
        Location location = new Location(1);

        PatientIdentifier paperRecordIdentifier = new PatientIdentifier();
        paperRecordIdentifier.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier.setIdentifier("ABC");
        paperRecordIdentifier.setLocation(location);
        patient.addIdentifier(paperRecordIdentifier);

        when(mockIdCardLabelTemplate.generateLabel(patient)).thenReturn("data\nlines\n");
        when(mockIdCardLabelTemplate.getEncoding()).thenReturn("UTF-8");

        paperRecordService.printIdCardLabel(patient, location);

        verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8");
    }

    @Test
    public void testExpirePullRequestsShouldCancelPullRequestsBeforeSpecifiedDate() throws Exception {

        Calendar cal = Calendar.getInstance();
        cal.set(2012, 1, 22);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 40);
        Date beforeExpireDate = cal.getTime();

        cal.set(Calendar.MINUTE, 50);

        Date afterExpireDate = cal.getTime();

        cal.set(Calendar.MINUTE, 45);
        Date expireDate = cal.getTime();

        Patient patient1 = new Patient(1);
        Patient patient2 = new Patient(2);
        Patient patient3 = new Patient(3);
        Patient patient4 = new Patient(4);
        Location requestLocation = new Location(1);
        Location recordLocation = new Location(1);

        PaperRecordRequest request1 = createPaperRecordRequest(patient1, recordLocation, "ABC", requestLocation,
                Status.ASSIGNED_TO_PULL);
        request1.setDateCreated(beforeExpireDate);

        PaperRecordRequest request2 = createPaperRecordRequest(patient2, recordLocation, "DEF", requestLocation,
                Status.ASSIGNED_TO_PULL);
        request2.setDateCreated(afterExpireDate);

        PaperRecordRequest request3 = createPaperRecordRequest(patient3, recordLocation, "GHI", requestLocation,
                Status.OPEN);
        request3.setDateCreated(beforeExpireDate);

        PaperRecordRequest request4 = createPaperRecordRequest(patient4, recordLocation, "JKL", requestLocation,
                Status.OPEN);
        request4.setDateCreated(afterExpireDate);

        when(mockPaperRecordDAO.findPaperRecordRequests(Collections.singletonList(Status.OPEN), null,
                null, null, true)).thenReturn(Arrays.asList(request1, request2));

        when(mockPaperRecordDAO.findPaperRecordRequests(Collections.singletonList(Status.ASSIGNED_TO_PULL), null,
                null, null, null)).thenReturn(Arrays.asList(request3, request4));

        paperRecordService.expirePendingPullRequests(expireDate);

        verify(mockPaperRecordDAO).saveOrUpdate(request1);
        verify(mockPaperRecordDAO).saveOrUpdate(request3);

        verify(mockPaperRecordDAO, never()).saveOrUpdate(request2);
        verify(mockPaperRecordDAO, never()).saveOrUpdate(request4);

        assertThat(request1.getStatus(), is(Status.CANCELLED));
        assertThat(request3.getStatus(), is(Status.CANCELLED));

        assertThat(request2.getStatus(), is(Status.ASSIGNED_TO_PULL));
        assertThat(request4.getStatus(), is(Status.OPEN));
    }

    @Test
    public void testExpirePullRequestsShouldoNotCancelCreateRequests() throws Exception {

        Calendar cal = Calendar.getInstance();
        cal.set(2012, 1, 22);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 40);
        Date beforeExpireDate = cal.getTime();

        cal.set(Calendar.MINUTE, 45);
        Date expireDate = cal.getTime();

        Patient patient1 = new Patient(1);
        Patient patient2 = new Patient(2);
        Location requestLocation = new Location(1);
        Location recordLocation = new Location(1);

        PaperRecordRequest request1 = createPaperRecordRequest(patient1, recordLocation, "ABC", requestLocation,
                Status.ASSIGNED_TO_CREATE);
        request1.setDateCreated(beforeExpireDate);

        PaperRecordRequest request2 = createPaperRecordRequest(patient2, recordLocation, null, requestLocation,
                Status.OPEN);
        request2.setDateCreated(beforeExpireDate);

        when(mockPaperRecordDAO.findPaperRecordRequests(Collections.singletonList(Status.ASSIGNED_TO_CREATE), null,
                null, null, null)).thenReturn(Collections.singletonList(request1));

        when(mockPaperRecordDAO.findPaperRecordRequests(Collections.singletonList(Status.OPEN), null,
                null, null, false)).thenReturn(Collections.singletonList(request2));

        paperRecordService.expirePendingPullRequests(expireDate);

        verify(mockPaperRecordDAO, never()).saveOrUpdate(request1);
        verify(mockPaperRecordDAO, never()).saveOrUpdate(request2);

        assertThat(request1.getStatus(), is(Status.ASSIGNED_TO_CREATE));
        assertThat(request2.getStatus(), is(Status.OPEN));
    }

    @Test
    public void createPaperMedicalRecordNumber_shouldSkipIdentifiersAlreadyInUse() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "A00001");

        when(mockPatientService.getPatientIdentifiers("A00001", Collections.singletonList(paperRecordIdentifierType),
                Collections.singletonList(medicalRecordLocation), null, null))
                .thenReturn(Collections.singletonList(identifier));

        when(mockIdentifierSourceService.generateIdentifier(paperRecordIdentifierType, "generating a new dossier number")).thenReturn("A00001", "A00002");

        PatientIdentifier paperMedicalRecordIdentifier = paperRecordService.createPaperMedicalRecordNumber(new Patient(), medicalRecordLocation);

        assertThat(paperMedicalRecordIdentifier.getIdentifier(), is("A00002"));
    }

    private PatientIdentifier createIdentifier(Location medicalRecordLocation, String identifier) {
        PatientIdentifier identifer = new PatientIdentifier();
        identifer.setIdentifier(identifier);
        identifer.setIdentifierType(paperRecordIdentifierType);
        identifer.setLocation(medicalRecordLocation);
        return identifer;
    }

    private Location createLocation(int locationId, String locationName) {
        Location requestLocation = new Location();
        requestLocation.setId(locationId);
        requestLocation.setName(locationName);
        return requestLocation;
    }

    private PaperRecordRequest createPaperRecordRequest(Patient patient, Location medicalRecordLocation, String identifier,
                                                        Location requestLocation, Status status) {
        PaperRecordRequest expectedRequest = new PaperRecordRequest();
        expectedRequest.setAssignee(null);
        expectedRequest.setCreator(authenticatedUser);
        expectedRequest.setIdentifier(identifier);
        expectedRequest.setRequestLocation(requestLocation);
        expectedRequest.setRecordLocation(medicalRecordLocation);
        expectedRequest.setPatient(patient);
        expectedRequest.updateStatus(status);
        return expectedRequest;
    }


    private PaperRecordRequest createPaperRecordRequest(Patient patient, Location medicalRecordLocation, String identifier) {
        return createPaperRecordRequest(patient, medicalRecordLocation, identifier, null, Status.OPEN);
    }

    private PaperRecordMergeRequest createExpectedMergeRequest(Patient preferredPatient, Patient notPreferredPatient,
                                                               String preferredIdentifier, String notPreferredIdentifier,
                                                               Location recordLocation) {
        PaperRecordMergeRequest expectedMergeRequest = new PaperRecordMergeRequest();
        expectedMergeRequest.setPreferredPatient(preferredPatient);
        expectedMergeRequest.setNotPreferredPatient(notPreferredPatient);
        expectedMergeRequest.setPreferredIdentifier(preferredIdentifier);
        expectedMergeRequest.setNotPreferredIdentifier(notPreferredIdentifier);
        expectedMergeRequest.setStatus(PaperRecordMergeRequest.Status.OPEN);
        expectedMergeRequest.setCreator(authenticatedUser);
        expectedMergeRequest.setRecordLocation(recordLocation);
        return expectedMergeRequest;
    }

    private PaperRecordRequest buildPaperRecordRequestWithoutIdentifier() {
        Patient patient = new Patient(1);
        Location location = new Location(1);
        PaperRecordRequest request = new PaperRecordRequest();
        request.setPatient(patient);
        request.updateStatus(PaperRecordRequest.Status.OPEN);
        request.setRecordLocation(location);
        return request;
    }

    private PaperRecordRequest buildPaperRecordRequestWithIdentifier() {
        Patient patient = new Patient(1);
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifierType(paperRecordIdentifierType);
        patientIdentifier.setIdentifier("ABC");
        patient.addIdentifier(patientIdentifier);
        Location location = new Location(1);
        PaperRecordRequest request = new PaperRecordRequest();
        request.setPatient(patient);
        request.updateStatus(PaperRecordRequest.Status.OPEN);
        request.setRecordLocation(location);
        request.setIdentifier("ABC");
        return request;
    }


    private class PaperRecordServiceStub extends PaperRecordServiceImpl {

        private PatientIdentifierType paperRecordIdentifierType;

        public PaperRecordServiceStub(PatientIdentifierType paperRecordIdentifierType) {
            this.paperRecordIdentifierType = paperRecordIdentifierType;
        }

        @Override
        protected Location getMedicalRecordLocationAssociatedWith(Location location) {
            return location;
        }

    }

    private class IsExpectedMergeRequest extends ArgumentMatcher<PaperRecordMergeRequest> {

        private PaperRecordMergeRequest expectedRequest;

        public IsExpectedMergeRequest(PaperRecordMergeRequest expectedRequest) {
            this.expectedRequest = expectedRequest;
        }

        @Override
        public boolean matches(Object o) {

            PaperRecordMergeRequest actualRequest = (PaperRecordMergeRequest) o;

            assertThat(actualRequest.getId(), is(expectedRequest.getId()));
            assertThat(actualRequest.getPreferredPatient(), is(expectedRequest.getPreferredPatient()));
            assertThat(actualRequest.getNotPreferredPatient(), is(expectedRequest.getNotPreferredPatient()));
            assertThat(actualRequest.getPreferredIdentifier(), is(expectedRequest.getPreferredIdentifier()));
            assertThat(actualRequest.getNotPreferredIdentifier(), is(expectedRequest.getNotPreferredIdentifier()));
            assertThat(actualRequest.getStatus(), is(expectedRequest.getStatus()));
            assertThat(actualRequest.getRecordLocation(), is(expectedRequest.getRecordLocation()));
            assertThat(actualRequest.getCreator(), is(expectedRequest.getCreator()));
            assertNotNull(actualRequest.getDateCreated());

            return true;
        }
    }

    private class IsAssignedTo extends ArgumentMatcher<PaperRecordRequest> {

        private Person shouldBeAssignedTo;

        private PaperRecordRequest.Status assignmentStatus;

        private String identifier;

        public IsAssignedTo(Person shouldBeAssignedTo, PaperRecordRequest.Status assignmentStatus) {
            this.shouldBeAssignedTo = shouldBeAssignedTo;
            this.assignmentStatus = assignmentStatus;
        }


        public IsAssignedTo(Person shouldBeAssignedTo, PaperRecordRequest.Status assignmentStatus, String identifier) {
            this.shouldBeAssignedTo = shouldBeAssignedTo;
            this.assignmentStatus = assignmentStatus;
            this.identifier = identifier;
        }

        @Override
        public boolean matches(Object o) {
            PaperRecordRequest request = (PaperRecordRequest) o;
            assertThat(request.getStatus(), is(assignmentStatus));
            assertThat(request.getAssignee(), is(shouldBeAssignedTo));

            if (identifier != null) {
                assertThat(request.getIdentifier(), is(identifier));
            }

            return true;
        }
    }

    private class NullBoolean extends ArgumentMatcher<Boolean> {
        public boolean matches(Object o) {
            return o == null ? true : false;
        }
    }

    private class NullLocation extends ArgumentMatcher<Location> {
        public boolean matches(Object o) {
            return o == null ? true : false;
        }
    }

    private class NullString extends ArgumentMatcher<String> {
        public boolean matches(Object o) {
            return o == null ? true : false;
        }
    }

    private class NullPatient extends ArgumentMatcher<Patient> {
        public boolean matches(Object o) {
            return o == null ? true : false;
        }
    }


    private class StatusListOf extends ArgumentMatcher<List<PaperRecordRequest.Status>> {

        private List<PaperRecordRequest.Status> expectedStatusList;

        public StatusListOf(List<PaperRecordRequest.Status> expectedStatusList) {
            this.expectedStatusList = expectedStatusList;
        }

        @Override
        public boolean matches(Object o) {
            List<PaperRecordRequest.Status> statusList = (List<PaperRecordRequest.Status>) o;

            if (statusList.size() != expectedStatusList.size()) {
                return false;
            }

            if (statusList.containsAll(expectedStatusList)) {
                return true;
            } else {
                return false;
            }

        }

    }
}
