package org.openmrs.module.paperrecord;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;

import java.util.Date;

/**
 * Represents a request to merge two existing paper records from the same record location
 */
public class PaperRecordMergeRequest extends BaseOpenmrsObject {

    public enum Status {OPEN, MERGED}

    private Integer mergeRequestId;;

    PaperRecord preferredPaperRecord;

    PaperRecord notPreferredPaperRecord;

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

    public PaperRecord getPreferredPaperRecord() {
        return preferredPaperRecord;
    }

    public void setPreferredPaperRecord(PaperRecord preferredPaperRecord) {
        this.preferredPaperRecord = preferredPaperRecord;
    }

    public PaperRecord getNotPreferredPaperRecord() {
        return notPreferredPaperRecord;
    }

    public void setNotPreferredPaperRecord(PaperRecord notPreferredPaperRecord) {
        this.notPreferredPaperRecord = notPreferredPaperRecord;
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
