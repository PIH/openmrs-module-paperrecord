package org.openmrs.module.paperrecord.reporting.library;

import org.openmrs.PatientIdentifier;
import org.openmrs.module.paperrecord.PaperRecordProperties;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Basic patient data columns provided by Paper Record module
 */
@Component
public class PaperRecordPatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {

    @Autowired
    private PaperRecordProperties paperRecordProperties;

    public static final String PREFIX = "paperrecord.patientDataDefinition.";

    @Override
    public Class<? super PatientDataDefinition> getDefinitionType() {
        return PatientDataDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return PREFIX;
    }

    @DocumentedDefinition("paperRecordIdentifier")
    public PatientDataDefinition getPaperRecordIdentifier() {
        PatientIdentifierDataDefinition def = new PatientIdentifierDataDefinition();
        def.addType(paperRecordProperties.getPaperRecordIdentifierType());
        def.setIncludeFirstNonNullOnly(true);
        return def;
    }

}
