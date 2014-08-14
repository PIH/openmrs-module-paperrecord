package org.openmrs.module.paperrecord.db;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.emrapi.db.HibernateSingleClassDAO;
import org.openmrs.module.paperrecord.PaperRecord;

import java.util.List;

public class HibernatePaperRecordDAO  extends HibernateSingleClassDAO<PaperRecord> implements PaperRecordDAO {

    public HibernatePaperRecordDAO() {
        super(PaperRecord.class);
    }

    @Override
    public List<PaperRecord> findPaperRecords(Patient patient, Location paperRecordLocation) {

        Criteria criteria = createPaperRecordCriteria();

        if (patient != null) {
            addPatientRestriction(criteria, patient);
        }

        if (paperRecordLocation != null) {
            addRecordLocationRestriction(criteria, paperRecordLocation);
        }

        addExcludeVoidedRestriction(criteria);

        return (List<PaperRecord>)  criteria.list();
    }


    @Override
    public PaperRecord findPaperRecord(PatientIdentifier paperRecordIdentifier, Location paperRecordLocation) {

        Criteria criteria = createPaperRecordCriteria();

        if (paperRecordIdentifier != null) {
            addPatientIdentifierAsStringRestriction(criteria, paperRecordIdentifier);
        }

        if (paperRecordLocation != null) {
            addRecordLocationRestriction(criteria, paperRecordLocation);
        }

        addExcludeVoidedRestriction(criteria);

        // TODO: since we aren't allowing more than one record per patient identifier, how do we assure that duplicate records don't get created?
        return (PaperRecord)  criteria.uniqueResult();
    }

    @Override
    public PaperRecord findPaperRecord(String paperRecordIdentifier, Location paperRecordLocation) {

        Criteria criteria = createPaperRecordCriteria();

        if (paperRecordIdentifier != null) {
            addPatientIdentifierAsStringRestriction(criteria, paperRecordIdentifier);
        }

        if (paperRecordLocation != null) {
            addRecordLocationRestriction(criteria, paperRecordLocation);
        }

        addExcludeVoidedRestriction(criteria);

        // TODO: since we aren't allowing more than one record per patient identifier, how do we assure that duplicate records don't get created?
        return (PaperRecord)  criteria.uniqueResult();
    }

    private Criteria createPaperRecordCriteria() {
        return sessionFactory.getCurrentSession().createCriteria(PaperRecord.class)
                .createAlias("patientIdentifier", "pi");
    }

    private void addPatientRestriction(Criteria criteria, Patient patient) {
        criteria.add(Restrictions.eq("pi.patient", patient));
    }

    private void addPatientIdentifierAsStringRestriction(Criteria criteria, PatientIdentifier patientIdentifier) {
        criteria.add(Restrictions.eq("patientIdentifier", patientIdentifier));
    }

    private void addPatientIdentifierAsStringRestriction(Criteria criteria, String patientIdentifier) {
        criteria.add(Restrictions.eq("pi.identifier", patientIdentifier));
    }

    private void addRecordLocationRestriction(Criteria criteria, Location paperRecordLocation) {
        criteria.add(Restrictions.eq("recordLocation", paperRecordLocation));
    }

    // note that we tie whether or not a paper record is voided to whether or not the associated patient identifier is voided
    private void addExcludeVoidedRestriction(Criteria criteria) {
        criteria.add(Restrictions.eq("pi.voided", false));
    }
}
