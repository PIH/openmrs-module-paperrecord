package org.openmrs.module.paperrecord;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;

public class PaperRecord extends BaseOpenmrsObject {

    private Integer recordId;

    private PatientIdentifier patientIdentifier;

    private Location location;

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

    public PatientIdentifier getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(PatientIdentifier patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
