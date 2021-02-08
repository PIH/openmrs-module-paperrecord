package org.openmrs.module.paperrecord;

import org.openmrs.LocationTag;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.emrapi.utils.ModuleProperties;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("paperRecordProperties")
public class PaperRecordProperties extends ModuleProperties {

    public PatientIdentifierType getPaperRecordIdentifierType() {
        return getPatientIdentifierTypeByGlobalProperty(PaperRecordConstants.GP_PAPER_RECORD_IDENTIFIER_TYPE, true);
    }

    public PatientIdentifierType getExternalDossierIdentifierType() {
        return getPatientIdentifierTypeByGlobalProperty(PaperRecordConstants.GP_EXTERNAL_DOSSIER_IDENTIFIER_TYPE, true);
    }

    public LocationTag getMedicalRecordLocationLocationTag() {
        return locationService.getLocationTagByName(PaperRecordConstants.LOCATION_TAG_MEDICAL_RECORD_LOCATION);
    }

    public LocationTag getArchivesLocationTag() {
        return locationService.getLocationTagByName(PaperRecordConstants.LOCATION_TAG_ARCHIVES_LOCATION);
    }

}
