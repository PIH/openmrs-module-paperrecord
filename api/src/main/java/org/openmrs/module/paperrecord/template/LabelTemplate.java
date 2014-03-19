package org.openmrs.module.paperrecord.template;

import org.openmrs.Patient;

public interface LabelTemplate {

    public final static Integer LABEL_PRINTER_LINE_MAX_SIZE = 25;
    public final static Integer CHARTLABEL_PRINTER_LINE_MAX_SIZE = 23;
    public final static Integer CHARTLABEL_LOWER_FONT_PRINTER_LINE_MAX_SIZE = 30;

    String generateLabel(Patient patient, String paperRecordIdentifier);

    String getEncoding();

}
