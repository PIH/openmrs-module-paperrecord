package org.openmrs.module.paperrecord.template;

import org.apache.commons.lang.StringUtils;
import org.openmrs.PatientIdentifier;

public class DefaultZplPaperFormLabelTemplate extends DefaultZplPaperRecordLabelTemplate implements PaperFormLabelTemplate {

    // the paper form label template is identical to the paper record label template except that the paper record identifier
    // is smaller on the form label than the record label

    @Override
    protected void generateBarCodeAndIdentifier(StringBuilder data, String paperRecordIdentifier, PatientIdentifier primaryIdentifier) {

         /* Print the patient's paper record identifier, if it exists */
        if (StringUtils.isNotBlank(paperRecordIdentifier)) {

            if (featureToggles.isFeatureEnabled("cdi")) {
                data.append("^FO680,40^FB520,1,0,R,0^AUN^FD"
                        + paperRecordIdentifier.substring(0, paperRecordIdentifier.length() - 6) + " "
                        + paperRecordIdentifier.substring(paperRecordIdentifier.length() - 6) + "^FS");
            }
            else {
                data.append("^FO680,40^FB520,1,0,R,0^AUN^FD" + paperRecordIdentifier + "^FS");
            }

        }

        /* Print the bar code, based on the primary identifier */
        data.append("^FO780,100^ATN^BY4^BCN,150,N^FD" + primaryIdentifier.getIdentifier() + "^FS");    // print barcode & identifier
    }


}
