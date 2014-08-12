package org.openmrs.module.paperrecord.db;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.emrapi.db.SingleClassDAO;
import org.openmrs.module.paperrecord.PaperRecord;

import java.util.List;

public interface PaperRecordDAO extends SingleClassDAO<PaperRecord> {

    List<PaperRecord> findPaperRecords(Patient patient, Location paperRecordLocation);

    PaperRecord findPaperRecord(PatientIdentifier paperRecordIdentifier, Location paperRecordLocation);

    PaperRecord findPaperRecord(String paperRecordIdentifier, Location paperRecordLocation);
}
