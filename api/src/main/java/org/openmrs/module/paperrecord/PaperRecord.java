package org.openmrs.module.paperrecord;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;

import java.util.Date;

/**
 * Models a physical Paper Record within the system
 * <p/>
 * Every paper record is associated with a Patient Identifier of type GP_PAPER_RECORD_IDENTIFIER_TYPE.
 * There should never be more than one paper record per identifier. The patient identifier and the paper record are tightly bound...
 * Note that the Paper Record does not have it's own void property--the isVoided method delegates to the voided method of the
 * underyling Patient Identifier.
 * <p/>
 * TODO: how well do we enforce that a patient identifier can never be associated with more than one paper record--we do not enforce it at the DB level, I believe
 * <p/>
 *  In the initial deployment of the system, all paper records have the same record location, "Mirebalais", but it is intended in
 * the future to support multiple locations... for instance, if multiple hospitals are handled in the same system,
 * or if there are multiple sets of patient records within a single hospital (ie, perhaps the records at the
 * "Outpatient Clinic" and the records at the "Women's Health Clinic").
 * <p/>
 * We don't currently 100% support multiple paper record locations... there are a few API methods within
 * the PaperRecordService that will have to be modified to support filtering by location in order to fully support
 * multiple locations. (These methods should be flagged with TO DOS referencings this point within the code)
 * <p/>
 *  Within a single location, all the record identifiers should be unique, and a patient should have no more than
 * one record (since record identifiers are assigned by the EMR, and not manually, we should be able to prevent
 * the creation of multiple records for the same patient at the same location--BUT we will need to handle the
 * potential to merging of two patients, each with their own paper record).
 * <p/>
 * Currently, a Paper Record can have one of two states:
 *  PENDING_CREATION: this means that although a paper record identifier has been assigned, the actual physical paper record (ie labeled folder) still needs to be created
 *  ACTIVE: the physical folder had as been created, and so therefore the record is active
 * <p/>
 *  (Potential future states could be MISSING or ARCHIVED)
 * <p/>
 * Also, although we currently don't specifically mandate that record identifiers be unique across *all* locations, but we may
 * want to enforce this as we add additional locations, so that given a record number we can identify the patient
 * the record refers to without having to be in the context of a specific location.  (Since for storage and retrieval,
 * it is convenient for records to have sequential record identifiers, we could accomplish this via an alphanumeric
 * prefix--ie, A0000001 and B0000001. In Mirebalais we are using a prefix like this preparation for eventuality of
 * adding multiple locations).
 * <p/>
 * Note that in our original model there was no Patient Record domain object--only Paper Record Requests. Whether or not a paper record existed
 * for a patient was deterimined by whether or not they had a patient identifier of the appropriate type.  We had to refactor
 * when it was determined that we wanted to be able to assign paper record numbers to patients before we created the
 * actual, physical, underlying record.
 */
public class PaperRecord extends BaseOpenmrsObject {

    // TODO: in the future we will probably want to create a MISSING status, and perhaps status to represent whether the
    // TODO: record is in/out of the archive; until then, I've created the ACTIVE state to cover any state other than PENDING_CREATION
    // TODO: I'm not a huge fan of the "ACTIVE" name and tried to use it minimally, so it can be removed/changed as necessary
    public static enum Status {PENDING_CREATION, ACTIVE}

    private Integer recordId;

    // TODO: do we want to better enforce only one record per identifier/location
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

    @Override
    public String toString() {
        String ret;
        ret = this.getId() == null ? "(no id) " : this.getId().toString() + " ";
        ret += this.getPatientIdentifier() == null ? "(no patient identifer) " : this.getPatientIdentifier().toString() + " ";
        ret += this.getRecordLocation() == null ? "(no record location) " : this.getRecordLocation().toString() + " ";
        ret += this.getStatus() == null ? "(no status) " : this.getStatus().toString() + " ";
        ret += this.getCreator() == null ? "(no creator) " : this.getCreator().toString() + " ";
        ret += this.getDateCreated() == null ? "(no date created) " : this.getDateCreated().toString() + " ";
        ret += this.getDateStatusChanged() == null ? "(no date status changed)" : this.getDateStatusChanged().toString();
        return "Paper Record: [" + ret + "]";
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
