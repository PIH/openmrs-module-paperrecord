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

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Models a request for paper record within the system
 * <p/>
 * Requests for paper records are modeled and tracked via PaperRecordRequest domain object.  Requests are made
 * via the PaperRecordService API.  A request is made for a certain Patient's record at a specified medical record
 * Location to be send to some requested Location.  Requests can have the following states:
 * <p/>
 * OPEN--the initial state of a request after it is placed
 * <p/>
 * ASSIGNED --after an archivist has claimed responsibility for a request, it is moved into this state
 * <p/>
 * SENT--once an archivist retrieves or creates a record and enters/scans the record identifier, the request is
 * transitioned to the SENT state; a request in the SENT state represents that the associated record has been
 * "checked out" of the archive room--and therefore the requested Location on the request should be the current
 * location of the record.
 * <p/>
 * RETURNED--one a record is returned to the archive room and the archivist enters/scans the record identifier,
 * the record is transitioned to the RETURNED state, effectively ending the workflow of a Paper Record Request.
 * <p/>
 * CANCELLED--notes that a request has been cancelled without being fulfilled
 * <p/>
 * "OPEN" and "ASSIGNED" are considered "pending" states. A single paper record
 * should never have more than one request in a "pending" state at any one time.
 * <p/>
 * A few notes--we don't currently 100% support multiple paper record locations... there are a few API methods within
 * the PaperRecordService that will have to be modified to support filtering by location in order to fully support
 * multiple locations. (These methods should be flagged with TO DOS referencings this point within the code)
 */


public class PaperRecordRequest extends BaseOpenmrsObject {

    public static enum Status {OPEN, ASSIGNED, SENT, RETURNED, CANCELLED}

    public static List<Status> PENDING_STATUSES = Arrays.asList(Status.OPEN, Status.ASSIGNED);

    private Integer requestId;

    private PaperRecord paperRecord;

    // TODO: remove this?
    private Location recordLocation;

    private Location requestLocation;

    private Person assignee;

    private Status status = Status.OPEN;

    private User creator;

    private Date dateCreated;

    private Date dateStatusChanged;

    public PaperRecordRequest() {
    }

    @Override
    public String toString() {
        String ret;
        ret = this.getId() == null ? "(no id) " : this.getId().toString() + " ";
        ret += this.getPaperRecord() == null ? "(no paper record) " : this.getPaperRecord().toString() + " ";
        ret += this.getRecordLocation() == null ? "(no record location) " : this.getRecordLocation().toString() + " ";
        ret += this.getRequestLocation() == null ? "(no request location) " : this.getRequestLocation().toString() + " ";
        ret += this.getStatus() == null ? "(no status) " : this.getStatus().toString() + " ";
        ret += this.getAssignee() == null ? "(no assignee) " : this.getAssignee().toString() + " ";
        ret += this.getCreator() == null ? "(no creator) " : this.getCreator().toString() + " ";
        ret += this.getDateCreated() == null ? "(no date created) " : this.getDateCreated().toString() + " ";
        ret += this.getDateStatusChanged() == null ? "(no date status changed)" : this.getDateStatusChanged().toString();
        return "Paper Record Request: [" + ret + "]";
    }

    public void updateStatus(Status status) {
        this.dateStatusChanged = new Date();
        this.status = status;
    }

    @Override
    public Integer getId() {
        return requestId;
    }

    @Override
    public void setId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public PaperRecord getPaperRecord() {
        return paperRecord;
    }

    public void setPaperRecord(PaperRecord paperRecord) {
        this.paperRecord = paperRecord;
    }

    public Location getRecordLocation() {
        return recordLocation;
    }

    public void setRecordLocation(Location recordLocation) {
        this.recordLocation = recordLocation;
    }

    public Location getRequestLocation() {
        return requestLocation;
    }

    public void setRequestLocation(Location requestLocation) {
        this.requestLocation = requestLocation;
    }

    public Person getAssignee() {
        return assignee;
    }

    public void setAssignee(Person assignee) {
        this.assignee = assignee;
    }

    // no setter for status is intentional, so we force use of the updateStatus method

    public Status getStatus() {
        return status;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateStatusChanged() {
        return dateStatusChanged;
    }

    public void setDateStatusChanged(Date dateStatusChanged) {
        this.dateStatusChanged = dateStatusChanged;
    }
}
