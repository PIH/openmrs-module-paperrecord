package org.openmrs.module.paperrecord;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;

import java.util.Date;

/**
 * Represents a request to merge two existing paper records from the same record location
 */
public class PaperRecordMergeRequest extends BaseOpenmrsObject {

    public enum Status {OPEN, MERGED}

    private Integer mergeRequestId;

    Patient preferredPatient;

    Patient notPreferredPatient;

    String preferredIdentifier;

    String notPreferredIdentifier;

    Location recordLocation;

    Status status = Status.OPEN;

    User creator;

    Date dateCreated;


    @Override
    public Integer getId() {
        return mergeRequestId;
    }

    @Override
    public void setId(Integer mergeRequestId) {
        this.mergeRequestId = mergeRequestId;
    }

    public Integer getMergeRequestId() {
        return mergeRequestId;
    }

    public void setMergeRequestId(Integer mergeRequestId) {
        this.mergeRequestId = mergeRequestId;
    }

    public Patient getPreferredPatient() {
        return preferredPatient;
    }

    public void setPreferredPatient(Patient preferredPatient) {
        this.preferredPatient = preferredPatient;
    }

    public Patient getNotPreferredPatient() {
        return notPreferredPatient;
    }

    public void setNotPreferredPatient(Patient notPreferredPatient) {
        this.notPreferredPatient = notPreferredPatient;
    }

    public String getPreferredIdentifier() {
        return preferredIdentifier;
    }

    public void setPreferredIdentifier(String preferredIdentifier) {
        this.preferredIdentifier = preferredIdentifier;
    }

    public String getNotPreferredIdentifier() {
        return notPreferredIdentifier;
    }

    public void setNotPreferredIdentifier(String notPreferredIdentifier) {
        this.notPreferredIdentifier = notPreferredIdentifier;
    }

    public Location getRecordLocation() {
        return recordLocation;
    }

    public void setRecordLocation(Location recordLocation) {
        this.recordLocation = recordLocation;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
}
