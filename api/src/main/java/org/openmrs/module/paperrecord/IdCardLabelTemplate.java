package org.openmrs.module.paperrecord;

import org.openmrs.Patient;

public interface IdCardLabelTemplate {

    String generateLabel(Patient patient);

    String getEncoding();

}
