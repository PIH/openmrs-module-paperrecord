package org.openmrs.module.paperrecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.EmrApiProperties;

import java.util.List;

public class DefaultZplIdCardLabelTemplate implements IdCardLabelTemplate {

    private final Log log = LogFactory.getLog(getClass());

    private EmrApiProperties emrApiProperties;

    private PaperRecordProperties paperRecordProperties;

    private MessageSourceService messageSourceService;

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setPaperRecordProperties(PaperRecordProperties paperRecordProperties) {
        this.paperRecordProperties = paperRecordProperties;
    }

    public void setMessageSourceService(MessageSourceService messageSourceService) {
        this.messageSourceService = messageSourceService;
    }

    @Override
    public String generateLabel(Patient patient) {

        if (patient.getPersonName() == null) {
            throw new IllegalArgumentException("Patient needs to have at least one name");
        }

        PatientIdentifier primaryIdentifier = patient.getPatientIdentifier(emrApiProperties.getPrimaryIdentifierType());

        if (primaryIdentifier == null) {
            throw new IllegalArgumentException("No primary identifier for this patient");
        }

        // TODO: potentially pull this formatting code into a configurable template?
        // build the command to send to the printer -- written in ZPL
        StringBuilder data = new StringBuilder();
        data.append("^XA");
        data.append("^CI28");   // specify Unicode encoding

         /* Name (Only print first and last name) */
        if (patient.getPersonName() != null) {
            data.append("^FO100,40^AUN^FD" + (patient.getPersonName().getGivenName() != null ? patient.getPersonName().getGivenName() : "") + " "
                    + (patient.getPersonName().getFamilyName() != null ? patient.getPersonName().getFamilyName() : "") + "^FS");
        }

        /* Primary identifier */
        data.append("^FO480,40^FB520,1,0,R,0^AUN^FD" + primaryIdentifier.getIdentifier() + "^FS");

        List<PatientIdentifier> paperRecordIdentifiers = patient.getPatientIdentifiers(paperRecordProperties.getPaperRecordIdentifierType());

        /* Print patient record identifiers in two columns*/
        if (paperRecordIdentifiers != null && paperRecordIdentifiers.size() > 0) {
            int verticalPosition = 110;
            int horizontalPosition = 100;
            int count = 0;

            for (PatientIdentifier identifier : paperRecordIdentifiers) {
                data.append("^FO" + horizontalPosition + "," + verticalPosition + "^AUN^FD" + identifier.getIdentifier() + "^FS");
                if (identifier.getLocation() != null) {
                    data.append("^FO" + horizontalPosition + "," + (verticalPosition + 50) + "^ATN^FD" + identifier.getLocation().getName() + " "
                            + messageSourceService.getMessage("emr.archivesRoom.recordNumber.label") + "^FS");
                }
                verticalPosition = verticalPosition + 100;
                count++;

                // switch to second column if needed
                if (verticalPosition == 410) {
                    verticalPosition = 110;
                    horizontalPosition = 550;
                }

                // we can't fit more than 6 dossier numbers on a label--this is a real edge case
                if (count > 6) {
                    break;
                }
            }
        }

        /* Draw the "tear line" */
        data.append("^FO1025,10^GB0,590,10^FS");

        /* Print command */
        data.append("^XZ");

        return data.toString();
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }

}

