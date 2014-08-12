package org.openmrs.module.paperrecord;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;

import java.util.Date;

public class PaperRecord extends BaseOpenmrsObject {

    // TODO: in the future we will probably want to create a MISSING status, and perhaps status to represent whether the
    // TODO: record is in/out of the archive; until then, I've created the ACTIVE state to cover any state other than PENDING_CREATION
    // TODO: I'm not a huge fan of the "ACTIVE" name and tried to use it minimally, so it can be removed/changed as necessary
    public static enum Status {PENDING_CREATION, ACTIVE}

    private Integer recordId;

    // TODO: only one record per identifier/location
    private PatientIdentifier patientIdentifier;

    private Location recordLocation;

    private Status status = Status.PENDING_CREATION;

    private User creator;

    private Date dateCreated;

    private Date dateStatusChanged;

    @Override
    public Integer getId() {
        return recordId;
    }

    @Override
    public void setId(Integer id) {
        recordId = id;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    // TODO is this bad?
    // note that we tie whether or not a paper record is voided to whether or not the associated patient identifier is voided
    public boolean getVoided() {
        return patientIdentifier == null ? false : patientIdentifier.getVoided();
    }

    public PatientIdentifier getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(PatientIdentifier patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
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

    public void updateStatus(Status status) {
        this.dateStatusChanged = new Date();
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getDateStatusChanged() {
        return dateStatusChanged;
    }

    public void setDateStatusChanged(Date dateStatusChanged) {
        this.dateStatusChanged = dateStatusChanged;
    }
}
