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

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.printer.PrinterService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Public API for functionality relating to paper medical records
 */
public interface PaperRecordService extends OpenmrsService {

    /**
     * Returns whether string is currently being used as a paper record identifier at the specified location
     *
     * @param identifier
     * @param location
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    boolean paperRecordIdentifierInUse(String identifier, Location location);

    /**
     * Returns true/false if a paper medical record exist at the specified location with the specified identifier
     * (We assume a record exists if the specified identifier has been assigned to a patient at the
     * specified location)
     *
     * @param identifier
     * @param location
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    boolean paperRecordExistsWithIdentifier(String identifier, Location location);


    /**
     * Returns true/false if the patient has a paper record at the specified location
     *
     * @param patient
     * @param location
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    boolean paperRecordExistsForPatient(Patient patient, Location location);

    /**
     * Returns true/false if the patient referenced by the given identifier has a paper record at the specified location
     *
     * @param patientIdentifier
     * @param location
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    boolean paperRecordExistsForPatientWithPrimaryIdentifier(String patientIdentifier, Location location);


    /**
     * Creates a paper record for the specified patient at the specified location
     * Assigns a patient identifier if necessary
     * Paper Record is set to the PENDING_CREATION state
     * If a paper record already exists at the specified location for the specified patient,
     * that paper record will be returned and an error will be logged
     *
     * @param patient
     * @param medicalRecordLocation
     * @return the Patient Record created
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    PaperRecord createPaperRecord(Patient patient, Location medicalRecordLocation);

    /**
     * This internal method should not be invoked directly!
     * <p/>
     * Workaround because we need this method to be @Transactional and Spring won't handle calling a @Transactional
     * method from within the same class:
     * http://stackoverflow.com/questions/3423972/spring-transaction-method-call-by-the-method-within-the-same-class-does-not-wo
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    PaperRecord createPaperRecordInternal(Patient patient, Location medicalRecordLocation);

    /**
     * Fetches the Paper Record Request with the specified id
     *
     * @param id primary key of the paper record request to retrieve
     * @return the patient record request with the specified id
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    PaperRecordRequest getPaperRecordRequestById(Integer id);

    /**
     * Fetches the Paper Record Merge Request with the specified id
     *
     * @param id
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    PaperRecordMergeRequest getPaperRecordMergeRequestById(Integer id);

    /**
     * Fetches all Paper Record Requests for the specified Patient
     *
     * @param patient a Patient
     * @return a List<PaperRecordRequest>
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getPaperRecordRequestsByPatient(Patient patient);

    /**
     * Requests the paper record for the specified patient for the specified location
     *
     * @param patient         the patient whose record we are requesting
     * @param recordLocation  the location of the record (ie, "Mirebalais Hospital"); if the specified location is not
     *                        a medical record location, will search up the location hierarchy for a valid medical
     *                        record location
     * @param requestLocation the location where the record is to be sent
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    List<PaperRecordRequest> requestPaperRecord(Patient patient, Location recordLocation, Location requestLocation);

    /**
     * This internal method should not be invoked directly!
     * <p/>
     * Workaround because we need this method to be @Transactional and Spring won't handle calling a @Transactional
     * method from within the same class:
     * http://stackoverflow.com/questions/3423972/spring-transaction-method-call-by-the-method-within-the-same-class-does-not-wo
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    List<PaperRecordRequest> requestPaperRecordInternal(Patient patient, Location recordLocation, Location requestLocation);

    /**
     * Gets all paper record requests in the OPEN state
     *
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    List<PaperRecordRequest> getOpenPaperRecordRequests();

    /**
     * Gets all paper record requests from the specified medical record location in the OPEN state
     *
     * @param medicalRecordLocation
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    List<PaperRecordRequest> getOpenPaperRecordRequests(Location medicalRecordLocation);

    /**
     * Retrieves all record requests that are open (ie, have yet to be assigned to an archivist for retrieval)
     * and are associated with records that have already been created (i.e., status != PENDING_CREATION)
     *
     * @return the list of all open paper record requests that need to be pulled
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getOpenPaperRecordRequestsToPull();

    /**
     * Retrieves all record requests from the specified medical record location
     * that are open (ie, have yet to be assigned to an archivist for retrieval)
     * and are associated with records that have already been created (i.e., status != PENDING_CREATION)
     *
     * @param medicalRecordLocation
     *
     * @return the list of all open paper record requests that need to be pulled
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getOpenPaperRecordRequestsToPull(Location medicalRecordLocation);

    /**
     * Retrieves all record requets that are open (ie, have yet to be assigned to an archivist for retrieval)
     * and are associated with records that need to be created (i.e., status == PENDING_CREATION)
     *
     * @return the list of all open paper record requests that need to be created
     */
    // TODO: once we have multiple medical record locations, we will need to add location as a criteria (see paperRecordExistsWithIdentifier)
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getOpenPaperRecordRequestsToCreate();

    /**
     * Retrieves all record requests from the specified medical record location
     * that are open (ie, have yet to be assigned to an archivist for retrieval)
     * and are associated with records that need to be created (i.e., status == PENDING_CREATION)
     *
     * @return the list of all open paper record requests that need to be created
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getOpenPaperRecordRequestsToCreate(Location medicalRecordLocation);

    /**
     * Creates or updates a Paper Record Request
     *
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    PaperRecordRequest savePaperRecordRequest(PaperRecordRequest paperRecordRequest);

    /**
     * Sets the status to ASSIGNED_TO_PULL and the assignee to the given value, for the given requests.
     *
     * @param requests
     * @param assignee
     * @param location the location to print any required registration labels at
     * @return the list that was passed in, but with assignees and status set
     * @throws IllegalStateException if any of the requests are not in the OPEN status
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    Map<String, List<String>> assignRequests(List<PaperRecordRequest> requests, Person assignee, Location location) throws UnableToPrintLabelException;

    /**
     * This internal method should not be invoked directly!
     * <p/>
     * Workaround because we need this method to be @Transactional and Spring won't handle calling a @Transactional
     * method from within the same class:
     * http://stackoverflow.com/questions/3423972/spring-transaction-method-call-by-the-method-within-the-same-class-does-not-wo
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    Map<String, List<String>> assignRequestsInternal(List<PaperRecordRequest> requests, Person assignee, Location location) throws UnableToPrintLabelException;

    /**
     * Retrieves all record requests in the ASSIGNED state
     *
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getAssignedPaperRecordRequests();

    /**
     * Retrieves all record requests from the specified medical record location in the ASSIGNED state
     *
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getAssignedPaperRecordRequests(Location medicalRecordLocation);

    /**
     * Retrieves all record requests that have been assigned and need to be pulled (ie, the associated PaperRecord status != PENDING_CREATION)
     *
     * @return the list of all assigned paper record requests that need to be pulled
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getAssignedPaperRecordRequestsToPull();

    /**
     * Retrieves all record requests from the specified medical record location
     * that have been assigned and need to be pulled (ie, the associated PaperRecord status != PENDING_CREATION)
     *
     * @return the list of all assigned paper record requests that need to be pulled
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getAssignedPaperRecordRequestsToPull(Location medicalRecordLocation);

    /**
     * Retrieves all record requests that have been assigned and need to be created (ie, the associated PaperRecord status == PENDING_CREATION)
     *
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getAssignedPaperRecordRequestsToCreate();

    /**
     * Retrieves all record requests from the specified medical record location
     * that have been assigned and need to be created (ie, the associated PaperRecord status == PENDING_CREATION)
     *
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getAssignedPaperRecordRequestsToCreate(Location medicalRecordLocation);

    /**
     * Returns the pending (i.e, OPEN or ASSIGNED) paper record request (if any) for the record with the specified identifier and location
     * (there should only be one pending request per identifier & *location*)
     *
     * @param identifier the paper record identifier OR the patient identifier associated with the request
     * @param medicalRecordLocation
     * @return the pending paper record request with the specified identifier (returns null if no request found)
     * @throws IllegalStateException if more than one request is found
     */
    PaperRecordRequest getPendingPaperRecordRequestByIdentifier(String identifier, Location medicalRecordLocation);

    /**
     * Returns the assigned (i.e, ASSIGNED) paper record request (if any) for the record with the specified identifier and location
     * (there should only be one assigned request per identifier & *location*)
     *
     * @param identifier the paper record identifier OR the patient identifier associated with the request
     * @param medicalRecordLocation
     * @return the assigned paper record request with the specified identifier (returns null if no request found
     * @throws IllegalStateException if more than one request is found
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    PaperRecordRequest getAssignedPaperRecordRequestByIdentifier(String identifier, Location medicalRecordLocation);


    /**
     * Returns the "sent" paper record requests (if any) for the record with specified identifier and location
     * (there may be multiple requests if for some reason a record was sent out twice without ever being returned)
     *
     * @param identifier the paper record identifier OR the patient identifier associated with the request
     * @param medicalRecordLocation
     * @return returns the "sent" paper record requests (if any) for the record with specified identifier
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordRequest> getSentPaperRecordRequestByIdentifier(String identifier, Location medicalRecordLocation);


    /**
     * Returns the most recent "sent" paper record request (if any) for the record
     * "Most Recent" is the one with the most recent dateStatusChanged field
     *
     * @param paperRecord
     * @return returns the most recent "sent" paper record request (if any) for the record with specified identifier
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    PaperRecordRequest getMostRecentSentPaperRecordRequest(PaperRecord paperRecord);

    /**
     * Marks the specified paper record request as "sent"
     * Also, if the associated PaperRecord has a status of PENDING_CREATION, it's status is set to ACTIVE.
     *
     * @param request
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    void markPaperRecordRequestAsSent(PaperRecordRequest request);

    /**
     * Marks the specified paper record request as "cancelled"
     *
     * @param request
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    void markPaperRecordRequestAsCancelled(PaperRecordRequest request);

    /**
     * Marks the specified paper record as "returned"
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    void markPaperRecordRequestAsReturned(PaperRecordRequest requests);

    /**
     * Prints a paper record label for the paper record associated wth the request
     * at the selected location
     *
     * @param request
     * @param location
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    void printPaperRecordLabel(PaperRecordRequest request, Location location) throws UnableToPrintLabelException;


    /**
     * Prints x numbers of paper record labels for the paper record associated with the request
     * at the default location
     *
     * @param request
     * @param location
     * @param count    the number of labels to print
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    void printPaperRecordLabels(PaperRecordRequest request, Location location, Integer count) throws UnableToPrintLabelException;

    /**
     * Prints x numbers of paper record labels for the paper record associated with the patient at the given location
     *
     * @param patient  the patient we want to print the label for
     * @param location the location where the record should be printed
     * @param count    the of labels to print
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    void printPaperRecordLabels(Patient patient, Location location, Integer count) throws UnableToPrintLabelException;

    /**
     * Prints x numbers of paper form labels for the paper record associated with the request
     * at the default location
     *
     * @param request
     * @param location
     * @param count    the number of labels to print
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    void printPaperFormLabels(PaperRecordRequest request, Location location, Integer count) throws UnableToPrintLabelException;


    /**
     * Prints x numbers of paper form labels for the paper record associated with the patient
     *
     * @param patient  the patient we want to print the label for
     * @param location the location where the record should be printed
     * @param count    the of labels to print
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    void printPaperFormLabels(Patient patient, Location location, Integer count) throws UnableToPrintLabelException;

    /**
     * Prints a label with the patient's paper record number(s), intended to be attached to the back of
     * the patient's ID card
     *
     * @param patient
     * @param location the location where the record should be printed
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    void printIdCardLabel(Patient patient, Location location) throws UnableToPrintLabelException;

    /**
     * Prints a full set of labels for a paper record:
     * 1 Paper Record Label, x Form Labels, and 1 ID Card Label
     * where x = PaperRecordConstants.NUMBER_OF_FORM_LABELS_TO_PRINT
     *
     * @param paperRecordRequest
     * @param location
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    void printPaperRecordLabelSet(PaperRecordRequest paperRecordRequest, Location location) throws UnableToPrintLabelException;

    /**
     * Creates a request to merge two paper records
     *
     * @param preferredPaperRecord   the preferred paper record
     * @param notPreferredPaperRecord the non-preferred paper record
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    void markPaperRecordsForMerge(PaperRecord preferredPaperRecord, PaperRecord notPreferredPaperRecord);

    /**
     * Marks that the paper record merge request has been completed
     *
     * @param mergeRequest
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    void markPaperRecordsAsMerged(PaperRecordMergeRequest mergeRequest);

    /**
     * Returns all merge requets with status = OPEN
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS)
    List<PaperRecordMergeRequest> getOpenPaperRecordMergeRequests(Location medicalRecordLocation);

    /**
     * Gets all paper records for the specified patient, across all locations
     * Excludes voided paper records (defined as paper records where the associated patient identifier is voided)
     *
     * @param patient
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    List<PaperRecord> getPaperRecords(Patient patient);

    /**
     * Gets all paper records for the specified patient, for the specified record location
     * Excludes voided paper records (defined as paper records where the associated patient identifier is voided)
     *
     * @param patient
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    List<PaperRecord> getPaperRecords(Patient patient, Location paperRecordLocation);

    /**
     * Gets all paper records for the specified patient identifier, for the specified record location
     * Excludes voided paper records (defined as paper records where the associated patient identifier is voided)
     *
     * @param patientIdentifier
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    PaperRecord getPaperRecord(PatientIdentifier patientIdentifier, Location paperRecordLocation);

    /**
     * Creates or updates a Paper Record
     *
     * @return
     */
    @Authorized(PaperRecordConstants.PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS)
    PaperRecord savePaperRecord(PaperRecord paperRecord);


    /**
     * Expires all pending pull requests (ie, those pull requests with status OPEN or ASSIGNED_TO_PULL) that
     * have a date_created before the specified expire date; Expires requests by setting the status
     * of those requests to CANCELLED
     *
     * @param expireDate
     */
    void expirePendingPullRequests(Date expireDate);

    /**
     * Expires all pending pull requests (ie, those create request with status OPEN or ASSIGNED_TO_CREATE) that
     * has a date_creaated before the specified expire date: Expires requests by setting the status
     * of those requests to CANCELLED
     * @param expireDate
     */
    void expirePendingCreateRequests(Date expireDate);

    /**
     * Finds the medical record location associated with the given location
     * (This searches up the hierarchy and returns the first location the Medical Record Location)
     *
     * @param location
     * @return
     * @throws IllegalStateException if no location found
     * @should ignore retired locations
     */
    Location getMedicalRecordLocationAssociatedWith(Location location);

    /**
     * Finds the archives room associated with this location
     * Thie method first determines the medical record location associated with the given
     * location (via the getMedicalRecordLocationAssociatedWith method) and then
     * traverses back done the tree until it finds a location tagged as an Archives Location
     * (Note that the assumption here is that there is only one archives location for each medical
     * record location, and the archives location must be a child of the medical record location)
     *
     * @return
     * @throws IllegalStateException if no location found
     * @should ignore retired locations
     */
    Location getArchivesLocationAssociatedWith(Location location);

    /**
     * Hack to bring this up to the interface level to allow us to stub out the template when printing
     */
    void setPrinterService(PrinterService printerService);

    /**
     * Hack to bring this up to the interface level to allow us to stub out the template in component tests
     */
    void setIdentifierSourceService(IdentifierSourceService identifierSourceService);

}

