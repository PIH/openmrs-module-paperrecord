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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.printer.PrinterService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaperRecordServiceComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    PaperRecordService paperRecordService;

    @Autowired
    PatientService patientService;

    @Autowired
    PersonService personService;

    @Autowired
    LocationService locationService;

    @Autowired
    PaperRecordProperties paperRecordProperties;

    private IdentifierSourceService mockIdentifierSourceService;

    private PrinterService mockPrinterService;

    @Before
    public void beforeAllTests() throws Exception {
        executeDataSet("paperRecordTestDataset.xml");

        // stub out the printer service
        mockPrinterService = mock(PrinterService.class);
        paperRecordService.setPrinterService(mockPrinterService);

        // stub out the identifier service
        mockIdentifierSourceService = mock(IdentifierSourceService.class);
        paperRecordService.setIdentifierSourceService(mockIdentifierSourceService);

        when(mockIdentifierSourceService.generateIdentifier(eq(paperRecordProperties.getPaperRecordIdentifierType()), any(Location.class), eq("generating a new dossier number")))
                .thenReturn("A00001", "A00002", "A00003");

    }

    @Test
    public void testThatServiceIsConfiguredCorrectly() {
        Assert.assertNotNull("Couldn't autowire PaperRecordService", paperRecordService);
        Assert.assertNotNull("Couldn't get PaperRecordService from Context", Context.getService(PaperRecordService.class));
    }

    @Test
    public void testPaperMedicalRecordExistsWithIdentifierReturnsTrueIfPaperMedicalRecordExists() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the paper record test dataset
        Assert.assertTrue(paperRecordService.paperRecordExistsWithIdentifier("CATBALL", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsReturnsTrueWhenUsingChildLocationOfMedicalRecordLocation() {

        // a child location of location defined in paperRecordServiceComponentTestDataset
        Location medicalRecordLocation = locationService.getLocation(1001);

        // this identifier exists in the paper record test dataset
        Assert.assertTrue(paperRecordService.paperRecordExistsWithIdentifier("CATBALL", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsReturnsFalseIfPaperMedicalRecordDoesNotExist() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the standard test data set, but there is no paper record associated with it
        Assert.assertFalse(paperRecordService.paperRecordExistsWithIdentifier("101", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsReturnsFalseIfIdentifierIsInUseButWrongLocation() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(2);

        // this identifier exists in the standard test data set
        Assert.assertFalse(paperRecordService.paperRecordExistsWithIdentifier("CATBALL", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsReturnsFalseIfIdentifierIsInUseButWrongIdentifierType() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the standard test data set
        Assert.assertFalse(paperRecordService.paperRecordExistsWithIdentifier("6TS-4", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsReturnsFalseIfIdentifierVoided() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the standard test data set
        Assert.assertFalse(paperRecordService.paperRecordExistsWithIdentifier("DOGBALL", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsForPatientWithIdentifierReturnsTrueIfPaperMedicalRecordExists() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the standard test data set, and references patient 7, who has a paper record assigned in the paper record test dataset
        Assert.assertTrue(paperRecordService.paperRecordExistsForPatientWithPrimaryIdentifier("6TS-4", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsForPatientShouldReturnFalseIfWrongIdentifierType() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the standard test data set, but it is paper record identifier, not a patient identifier
        Assert.assertFalse(paperRecordService.paperRecordExistsForPatientWithPrimaryIdentifier("CATBALL", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsForPatientShouldReturnFalseIfWrongLocation() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(2);

        // the patient with this primary identifier has a paper record identifier for location 1, but not location 2
        Assert.assertFalse(paperRecordService.paperRecordExistsForPatientWithPrimaryIdentifier("6TS-4", medicalRecordLocation));
    }

    @Test
    public void testPaperMedicalRecordExistsForPatientShouldReturnFalseIfPaperRecordIdentifierVoided() {

        // from the standard test dataset
        Location medicalRecordLocation = locationService.getLocation(1);

        // the patient (8) with this primary identifierhas a paper record identifier, but it is voided
        Assert.assertFalse(paperRecordService.paperRecordExistsForPatientWithPrimaryIdentifier("ABC123", medicalRecordLocation));
    }

    @Test
    public void testRequestPaperRecord() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // first, make sure that this record is not returned by the "to pull" service method
        Assert.assertEquals(0, paperRecordService.getOpenPaperRecordRequestsToPull(medicalRecordLocation).size());

        // make sure the record is in the database
        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        PaperRecordRequest request = requests.get(0);
        Assert.assertEquals(new Integer(2), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(1), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(3), request.getRequestLocation().getId());
        Assert.assertEquals("101", request.getPaperRecord().getPatientIdentifier().getIdentifier());
        Assert.assertEquals(PaperRecordRequest.Status.OPEN, request.getStatus());
        Assert.assertEquals(PaperRecord.Status.PENDING_CREATION, request.getPaperRecord().getStatus());
        Assert.assertNull(request.getAssignee());

    }

    @Test
    public void testRequestPaperRecordFromChildLocation() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1001);
        Location requestLocation = locationService.getLocation(3);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // first, make sure that this record is not returned by the "to pull" service method
        Assert.assertEquals(0, paperRecordService.getOpenPaperRecordRequestsToPull(medicalRecordLocation).size());

        // make sure the record is in the database
        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        PaperRecordRequest request = requests.get(0);
        Assert.assertEquals(new Integer(2), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(1), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(3), request.getRequestLocation().getId());
        Assert.assertEquals("101", request.getPaperRecord().getPatientIdentifier().getIdentifier());
        Assert.assertEquals(PaperRecordRequest.Status.OPEN, request.getStatus());
        Assert.assertEquals(PaperRecord.Status.PENDING_CREATION, request.getPaperRecord().getStatus());
        Assert.assertNull(request.getAssignee());

    }


    @Test
    public void testRequestPaperRecordWhenNoValidPatientIdentifierForPaperRecord() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(2);
        Location requestLocation = locationService.getLocation(3);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // first, make sure that this record is not returned by the "to pull" service method
        Assert.assertEquals(0, paperRecordService.getOpenPaperRecordRequestsToPull(medicalRecordLocation).size());

        // make sure the record is in the database
        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        PaperRecordRequest request = requests.get(0);
        Assert.assertEquals(new Integer(2), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(2), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(3), request.getRequestLocation().getId());
        Assert.assertEquals("A00001", request.getPaperRecord().getPatientIdentifier().getIdentifier());
        Assert.assertEquals(PaperRecordRequest.Status.OPEN, request.getStatus());
        Assert.assertEquals(PaperRecord.Status.PENDING_CREATION, request.getPaperRecord().getStatus());
        Assert.assertNull(request.getAssignee());

    }

    @Test
    public void testGetOpenPaperRecordRequestsToCreateForPatientsWithNoIdentifiers() {

        // all these are from the standard test dataset (neither patient have medical record identifiers at location 2)
        Patient patient = patientService.getPatient(2);
        Patient anotherPatient = patientService.getPatient(8);
        Location medicalRecordLocation = locationService.getLocation(2);
        Location requestLocation = locationService.getLocation(3);

        Assert.assertEquals(0, paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation).size());

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        paperRecordService.requestPaperRecord(anotherPatient, medicalRecordLocation, requestLocation);

        // make sure both records are now in the database
        Assert.assertEquals(2, paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation).size());
    }


    @Test
    public void testGetOpenPaperRecordRequestsToCreateForPatientsWithIdentifiers() {

        // all these are from the standard test dataset (both patients have medical record identifiers at location 1, but no paper record entry created yet)
        Patient patient = patientService.getPatient(2);
        Patient anotherPatient = patientService.getPatient(999);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);


        Assert.assertEquals(0, paperRecordService.getOpenPaperRecordRequestsToPull(medicalRecordLocation).size());

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);
        paperRecordService.requestPaperRecord(anotherPatient, medicalRecordLocation, requestLocation);

        // make sure both records are now is in the database
        Assert.assertEquals(2, paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation).size());
    }

    @Test
    public void testGetPaperRecordRequestById() {

        PaperRecordRequest request = paperRecordService.getPaperRecordRequestById(1);

        Assert.assertNotNull(request);
        Assert.assertEquals(new Integer(7), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(1), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(2), request.getRequestLocation().getId());
        Assert.assertEquals("CATBALL", request.getPaperRecord().getPatientIdentifier().getIdentifier());
        Assert.assertEquals(PaperRecordRequest.Status.SENT, request.getStatus());
        Assert.assertNull(request.getAssignee());

    }

    @Test
    public void testAssignRequest() throws UnableToPrintLabelException {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        // remove the address associated with this patient, so that we can avoid having to configure the Address template
        // (which isn't what we are looking to test here anyhow)
        patient.removeAddress(patient.getPersonAddress());

        // request a record
        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // retrieve that record
        List<PaperRecordRequest> paperRecordRequests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, paperRecordRequests.size()); // sanity check

        // assign the person to the request
        Person person = personService.getPerson(7);

        paperRecordService.assignRequests(paperRecordRequests, person, null);

        // should not move the record as the person already has an paper record identifier
        paperRecordRequests = paperRecordService.getAssignedPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, paperRecordRequests.size());
        PaperRecordRequest request = paperRecordRequests.get(0);
        Assert.assertEquals(PaperRecordRequest.Status.ASSIGNED, request.getStatus());
        Assert.assertEquals(PaperRecord.Status.PENDING_CREATION, request.getPaperRecord().getStatus());
        Assert.assertEquals("101", request.getPaperRecord().getPatientIdentifier().getIdentifier());

    }

    @Test
    public void testRequestPaperRecordWithDuplicateRequest() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // sanity check; make sure the record is in the database
        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        Date dateCreated = requests.get(0).getDateCreated();

        // now request the same record again
        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // there should still only be one paper record request
        requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        PaperRecordRequest request = requests.get(0);
        Assert.assertEquals(new Integer(2), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(1), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(3), request.getRequestLocation().getId());
        Assert.assertEquals("101", request.getPaperRecord().getPatientIdentifier().getIdentifier());
        Assert.assertEquals(PaperRecordRequest.Status.OPEN, request.getStatus());
        Assert.assertEquals(dateCreated, request.getDateCreated());
        Assert.assertNull(request.getAssignee());
    }

    @Test
    public void testRequestPaperRecordWhenDuplicateRequestShouldUpdateLocation() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);
        Location anotherRequestLocation = locationService.getLocation(2);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // sanity check; make sure the record is in the database
        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        Date dateCreated = requests.get(0).getDateCreated();

        // now request the same record again
        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, anotherRequestLocation);

        // there should still only be one paper record request, but with the new location
        requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        PaperRecordRequest request = requests.get(0);
        Assert.assertEquals(new Integer(2), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(1), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(2), request.getRequestLocation().getId());
        Assert.assertEquals("101",request.getPaperRecord().getPatientIdentifier().getIdentifier());
        Assert.assertEquals(PaperRecordRequest.Status.OPEN, request.getStatus());
        Assert.assertEquals(dateCreated, request.getDateCreated());
        Assert.assertNull(request.getAssignee());
    }

    @Test
    public void testRequestPaperRecordWhenDuplicateRequestThatHasAlreadyBeenAssigned() throws UnableToPrintLabelException {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        // remove the address associated with this patient, so that we can avoid having to configure the Address template
        // (which isn't what we are looking to test here anyhow)
        patient.removeAddress(patient.getPersonAddress());

        List<PaperRecordRequest> paperRecordRequests = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        Person person = personService.getPerson(7);
        paperRecordService.assignRequests(paperRecordRequests, person, null);

        // sanity check; make sure the record is in the database
        List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        Date dateCreated = requests.get(0).getDateCreated();

        // now request the same record again
        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // there should not be any open requested, and only the one assigned request
        requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(0, requests.size());
        requests = paperRecordService.getAssignedPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        PaperRecordRequest request = requests.get(0);
        Assert.assertEquals(new Integer(2), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(1), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(3), request.getRequestLocation().getId());
        Assert.assertEquals("101", request.getPaperRecord().getPatientIdentifier().getIdentifier());
        Assert.assertEquals(PaperRecordRequest.Status.ASSIGNED, request.getStatus());
        Assert.assertEquals(PaperRecord.Status.PENDING_CREATION, request.getPaperRecord().getStatus());
        Assert.assertEquals(dateCreated, request.getDateCreated());
        Assert.assertEquals(person, request.getAssignee());
    }

    @Test
    public void testRequestPaperRecordWhenSamePatientButDifferentMedicalRecordLocation() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(6);
        Location medicalRecordLocation = locationService.getLocation(2);
        Location anotherMedicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(1);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // sanity check; make sure the record is in the database
        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        Date dateCreated = requests.get(0).getDateCreated();

        // now request the the record from the same patient, but a different medical record location
        paperRecordService.requestPaperRecord(patient, anotherMedicalRecordLocation, requestLocation);

        // make sure the requests are in the proper queues
        requests = paperRecordService.getOpenPaperRecordRequestsToCreate();  // both if we don't limit by meidcal record location
        Assert.assertEquals(2, requests.size());

        requests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());

        requests = paperRecordService.getOpenPaperRecordRequestsToCreate(anotherMedicalRecordLocation);
        Assert.assertEquals(1, requests.size());
    }


    @Test
    public void testRequestPaperRecordShouldNotConsiderSentRequestAsDuplicate() {

        // create a request for the patient that has a "completed" request defined in paperRecordTestDataset.xml
        Patient patient = patientService.getPatient(7);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(2);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // make sure this request has been created (it is a pull request, since this patient already has a patient record)
        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToPull(medicalRecordLocation);
        Assert.assertEquals(1, requests.size());
        Date dateCreated = requests.get(0).getDateCreated();

        PaperRecordRequest request = requests.get(0);
        Assert.assertEquals(new Integer(7), request.getPaperRecord().getPatientIdentifier().getPatient().getId());
        Assert.assertEquals(new Integer(1), request.getPaperRecord().getRecordLocation().getId());
        Assert.assertEquals(new Integer(2), request.getRequestLocation().getId());
        Assert.assertEquals(PaperRecordRequest.Status.OPEN, request.getStatus());
        Assert.assertEquals(dateCreated, request.getDateCreated());
        Assert.assertNull(request.getAssignee());
    }

    @Test
    public void testGetAssignedRequestByIdentifierShouldNotReturnOpenRequest() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        PaperRecordRequest request = paperRecordService.getAssignedPaperRecordRequestByIdentifier("101", medicalRecordLocation);
        Assert.assertNull(request);
    }

    @Test
    public void testGetAssignedRequestByIdentifierShouldReturnAssignedPullRequest() throws UnableToPrintLabelException {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        // remove the address associated with this patient, so that we can avoid having to configure the Address template
        // (which isn't what we are looking to test here anyhow)
        patient.removeAddress(patient.getPersonAddress());

        // request a record
        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // retrieve that record
        List<PaperRecordRequest> paperRecordRequests = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation);
        Assert.assertEquals(1, paperRecordRequests.size()); // sanity check

        // assign the person to the request
        Person person = personService.getPerson(7);
        paperRecordService.assignRequests(paperRecordRequests, person, null);

        PaperRecordRequest request = paperRecordService.getAssignedPaperRecordRequestByIdentifier("101", medicalRecordLocation);

        Assert.assertEquals(PaperRecordRequest.Status.ASSIGNED, request.getStatus());
        Assert.assertEquals(PaperRecord.Status.PENDING_CREATION, request.getPaperRecord().getStatus());
        Assert.assertEquals(new Integer(7), request.getAssignee().getId());
        Assert.assertEquals("101", request.getPaperRecord().getPatientIdentifier().getIdentifier());

    }

    @Test
    public void testGetAssignedRequestByIdentifierShouldNotReturnOpenRequestWhenReferencedByPatientIdentifier() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        PaperRecordRequest request = paperRecordService.getAssignedPaperRecordRequestByIdentifier("101-6", medicalRecordLocation);
        Assert.assertNull(request);
    }

    @Test
    public void testGetAssignedRequestByIdentifierShouldReturnNullIfNoActiveRequests() {

        Location medicalRecordLocation = locationService.getLocation(1);

        // there is a paper record request in the sample database with this identifier, but it is marked as SENT
        Assert.assertNull(paperRecordService.getAssignedPaperRecordRequestByIdentifier("CATBALL", medicalRecordLocation));
    }

    @Test
    public void testMarkPaperRecordRequestAsSentShouldMarkRecordRequestAsSentAndSetRecordToActive() {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        // request a record
        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // retrieve that record
        PaperRecordRequest request = paperRecordService.getOpenPaperRecordRequestsToCreate().get(0);

        // store the id for future retrieval
        int id = request.getId();

        paperRecordService.markPaperRecordRequestAsSent(request);

        // make sure this request has been changed to "sent" in the database
        Context.flushSession();
        Context.clearSession();

        PaperRecordRequest returnedRequest = paperRecordService.getPaperRecordRequestById(id);
        Assert.assertEquals(PaperRecordRequest.Status.SENT, request.getStatus());
        Assert.assertEquals(PaperRecord.Status.ACTIVE, request.getPaperRecord().getStatus());
    }

    @Test
    public void testMarkPaperRecordRequestsAsReturnedShouldMarkSentRecordRequestAsReturned()
            throws Exception {

        // all these are from the standard test dataset
        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(3);

        // request a record
        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        // retrieve that record
        PaperRecordRequest request = paperRecordService.getOpenPaperRecordRequestsToCreate(medicalRecordLocation).get(0);

        // store the id and identifier for future retrieval
        int id = request.getId();

        // mark the record as sent
        paperRecordService.markPaperRecordRequestAsSent(request);

        // mark it as returned
        paperRecordService.markPaperRecordRequestAsReturned(request);

        // make sure this request has been changed to "returned" in the database
        Context.flushSession();
        Context.clearSession();

        PaperRecordRequest returnedRequest = paperRecordService.getPaperRecordRequestById(id);
        Assert.assertEquals(PaperRecordRequest.Status.RETURNED, request.getStatus());
    }

    @Test
    public void testGetSentPaperRecordRequestsShouldFetchSentRecordRequest() {

        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the sample test data
        List<PaperRecordRequest> requests = paperRecordService.getSentPaperRecordRequestByIdentifier("CATBALL", medicalRecordLocation);
        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(new Integer(1), requests.get(0).getId());
    }

    @Test
    public void testGetSentPaperRecordRequestsShouldFetchSentRecordRequestsReferencedByPatientIdentifier() {

        Location medicalRecordLocation = locationService.getLocation(1);

        // this identifier exists in the sample test data
        List<PaperRecordRequest> requests = paperRecordService.getSentPaperRecordRequestByIdentifier("6TS-4", medicalRecordLocation);
        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(new Integer(1), requests.get(0).getId());
    }

    @Test
    public void testGetSentRequestByIdentifierShouldReturnNullIfNoSentRequests() {

        Location medicalRecordLocation = locationService.getLocation(1);

        Assert.assertNull(paperRecordService.getAssignedPaperRecordRequestByIdentifier("101", medicalRecordLocation));
    }

    @Test
    public void testUpdateStatusSetDateLastUpdated() throws InterruptedException {

        PaperRecordRequest request = new PaperRecordRequest();

        request.updateStatus(PaperRecordRequest.Status.OPEN);
        Date date = request.getDateStatusChanged();

        paperRecordService.savePaperRecordRequest(request);
        int id = request.getId();

        Context.flushSession();
        Context.clearSession();

        PaperRecordRequest retrievedRequest = paperRecordService.getPaperRecordRequestById(id);
        Assert.assertNotNull(retrievedRequest.getDateStatusChanged());
    }

    @Test
    public void testMarkPapersRecordForMergeShouldCreatePaperRecordMergeRequest() throws Exception {

        Patient patient1 = patientService.getPatient(2);
        Patient patient2 = patientService.getPatient(6);

        Location paperRecordLocation = locationService.getLocation(1);

        // create a couple paper records
        PaperRecord paperRecord1 = paperRecordService.createPaperRecord(patient1, paperRecordLocation);
        PaperRecord paperRecord2 = paperRecordService.createPaperRecord(patient2, paperRecordLocation);

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        Assert.assertEquals(1, paperRecordService.getOpenPaperRecordMergeRequests(paperRecordLocation).size());
        PaperRecordMergeRequest request = paperRecordService.getOpenPaperRecordMergeRequests(paperRecordLocation).get(0);

        Assert.assertEquals(paperRecord1, request.getPreferredPaperRecord());
        Assert.assertEquals(paperRecord2, request.getNotPreferredPaperRecord());
        Assert.assertEquals(PaperRecordMergeRequest.Status.OPEN, request.getStatus());
        Assert.assertNotNull(request.getDateCreated());
        Assert.assertEquals(Context.getAuthenticatedUser(), request.getCreator());

        Assert.assertFalse(paperRecord1.getPatientIdentifier().isVoided());
        Assert.assertTrue(paperRecord2.getPatientIdentifier().isVoided());
    }

    @Test
    public void testMarkPaperRecordsAsMergedShouldMarkPaperRecordsAsMerged() throws Exception {

        Patient patient1 = patientService.getPatient(2);
        Patient patient2 = patientService.getPatient(6);

        Location paperRecordLocation = locationService.getLocation(1);

        // create a couple paper records
        PaperRecord paperRecord1 = paperRecordService.createPaperRecord(patient1, paperRecordLocation);
        PaperRecord paperRecord2 = paperRecordService.createPaperRecord(patient2, paperRecordLocation);

        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);

        Assert.assertEquals(1, paperRecordService.getOpenPaperRecordMergeRequests(paperRecordLocation).size());  // sanity check
        PaperRecordMergeRequest request = paperRecordService.getOpenPaperRecordMergeRequests(paperRecordLocation).get(0);

        paperRecordService.markPaperRecordsAsMerged(request);

        int id = request.getId();

        Context.flushSession();
        Context.clearSession();

        request = paperRecordService.getPaperRecordMergeRequestById(id);
        Assert.assertEquals(PaperRecordMergeRequest.Status.MERGED, request.getStatus());
    }

    @Test
    public void testMarkPaperRecordsAsMergedShouldMergeExistingPaperRecordRequests() throws Exception {

        Location paperRecordLocation = locationService.getLocation(1);
        Location someLocation = locationService.getLocation(2);
        Location anotherLocation = locationService.getLocation(3);

        Patient patient1 = patientService.getPatient(2);
        Patient patient2 = patientService.getPatient(6);

        // create a couple paper records
        PaperRecord paperRecord1 = paperRecordService.createPaperRecord(patient1, paperRecordLocation);
        PaperRecord paperRecord2 = paperRecordService.createPaperRecord(patient2, paperRecordLocation);

        // first, create a couple record requests
        PaperRecordRequest request1 = paperRecordService.requestPaperRecord(patient1, paperRecordLocation, someLocation).get(0);
        PaperRecordRequest request2 = paperRecordService.requestPaperRecord(patient2, paperRecordLocation, anotherLocation).get(0);
        request2.updateStatus(PaperRecordRequest.Status.SENT);
        paperRecordService.savePaperRecordRequest(request2);
        PaperRecordRequest request3 = paperRecordService.requestPaperRecord(patient2, paperRecordLocation, anotherLocation).get(0);


        Assert.assertEquals(2, paperRecordService.getOpenPaperRecordRequestsToCreate().size());   // sanity checks
        Assert.assertNull(paperRecordService.getMostRecentSentPaperRecordRequest(paperRecord1));
        Assert.assertNotNull(paperRecordService.getMostRecentSentPaperRecordRequest(paperRecord2));

        // now create the merge request & then mark it as merged
        paperRecordService.markPaperRecordsForMerge(paperRecord1, paperRecord2);
        PaperRecordMergeRequest mergeRequest = paperRecordService.getOpenPaperRecordMergeRequests(paperRecordLocation).get(0);
        paperRecordService.markPaperRecordsAsMerged(mergeRequest);

        // there should be no outstanding cretae requests (since we just cancel them)
        Assert.assertEquals(0, paperRecordService.getOpenPaperRecordRequestsToCreate().size());

        // the sent request should have been moved to the other paper record
        Assert.assertNotNull(paperRecordService.getMostRecentSentPaperRecordRequest(paperRecord1));
        Assert.assertNull(paperRecordService.getMostRecentSentPaperRecordRequest(paperRecord2));

    }

    @Test
    public void shouldCancelOpenPaperRecordRequestsToCreate() {

        assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(0));

        Patient patient = patientService.getPatient(2);
        Location medicalRecordLocation = locationService.getLocation(2);
        Location requestLocation = locationService.getLocation(3);

        paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation);

        List<PaperRecordRequest> requests = paperRecordService.getOpenPaperRecordRequestsToCreate();
        assertThat(requests.size(), is(1));

        PaperRecordRequest request = requests.get(0);
        paperRecordService.markPaperRecordRequestAsCancelled(request);

        assertThat(request.getStatus(), is(PaperRecordRequest.Status.CANCELLED));
        assertThat(paperRecordService.getOpenPaperRecordRequestsToCreate().size(), is(0));
    }

    @Test
    public void testPaperRecordIdentifierInUseIfInUse() {

        Location medicalRecordLocation1 = locationService.getLocation(1);
        Location medicalRecordLocation2 = locationService.getLocation(2);

        // identifier in use at this location
        assertTrue(paperRecordService.paperRecordIdentifierInUse("CATBALL", medicalRecordLocation1));

        // real paper record identifier, but wrong location
        assertFalse(paperRecordService.paperRecordIdentifierInUse("CATBALL", medicalRecordLocation2));

        // bogus identifier at valid medical record location
        assertFalse(paperRecordService.paperRecordIdentifierInUse("BOGUS", medicalRecordLocation1));

        // primary identifier should return false
        assertFalse(paperRecordService.paperRecordIdentifierInUse("101-6", medicalRecordLocation1));

    }

    @Test
    public void testPaperRecordExistsForPatient() {

        Patient patientWithPaperRecord = patientService.getPatient(7);
        Patient patientWithoutPaperRecord = patientService.getPatient(6);

        Location medicalRecordLocation1 = locationService.getLocation(1);
        Location medicalRecordLocation2 = locationService.getLocation(2);

        // right location
        assertTrue(paperRecordService.paperRecordExistsForPatient(patientWithPaperRecord, medicalRecordLocation1));

        // wrong location
        assertFalse(paperRecordService.paperRecordExistsForPatient(patientWithPaperRecord, medicalRecordLocation2));


        // patient with paper record identifier, but no paper record itself (in real-life, we probbly should never get this)
        assertFalse(paperRecordService.paperRecordExistsForPatient(patientWithoutPaperRecord, medicalRecordLocation1));

    }

    @Test
    public void testGetMostRecentPaperRecordRequest() {

        Patient patient = patientService.getPatient(7);

        Location medicalRecordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(2);

        // we are request this record a few times
        PaperRecordRequest paperRecordRequest1 = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation).get(0);
        paperRecordService.markPaperRecordRequestAsSent(paperRecordRequest1);

        PaperRecordRequest paperRecordRequest2 = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation).get(0);
        paperRecordService.markPaperRecordRequestAsSent(paperRecordRequest2);

        PaperRecordRequest paperRecordRequest3 = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation).get(0);
        paperRecordService.markPaperRecordRequestAsSent(paperRecordRequest3);

        // note that this last two requests aren't in the "sent" state
        PaperRecordRequest paperRecordRequest4 = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation).get(0);
        paperRecordService.markPaperRecordRequestAsReturned(paperRecordRequest4);

        PaperRecordRequest paperRecordRequest5 = paperRecordService.requestPaperRecord(patient, medicalRecordLocation, requestLocation).get(0);
        paperRecordService.markPaperRecordRequestAsReturned(paperRecordRequest5);

        PaperRecord paperRecord = paperRecordService.getPaperRecords(patient, medicalRecordLocation).get(0);
        assertThat(paperRecordService.getMostRecentSentPaperRecordRequest(paperRecord), is(paperRecordRequest3));

    }

}
