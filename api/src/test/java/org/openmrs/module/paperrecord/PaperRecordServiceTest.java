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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.paperrecord.PaperRecordRequest.Status;
import org.openmrs.module.paperrecord.db.PaperRecordDAO;
import org.openmrs.module.paperrecord.db.PaperRecordMergeRequestDAO;
import org.openmrs.module.paperrecord.db.PaperRecordRequestDAO;
import org.openmrs.module.paperrecord.template.IdCardLabelTemplate;
import org.openmrs.module.paperrecord.template.PaperFormLabelTemplate;
import org.openmrs.module.paperrecord.template.PaperRecordLabelTemplate;
import org.openmrs.module.printer.Printer;
import org.openmrs.module.printer.PrinterService;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.paperrecord.PaperRecordRequest.PENDING_STATUSES;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class PaperRecordServiceTest {

    private PaperRecordServiceImpl paperRecordService;

    private PaperRecordDAO mockPaperRecordDAO;

    private PaperRecordRequestDAO mockPaperRecordRequestDAO;

    private PaperRecordMergeRequestDAO mockPaperRecordMergeRequestDAO;

    private IdentifierSourceService mockIdentifierSourceService;

    private PatientService mockPatientService;

    private PrinterService mockPrinterService;

    private EmrApiProperties mockEmrApiProperties;

    private PaperRecordProperties mockPaperRecordProperties;

    private PaperRecordLabelTemplate mockPaperRecordLabelTemplate;

    private PaperFormLabelTemplate mockPaperFormLabelTemplate;

    private IdCardLabelTemplate mockIdCardLabelTemplate;

    private User authenticatedUser;

    private PatientIdentifierType paperRecordIdentifierType;

    private PatientIdentifierType primaryIdentifierType;

    @Before
    public void setup() {
        mockStatic(Context.class);

        authenticatedUser = new User();
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);

        mockPaperRecordDAO = mock(PaperRecordDAO.class);
        mockPaperRecordRequestDAO = mock(PaperRecordRequestDAO.class);
        mockPaperRecordMergeRequestDAO = mock(PaperRecordMergeRequestDAO.class);
        mockIdentifierSourceService = mock(IdentifierSourceService.class);
        mockPatientService = mock(PatientService.class);
        mockPrinterService = mock(PrinterService.class);
        mockEmrApiProperties = mock(EmrApiProperties.class);
        mockPaperRecordProperties = mock(PaperRecordProperties.class);
        mockPaperRecordLabelTemplate = mock(PaperRecordLabelTemplate.class);
        mockPaperFormLabelTemplate = mock(PaperFormLabelTemplate.class);
        mockIdCardLabelTemplate = mock(IdCardLabelTemplate.class);

        paperRecordIdentifierType = new PatientIdentifierType();
        paperRecordIdentifierType.setId(2);
        when(mockPaperRecordProperties.getPaperRecordIdentifierType()).thenReturn(paperRecordIdentifierType);

        primaryIdentifierType = new PatientIdentifierType();
        primaryIdentifierType.setId(3);
        when(mockEmrApiProperties.getPrimaryIdentifierType()).thenReturn(primaryIdentifierType);

        paperRecordService = new PaperRecordServiceStub(paperRecordIdentifierType);
        paperRecordService.setPaperRecordDAO(mockPaperRecordDAO);
        paperRecordService.setPaperRecordRequestDAO(mockPaperRecordRequestDAO);
        paperRecordService.setPaperRecordMergeRequestDAO(mockPaperRecordMergeRequestDAO);
        paperRecordService.setIdentifierSourceService(mockIdentifierSourceService);
        paperRecordService.setPatientService(mockPatientService);
        paperRecordService.setPrinterService(mockPrinterService);
        paperRecordService.setEmrApiProperties(mockEmrApiProperties);
        paperRecordService.setPaperRecordProperties(mockPaperRecordProperties);
        paperRecordService.setPaperRecordLabelTemplate(mockPaperRecordLabelTemplate);
        paperRecordService.setPaperFormLabelTemplate(mockPaperFormLabelTemplate);
        paperRecordService.setIdCardLabelTemplate(mockIdCardLabelTemplate);

        // so we handle the hack in PaperRecordServiceImpl to make sure assignRequestsInternal is transactional
        when(Context.getService(PaperRecordService.class)).thenReturn(paperRecordService);

    }

    @Test
    public void testPaperRecordExistsWithIdentifierShouldReturnTrueIfPaperMedicalRecordExists() {
        Location medicalRecordLocation = createMedicalRecordLocation();
        PaperRecord paperRecord = new PaperRecord();
        when(mockPaperRecordDAO.findPaperRecord("ABCZYX", medicalRecordLocation)).thenReturn(paperRecord);
        assertTrue(paperRecordService.paperRecordExistsWithIdentifier("ABCZYX", medicalRecordLocation));
    }

    @Test
    public void testPaperRecordExistsWithIdentifierShouldReturnFalseIfPaperMedicalRecordDoesNotExist() {
        Location medicalRecordLocation = createMedicalRecordLocation();
        when(mockPaperRecordDAO.findPaperRecord("ABCZYX", medicalRecordLocation)).thenReturn(null);
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
        PaperRecord paperRecord = new PaperRecord();
        when(mockPaperRecordDAO.findPaperRecords(patient, medicalRecordLocation)).thenReturn(Collections.singletonList(paperRecord));
        assertTrue(paperRecordService.paperRecordExistsForPatientWithPrimaryIdentifier("ABC123", medicalRecordLocation));
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
        when(mockPaperRecordDAO.findPaperRecords(patient, medicalRecordLocation)).thenReturn(null);
        assertFalse(paperRecordService.paperRecordExistsForPatientWithPrimaryIdentifier("ABC123", medicalRecordLocation));
    }

    @Test
    public void testPaperRecordExistsForPatientWithIdentifierShouldNotFailIfNoPatientWithPrimaryIdentifier() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient = new Patient();
        patient.setId(2);

        assertFalse(paperRecordService.paperRecordExistsForPatientWithPrimaryIdentifier("ABC123", medicalRecordLocation));
    }

    @Test
    public void testRequestPaperRecord() throws Exception {

        Patient patient = new Patient();
        patient.setId(15);

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location requestLocation = createLocation(4, "Outpatient Clinic");

        PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "ABCZYX");
        patient.addIdentifier(identifier);

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setRecordLocation(medicalRecordLocation);
        paperRecord.setPatientIdentifier(identifier);
        when(mockPaperRecordDAO.findPaperRecords(patient, medicalRecordLocation)).thenReturn(Collections.singletonList(paperRecord));

        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);

        List<PaperRecordRequest> returnedRequests = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordRequestDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        assertThat(returnedRequests.size(), is(1));
        expectedRequestMatcher.matches(returnedRequests.get(0));
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

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(identifer);
        paperRecord.setRecordLocation(medicalRecordLocation);
        when(mockPaperRecordDAO.findPaperRecords(patient, medicalRecordLocation)).thenReturn(Collections.singletonList(paperRecord));

        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "ABCZYX");
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);

        List<PaperRecordRequest> returnedRequests = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordRequestDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        assertThat(returnedRequests.size(), is(1));
        expectedRequestMatcher.matches(returnedRequests.get(0));
    }

    @Test
    public void testRequestPaperRecordWhenPatientHasNoPaperRecordIdentifier() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();

        String paperMedicalRecordNumberAsExpected = "A000001";
        when(mockIdentifierSourceService.generateIdentifier(paperRecordIdentifierType, medicalRecordLocation, "generating a new paper record identifier number")).thenReturn(paperMedicalRecordNumberAsExpected);

        Patient patient = new Patient();
        patient.setId(15);

        Location requestLocation = createLocation(4, "Outpatient Clinic");

        PaperRecordRequest expectedRequest = createPaperRecordRequest(patient, medicalRecordLocation, "A000001");

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(expectedRequest);

        List<PaperRecordRequest> returnedRequests = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordRequestDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        assertThat(returnedRequests.size(), is(1));
        expectedRequestMatcher.matches(returnedRequests.get(0));
    }

    @Test
    public void testAssignRequest() throws Exception {

        Person assignTo = new Person(15);
        Patient patient = new Patient(1);
        Location location = new Location(1);

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setPatient(patient);
        identifier.setLocation(location);
        identifier.setIdentifierType(paperRecordIdentifierType);
        identifier.setIdentifier("ABC");
        patient.addIdentifier(identifier);

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(createPaperRecordRequest(patient, location, "ABC"));
        requests.add(createPaperRecordRequest(patient, location, "ABC"));
        requests.add(createPaperRecordRequest(patient, location, "ABC"));

        Map<String, List<String>> response = paperRecordService.assignRequests(requests, assignTo, null);

        assertThat(response.get("success").size(), is(3));

        verify(mockPaperRecordRequestDAO, times(3)).saveOrUpdate(argThat(new IsAssignedTo(assignTo, Status.ASSIGNED)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssignRequestsShouldFailIfRequestsNull() throws Exception {

        Person assignTo = new Person(15);
        paperRecordService.assignRequests(null, assignTo, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssignRequestsShouldFailIfAssigneeNull() throws Exception {

        Patient patient = new Patient(1);
        Location location = new Location(1);

        List<PaperRecordRequest> requests = new ArrayList<PaperRecordRequest>();
        requests.add(createPaperRecordRequest(patient, location));
        requests.add(createPaperRecordRequest(patient, location));
        requests.add(createPaperRecordRequest(patient, location));

        paperRecordService.assignRequests(requests, null, null);
    }

    @Test
    public void whenPatientDoesNotHaveAnPaperMedicalRecordIdentifierShouldCreateAnPaperMedicalRecordNumberAndAssignToHim() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        String paperMedicalRecordNumberAsExpected = "A000001";
        when(mockIdentifierSourceService.generateIdentifier(paperRecordIdentifierType, medicalRecordLocation, "generating a new paper record identifier number")).thenReturn(paperMedicalRecordNumberAsExpected);

        Patient patient = new Patient();

        //PatientIdentifier identifier = new PatientIdentifier(paperMedicalRecordNumberAsExpected, paperRecordIdentifierType, createMedicalRecordLocation());

        String paperMedicalRecordNumber = paperRecordService.createPaperRecord(patient, medicalRecordLocation).getPatientIdentifier().getIdentifier();

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

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                eq(patient), eq(medicalRecordLocation), argThat(new NullString()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        // the returned request should be the existing request
        List<PaperRecordRequest> returnedRequest = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        verify(mockPaperRecordRequestDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        assertThat(returnedRequest.size(), is(1));
        expectedRequestMatcher.matches(returnedRequest.get(0));

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

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                eq(patient), eq(medicalRecordLocation), argThat(new NullString()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        // the returned request should be the existing request
        List<PaperRecordRequest> returnedRequests = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, newRequestLocation);
        verify(mockPaperRecordRequestDAO).saveOrUpdate(argThat(expectedRequestMatcher));
        expectedRequestMatcher.matches(returnedRequests.get(0));

    }

    @Test
    public void getPendingPaperRecordRequestByIdentifierShouldRetrieveRequestByPaperRecordIdentifier() {

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
        request.updateStatus(Status.OPEN);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        PaperRecordRequest returnedRequest = paperRecordService.getPendingPaperRecordRequestByIdentifier(identifier, medicalRecordLocation);
        expectedRequestMatcher.matches(returnedRequest);

    }

    @Test
    public void getPendingPaperRecordRequestByIdentifierShouldRetrieveRequestByPatientIdentifier() {

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
        request.updateStatus(Status.OPEN);

        when(mockPatientService.getPatients(null, "Patient_ID", Collections.singletonList(primaryIdentifierType), true)).thenReturn(Collections.singletonList(patient));
        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                eq(patient), eq(medicalRecordLocation), argThat(new NullString()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        PaperRecordRequest returnedRequest = paperRecordService.getPendingPaperRecordRequestByIdentifier("Patient_ID", medicalRecordLocation);
        expectedRequestMatcher.matches(returnedRequest);
    }

    @Test
    public void getPendingPaperRecordRequestByIdentifierShouldReturnNullIfNoActiveRequestWithThatIdentifier() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        String identifier = "ABC123";
        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(PENDING_STATUSES)),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(null);
        assertNull(paperRecordService.getPendingPaperRecordRequestByIdentifier(identifier, medicalRecordLocation));
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
        request.updateStatus(Status.ASSIGNED);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.ASSIGNED))),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        PaperRecordRequest returnedRequest = paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier, medicalRecordLocation);
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
        request.updateStatus(Status.ASSIGNED);

        when(mockPatientService.getPatients(null, "Patient_ID", Collections.singletonList(primaryIdentifierType), true)).thenReturn(Collections.singletonList(patient));
        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.ASSIGNED))),
                eq(patient), eq(medicalRecordLocation), argThat(new NullString()))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        PaperRecordRequest returnedRequest = paperRecordService.getAssignedPaperRecordRequestByIdentifier("Patient_ID", medicalRecordLocation);
        expectedRequestMatcher.matches(returnedRequest);

    }

    @Test
    public void getAssignedPaperRecordRequestByIdentifierShouldReturnNullIfNoActiveRequestWithThatIdentifier() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        String identifier = "ABC123";
        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.ASSIGNED))),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(null);
        assertNull(paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier, medicalRecordLocation));
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

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.ASSIGNED))),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(Collections.singletonList(request));

        PaperRecordRequest returnedRequest = paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier, medicalRecordLocation);
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

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.ASSIGNED))),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier)))
                .thenReturn(Arrays.asList(request, anotherRequest));
        paperRecordService.getAssignedPaperRecordRequestByIdentifier(identifier, medicalRecordLocation);
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

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        List<PaperRecordRequest> returnedRequests = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier, medicalRecordLocation);
        expectedRequestMatcher.matches(returnedRequests.get(0));

    }


    @Test
    public void getSentPaperRecordRequestByIdentifierShouldReturnNullIfNoActiveRequestWithThatIdentifier() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        String identifier = "ABC123";
        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(null);
        assertNull(paperRecordService.getSentPaperRecordRequestByIdentifier(identifier, medicalRecordLocation));
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
        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                eq(patient), eq(medicalRecordLocation), argThat(new NullString()))).thenReturn(Collections.singletonList(request));

        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        List<PaperRecordRequest> returnedRequests = paperRecordService.getSentPaperRecordRequestByIdentifier("Patient_ID", medicalRecordLocation);
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

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(argThat(new StatusListOf(Collections.singletonList(Status.SENT))),
                argThat(new NullPatient()), eq(medicalRecordLocation), eq(identifier))).thenReturn(Collections.singletonList(request));
        IsExpectedRequest expectedRequestMatcher = new IsExpectedRequest(request);

        List<PaperRecordRequest> returnedRequests = paperRecordService.getSentPaperRecordRequestByIdentifier(identifier, medicalRecordLocation);
        assertThat(returnedRequests.size(), is(0));
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
        verify(mockPaperRecordRequestDAO).saveOrUpdate(argThat(expectedRequestMatcher));
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
        verify(mockPaperRecordRequestDAO).saveOrUpdate(argThat(expectedRequestMatcher));
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
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        IsExpectedMergeRequest expectedMergeRequestMatcher = new IsExpectedMergeRequest(createExpectedMergeRequest(paperRecord1, paperRecord2));

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
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(anotherLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(anotherLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

    }

    @Test
    public void testMarkPaperRecordsAsMergedShouldMarkPaperRecordsAsMerged() throws Exception {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordMergeRequest mergeRequest = new PaperRecordMergeRequest();
        mergeRequest.setPreferredPaperRecord(paperRecord1);
        mergeRequest.setNotPreferredPaperRecord(paperRecord2);
        mergeRequest.setDateCreated(new Date());
        paperRecordService.markPaperRecordsAsMerged(mergeRequest);

        assertThat(mergeRequest.getStatus(), is(PaperRecordMergeRequest.Status.MERGED));
        IsExpectedMergeRequest expectedMergeRequestMatcher = new IsExpectedMergeRequest(mergeRequest);
        verify(mockPaperRecordMergeRequestDAO).saveOrUpdate(argThat(expectedMergeRequestMatcher));
    }


    @Test
    public void mergingPatientsShouldCancelOpenRequestForPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setPaperRecord(paperRecord1);
        paperRecordRequest.updateStatus(Status.OPEN);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(PENDING_STATUSES, paperRecord1)).thenReturn(Collections.singletonList(paperRecordRequest));

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        assertThat(paperRecordRequest.getStatus(), is(Status.CANCELLED));

    }


    @Test
    public void mergingPatientsShouldCancelAssignedRequestForPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setPaperRecord(paperRecord1);
        paperRecordRequest.updateStatus(Status.ASSIGNED);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(PENDING_STATUSES, paperRecord1)).thenReturn(Collections.singletonList(paperRecordRequest));

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        assertThat(paperRecordRequest.getStatus(), is(Status.CANCELLED));
    }


     @Test
    public void mergingPatientsShouldNotCancelSentRequestForPreferredPatient() {

      Location medicalRecordLocation = createMedicalRecordLocation();

      Patient patient1 = new Patient();
      Patient patient2 = new Patient();

      PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
      PaperRecord paperRecord1 = new PaperRecord();
      paperRecord1.setRecordLocation(medicalRecordLocation);
      paperRecord1.setPatientIdentifier(identifier1);

      PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
      PaperRecord paperRecord2 = new PaperRecord();
      paperRecord2.setRecordLocation(medicalRecordLocation);
      paperRecord2.setPatientIdentifier(identifier2);

      patient1.addIdentifier(identifier1);
      patient2.addIdentifier(identifier2);

      PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
      paperRecordRequest.setPaperRecord(paperRecord1);
      paperRecordRequest.updateStatus(Status.SENT);

      when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.SENT), paperRecord1)).thenReturn(Collections.singletonList(paperRecordRequest));

      paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

      assertThat(paperRecordRequest.getStatus(), is(Status.SENT));

    }

    @Test
    public void mergingPatientsShouldNotCancelReturnedRequestForPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setPaperRecord(paperRecord1);
        paperRecordRequest.updateStatus(Status.CANCELLED);

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        assertThat(paperRecordRequest.getStatus(), is(Status.CANCELLED));

    }

    @Test
    public void mergingPatientsShouldCancelOpenRequestNotForPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setPaperRecord(paperRecord2);
        paperRecordRequest.updateStatus(Status.OPEN);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(PENDING_STATUSES, paperRecord2)).thenReturn(Collections.singletonList(paperRecordRequest));

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        assertThat(paperRecordRequest.getStatus(), is(Status.CANCELLED));

    }

    @Test
    public void mergingPatientsShouldCancelAssignedRequestForNotPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setPaperRecord(paperRecord2);
        paperRecordRequest.updateStatus(Status.ASSIGNED);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(PENDING_STATUSES, paperRecord2)).thenReturn(Collections.singletonList(paperRecordRequest));

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        assertThat(paperRecordRequest.getStatus(), is(Status.CANCELLED));
    }


    @Test
    public void mergingPatientsShouldNotCancelSentRequestForNotPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();

        Patient patient1 = new Patient();
        Patient patient2 = new Patient();

        PatientIdentifier identifier1 = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord paperRecord1 = new PaperRecord();
        paperRecord1.setRecordLocation(medicalRecordLocation);
        paperRecord1.setPatientIdentifier(identifier1);

        PatientIdentifier identifier2 = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord paperRecord2 = new PaperRecord();
        paperRecord2.setRecordLocation(medicalRecordLocation);
        paperRecord2.setPatientIdentifier(identifier2);

        patient1.addIdentifier(identifier1);
        patient2.addIdentifier(identifier2);

        PaperRecordRequest paperRecordRequest = new PaperRecordRequest();
        paperRecordRequest.setPaperRecord(paperRecord2);
        paperRecordRequest.updateStatus(Status.SENT);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.SENT), paperRecord2)).thenReturn(Collections.singletonList(paperRecordRequest));

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        assertThat(paperRecordRequest.getStatus(), is(Status.SENT));

    }

    @Test
    public void mergingPatientsShouldMoveAllSentPaperRecordRequestsToPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location recordLocation = new Location();
        Location requestLocation = new Location();

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PatientIdentifier preferredIdentifier = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord preferredPaperRecord = new PaperRecord();
        preferredPaperRecord.setRecordLocation(medicalRecordLocation);
        preferredPaperRecord.setPatientIdentifier(preferredIdentifier);

        PatientIdentifier nonPreferredIdentifier = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord nonPreferredPaperRecord = new PaperRecord();
        nonPreferredPaperRecord.setRecordLocation(medicalRecordLocation);
        nonPreferredPaperRecord.setPatientIdentifier(nonPreferredIdentifier);

        preferred.addIdentifier(preferredIdentifier);
        notPreferred.addIdentifier(nonPreferredIdentifier);

        PaperRecordRequest preferredRequest = new PaperRecordRequest();
        preferredRequest.setPaperRecord(preferredPaperRecord);
        preferredRequest.setRequestLocation(requestLocation);
        preferredRequest.updateStatus(Status.SENT);

        PaperRecordRequest nonPreferredRequest = new PaperRecordRequest();
        nonPreferredRequest.setPaperRecord(nonPreferredPaperRecord);
        nonPreferredRequest.setRequestLocation(requestLocation);
        nonPreferredRequest.updateStatus(Status.SENT);

        // we are expecting that the non-preferred request is now associated with the the preferred patient
        PaperRecordRequest expectedRequest = new PaperRecordRequest();
        expectedRequest.setPaperRecord(preferredPaperRecord);
        expectedRequest.setRequestLocation(requestLocation);
        expectedRequest.updateStatus(Status.SENT);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.SENT), nonPreferredPaperRecord)).thenReturn(Collections.singletonList(nonPreferredRequest));

        paperRecordService.markPaperRecordsForMerge(preferredPaperRecord, nonPreferredPaperRecord);

        assertThat(nonPreferredRequest, is(new IsExpectedRequest(expectedRequest)));
    }

    public void mergingPatientsShouldNotMoveReturnedPaperRecordRequestsToPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location recordLocation = new Location();
        Location requestLocation = new Location();

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PatientIdentifier preferredIdentifier = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord preferredPaperRecord = new PaperRecord();
        preferredPaperRecord.setRecordLocation(medicalRecordLocation);
        preferredPaperRecord.setPatientIdentifier(preferredIdentifier);

        PatientIdentifier nonPreferredIdentifier = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord nonPreferredPaperRecord = new PaperRecord();
        nonPreferredPaperRecord.setRecordLocation(medicalRecordLocation);
        nonPreferredPaperRecord.setPatientIdentifier(nonPreferredIdentifier);

        preferred.addIdentifier(preferredIdentifier);
        notPreferred.addIdentifier(nonPreferredIdentifier);

        PaperRecordRequest preferredRequest = new PaperRecordRequest();
        preferredRequest.setPaperRecord(preferredPaperRecord);
        preferredRequest.setRequestLocation(requestLocation);
        preferredRequest.updateStatus(Status.RETURNED);

        PaperRecordRequest nonPreferredRequest = new PaperRecordRequest();
        nonPreferredRequest.setPaperRecord(nonPreferredPaperRecord);
        nonPreferredRequest.setRequestLocation(requestLocation);
        nonPreferredRequest.updateStatus(Status.RETURNED);

        // should be same original
        PaperRecordRequest expectedRequest = new PaperRecordRequest();
        expectedRequest.setPaperRecord(nonPreferredPaperRecord);
        expectedRequest.setRequestLocation(requestLocation);
        expectedRequest.updateStatus(Status.RETURNED);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.SENT), nonPreferredPaperRecord)).thenReturn(Collections.singletonList(nonPreferredRequest));

        paperRecordService.markPaperRecordsForMerge(preferredPaperRecord, nonPreferredPaperRecord);

        assertThat(nonPreferredRequest, is(new IsExpectedRequest(expectedRequest)));
    }

    public void mergingPatientsShouldNotMoveCancelledPaperRecordRequestsToPreferredPatient() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        Location recordLocation = new Location();
        Location requestLocation = new Location();

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        PatientIdentifier preferredIdentifier = createIdentifier(medicalRecordLocation, "ABC");
        PaperRecord preferredPaperRecord = new PaperRecord();
        preferredPaperRecord.setRecordLocation(medicalRecordLocation);
        preferredPaperRecord.setPatientIdentifier(preferredIdentifier);

        PatientIdentifier nonPreferredIdentifier = createIdentifier(medicalRecordLocation, "EFG");
        PaperRecord nonPreferredPaperRecord = new PaperRecord();
        nonPreferredPaperRecord.setRecordLocation(medicalRecordLocation);
        nonPreferredPaperRecord.setPatientIdentifier(nonPreferredIdentifier);

        preferred.addIdentifier(preferredIdentifier);
        notPreferred.addIdentifier(nonPreferredIdentifier);

        PaperRecordRequest preferredRequest = new PaperRecordRequest();
        preferredRequest.setPaperRecord(preferredPaperRecord);
        preferredRequest.setRequestLocation(requestLocation);
        preferredRequest.updateStatus(Status.CANCELLED);

        PaperRecordRequest nonPreferredRequest = new PaperRecordRequest();
        nonPreferredRequest.setPaperRecord(nonPreferredPaperRecord);
        nonPreferredRequest.setRequestLocation(requestLocation);
        nonPreferredRequest.updateStatus(Status.CANCELLED);

        // should be same original
        PaperRecordRequest expectedRequest = new PaperRecordRequest();
        expectedRequest.setPaperRecord(nonPreferredPaperRecord);
        expectedRequest.setRequestLocation(requestLocation);
        expectedRequest.updateStatus(Status.CANCELLED);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.SENT), nonPreferredPaperRecord)).thenReturn(Collections.singletonList(nonPreferredRequest));

        paperRecordService.markPaperRecordsForMerge(preferredPaperRecord, nonPreferredPaperRecord);

        assertThat(nonPreferredRequest, is(new IsExpectedRequest(expectedRequest)));
    }

    @Test
    public void testPrintPaperRecordLabelShouldPrintSingleLabel() throws Exception {

        Location location = new Location(1);
        Patient patient = new Patient(1);

        when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");

        PaperRecordRequest request = createPaperRecordRequest(patient, location, "ABC");

        paperRecordService.printPaperRecordLabel(request, location);

        verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8", false, 600);

    }

    @Test
    public void testPrintPaperRecordLabelsShouldPrintThreeLabelIfCountSetToThree() throws Exception {

        Location location = new Location(1);
        Patient patient = new Patient(1);

        when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");

        PaperRecordRequest request = createPaperRecordRequest(patient, location, "ABC");

        paperRecordService.printPaperRecordLabels(request, location, 3);

        verify(mockPrinterService).printViaSocket("data\nlines\ndata\n" +
                "lines\ndata\nlines\n", Printer.Type.LABEL, location, "UTF-8", false, 800);

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

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(paperRecordIdentifier);
        paperRecord.setRecordLocation(location);

        when(mockPaperRecordDAO.findPaperRecords(patient, location)).thenReturn(Collections.singletonList(paperRecord));
        when(mockPaperRecordLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperRecordLabelTemplate.getEncoding()).thenReturn("UTF-8");

        paperRecordService.printPaperRecordLabels(patient, location, 1);

        verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8", false, 600);
    }

    @Test
    public void testPrintPaperFormLabelsShouldPrintThreeLabelIfCountSetToThree() throws Exception {

        Location location = new Location(1);
        Patient patient = new Patient(1);

        when(mockPaperFormLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperFormLabelTemplate.getEncoding()).thenReturn("UTF-8");

        PaperRecordRequest request = createPaperRecordRequest(patient, location, "ABC");

        paperRecordService.printPaperFormLabels(request, location, 3);

        verify(mockPrinterService).printViaSocket("data\nlines\ndata\n" +
                "lines\ndata\nlines\n", Printer.Type.LABEL, location, "UTF-8", false, 800);

    }



    @Test
    public void testPrintPaperFormLabelsByPatientShouldPrintThreeLabelIfCountSetToThree() throws Exception {

        Location location = new Location(1);
        Patient patient = new Patient(1);

        PatientIdentifier paperRecordIdentifier = new PatientIdentifier();
        paperRecordIdentifier.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier.setIdentifier("ABC");
        paperRecordIdentifier.setLocation(location);
        patient.addIdentifier(paperRecordIdentifier);

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(paperRecordIdentifier);
        paperRecord.setRecordLocation(location);

        when(mockPaperRecordDAO.findPaperRecords(patient, location)).thenReturn(Collections.singletonList(paperRecord));
        when(mockPaperFormLabelTemplate.generateLabel(patient, "ABC")).thenReturn("data\nlines\n");
        when(mockPaperFormLabelTemplate.getEncoding()).thenReturn("UTF-8");

        paperRecordService.printPaperFormLabels(patient, location, 3);

        verify(mockPrinterService).printViaSocket("data\nlines\ndata\n" +
                "lines\ndata\nlines\n", Printer.Type.LABEL, location, "UTF-8", false, 800);

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

        when(mockIdCardLabelTemplate.generateLabel(eq(patient), anyString())).thenReturn("data\nlines\n");
        when(mockIdCardLabelTemplate.getEncoding()).thenReturn("UTF-8");

        paperRecordService.printIdCardLabel(patient, location);

        verify(mockPrinterService).printViaSocket("data\nlines\n", Printer.Type.LABEL, location, "UTF-8", false, 600);
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
                Status.ASSIGNED);
        request1.setDateCreated(beforeExpireDate);

        PaperRecordRequest request2 = createPaperRecordRequest(patient2, recordLocation, "DEF", requestLocation,
                Status.ASSIGNED);
        request2.setDateCreated(afterExpireDate);

        PaperRecordRequest request3 = createPaperRecordRequest(patient3, recordLocation, "GHI", requestLocation,
                Status.OPEN);
        request3.setDateCreated(beforeExpireDate);

        PaperRecordRequest request4 = createPaperRecordRequest(patient4, recordLocation, "JKL", requestLocation,
                Status.OPEN);
        request4.setDateCreated(afterExpireDate);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.ASSIGNED), null,
                null, null)).thenReturn(Arrays.asList(request1, request2));

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.OPEN), null,
                null, null)).thenReturn(Arrays.asList(request3, request4));

        paperRecordService.expirePendingPullRequests(expireDate);

        verify(mockPaperRecordRequestDAO).saveOrUpdate(request1);
        verify(mockPaperRecordRequestDAO).saveOrUpdate(request3);

        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request2);
        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request4);

        assertThat(request1.getStatus(), is(Status.CANCELLED));
        assertThat(request3.getStatus(), is(Status.CANCELLED));

        assertThat(request2.getStatus(), is(Status.ASSIGNED));
        assertThat(request4.getStatus(), is(Status.OPEN));
    }

    @Test
    public void testExpirePullRequestsShouldNotCancelRequestsPendingCreation() throws Exception {

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
                Status.ASSIGNED, PaperRecord.Status.PENDING_CREATION);
        request1.setDateCreated(beforeExpireDate);

        PaperRecordRequest request2 = createPaperRecordRequest(patient2, recordLocation, null, requestLocation,
                Status.OPEN, PaperRecord.Status.PENDING_CREATION);
        request2.setDateCreated(beforeExpireDate);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.ASSIGNED), null,
                null, null)).thenReturn(Collections.singletonList(request1));

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.OPEN), null,
                null, null)).thenReturn(Collections.singletonList(request2));

        paperRecordService.expirePendingPullRequests(expireDate);

        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request1);
        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request2);

        assertThat(request1.getStatus(), is(Status.ASSIGNED));
        assertThat(request2.getStatus(), is(Status.OPEN));
    }


    @Test
    public void testExpireCreateRequestsShouldCancelCreateRequestsBeforeSpecifiedDate() throws Exception {

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
                Status.ASSIGNED, PaperRecord.Status.PENDING_CREATION);
        request1.setDateCreated(beforeExpireDate);

        PaperRecordRequest request2 = createPaperRecordRequest(patient2, recordLocation, "DEF", requestLocation,
                Status.ASSIGNED, PaperRecord.Status.PENDING_CREATION);
        request2.setDateCreated(afterExpireDate);

        PaperRecordRequest request3 = createPaperRecordRequest(patient3, recordLocation, "GHI", requestLocation,
                Status.OPEN, PaperRecord.Status.PENDING_CREATION);
        request3.setDateCreated(beforeExpireDate);

        PaperRecordRequest request4 = createPaperRecordRequest(patient4, recordLocation, "KLM", requestLocation,
                Status.OPEN, PaperRecord.Status.PENDING_CREATION);
        request4.setDateCreated(afterExpireDate);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.ASSIGNED), null,
                null, null)).thenReturn(Arrays.asList(request1, request2));

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.OPEN), null,
                null, null)).thenReturn(Arrays.asList(request3, request4));

        paperRecordService.expirePendingCreateRequests(expireDate);

        verify(mockPaperRecordRequestDAO).saveOrUpdate(request1);
        verify(mockPaperRecordRequestDAO).saveOrUpdate(request3);

        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request2);
        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request4);

        assertThat(request1.getStatus(), is(Status.CANCELLED));
        assertThat(request3.getStatus(), is(Status.CANCELLED));

        assertThat(request2.getStatus(), is(Status.ASSIGNED));
        assertThat(request4.getStatus(), is(Status.OPEN));
    }


    @Test
    public void testExpireCreateRequestsShouldNotCancelPullRequests() throws Exception {

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
                Status.ASSIGNED, PaperRecord.Status.ACTIVE);
        request1.setDateCreated(beforeExpireDate);

        PaperRecordRequest request2 = createPaperRecordRequest(patient2, recordLocation, "DEF", requestLocation,
                Status.OPEN);
        request2.setDateCreated(beforeExpireDate);

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.ASSIGNED), null,
                null, null)).thenReturn(Collections.singletonList(request1));

        when(mockPaperRecordRequestDAO.findPaperRecordRequests(Collections.singletonList(Status.OPEN), null,
                null, null)).thenReturn(Collections.singletonList(request2));

        paperRecordService.expirePendingCreateRequests(expireDate);

        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request1);
        verify(mockPaperRecordRequestDAO, never()).saveOrUpdate(request2);

        assertThat(request1.getStatus(), is(Status.ASSIGNED));
        assertThat(request2.getStatus(), is(Status.OPEN));
    }


    @Test
    public void createPaperMedicalRecordNumber_shouldSkipIdentifiersAlreadyInUse() {

        Location medicalRecordLocation = createMedicalRecordLocation();
        PatientIdentifier identifier = createIdentifier(medicalRecordLocation, "A00001");

        when(mockPatientService.getPatientIdentifiers("A00001", Collections.singletonList(paperRecordIdentifierType),
                Collections.singletonList(medicalRecordLocation), null, null))
                .thenReturn(Collections.singletonList(identifier));

        when(mockIdentifierSourceService.generateIdentifier(paperRecordIdentifierType, medicalRecordLocation, "generating a new paper record identifier number")).thenReturn("A00001", "A00002");

        PatientIdentifier paperMedicalRecordIdentifier = paperRecordService.createPaperRecord(new Patient(), medicalRecordLocation).getPatientIdentifier();

        assertThat(paperMedicalRecordIdentifier.getIdentifier(), is("A00002"));
    }

    // note that the getMedicalRecordLocation has been mocked out, so we are only testing the getArchivesLocation part here
    @Test
    public void getArchivesLocation_shouldFindArchivesLocation() {

        LocationTag medicalRecordTag = new LocationTag(PaperRecordConstants.LOCATION_TAG_MEDICAL_RECORD_LOCATION, null);
        LocationTag archivesTag = new LocationTag(PaperRecordConstants.LOCATION_TAG_ARCHIVES_LOCATION, null);
        when(mockPaperRecordProperties.getArchivesLocationTag()).thenReturn(archivesTag);

        Location medicalRecordLocation = new Location();
        Location archives = new Location();
        Location outpatientClinic = new Location();
        Location dental = new Location();

        medicalRecordLocation.addTag(medicalRecordTag);
        archives.addTag(archivesTag);

        medicalRecordLocation.addChildLocation(dental);
        medicalRecordLocation.addChildLocation(outpatientClinic);
        outpatientClinic.addChildLocation(archives);

        assertThat(paperRecordService.getArchivesLocationAssociatedWith(medicalRecordLocation), is(archives));
    }

    @Test(expected = IllegalStateException.class)
    public void getArchivesLocation_shouldNotFindArchivesLocationOutsideOfHierarchy() {

        LocationTag archivesTag = new LocationTag(PaperRecordConstants.LOCATION_TAG_ARCHIVES_LOCATION, null);
        when(mockPaperRecordProperties.getArchivesLocationTag()).thenReturn(archivesTag);

        Location medicalRecordLocation = new Location();
        Location archives = new Location();
        Location outpatientClinic = new Location();
        Location dental = new Location();
        Location anotherMedicalRecordLocation = new Location();
        Location anotherOutpatientClinic = new Location();

        archives.addTag(archivesTag);

        medicalRecordLocation.addChildLocation(dental);
        medicalRecordLocation.addChildLocation(outpatientClinic);
        outpatientClinic.addChildLocation(archives);

        anotherMedicalRecordLocation.addChildLocation(anotherOutpatientClinic);

        paperRecordService.getArchivesLocationAssociatedWith(anotherMedicalRecordLocation);

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
                                                        Location requestLocation, PaperRecordRequest.Status paperRecordRequestStatus) {

        return createPaperRecordRequest(patient, medicalRecordLocation, identifier, requestLocation, paperRecordRequestStatus, PaperRecord.Status.ACTIVE);
    }

    private PaperRecordRequest createPaperRecordRequest(Patient patient, Location medicalRecordLocation, String identifier,
                                                    Location requestLocation, PaperRecordRequest.Status paperRecordRequestStatus,
                                                    PaperRecord.Status paperRecordStatus) {

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setRecordLocation(medicalRecordLocation);
        paperRecord.updateStatus(paperRecordStatus);

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifierType(paperRecordIdentifierType);
        patientIdentifier.setIdentifier(identifier);
        patient.addIdentifier(patientIdentifier);
        paperRecord.setPatientIdentifier(patientIdentifier);

        PaperRecordRequest expectedRequest = new PaperRecordRequest();
        expectedRequest.setAssignee(null);
        expectedRequest.setCreator(authenticatedUser);
        expectedRequest.setPaperRecord(paperRecord);
        expectedRequest.setRequestLocation(requestLocation);
        expectedRequest.updateStatus(paperRecordRequestStatus);
        return expectedRequest;
    }


    private PaperRecordRequest createPaperRecordRequest(Patient patient, Location medicalRecordLocation, String identifier) {
        return createPaperRecordRequest(patient, medicalRecordLocation, identifier, null, Status.OPEN);
    }

    private PaperRecordRequest createPaperRecordRequest(Patient patient, Location location) {
        return createPaperRecordRequest(patient, location, "");
    }

    private PaperRecordMergeRequest createExpectedMergeRequest(PaperRecord preferredPaperRecord, PaperRecord notPreferredPaperRecord) {
        PaperRecordMergeRequest expectedMergeRequest = new PaperRecordMergeRequest();
        expectedMergeRequest.setPreferredPaperRecord(preferredPaperRecord);
        expectedMergeRequest.setNotPreferredPaperRecord(notPreferredPaperRecord);
        expectedMergeRequest.setStatus(PaperRecordMergeRequest.Status.OPEN);
        expectedMergeRequest.setCreator(authenticatedUser);
        return expectedMergeRequest;
    }

    private class PaperRecordServiceStub extends PaperRecordServiceImpl {

        private PatientIdentifierType paperRecordIdentifierType;

        public PaperRecordServiceStub(PatientIdentifierType paperRecordIdentifierType) {
            this.paperRecordIdentifierType = paperRecordIdentifierType;
        }

        @Override
        public Location getMedicalRecordLocationAssociatedWith(Location location) {
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
            assertThat(actualRequest.getPreferredPaperRecord(), is(expectedRequest.getPreferredPaperRecord()));
            assertThat(actualRequest.getNotPreferredPaperRecord(), is(expectedRequest.getNotPreferredPaperRecord()));
            assertThat(actualRequest.getStatus(), is(expectedRequest.getStatus()));
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
                assertThat(request.getPaperRecord().getPatientIdentifier().getIdentifier(), is(identifier));
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
