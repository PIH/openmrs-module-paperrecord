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

    // TODO: implement these!

    @Override
    public List<PaperRecord> findPaperRecords(Patient patient, Location paperRecordLocation) {

        Criteria criteria = createPaperRecordCriteria();

        if (patient != null) {
            addPatientRestriction(criteria, patient);
        }

        if (paperRecordLocation != null) {
            addRecordLocationRestriction(criteria, paperRecordLocation);
        }

        return (List<PaperRecord>)  criteria.list();
    }


    @Override
    public PaperRecord findPaperRecord(PatientIdentifier paperRecordIdentifier, Location paperRecordLocation) {

        Criteria criteria = createPaperRecordCriteria();

        if (paperRecordIdentifier != null) {
            addPatientIdentifierRestriction(criteria, paperRecordIdentifier);
        }

        if (paperRecordLocation != null) {
            addRecordLocationRestriction(criteria, paperRecordLocation);
        }

        // TODO: how do we assure that duplicate records don't get created?
        return (PaperRecord)  criteria.uniqueResult();
    }


    private Criteria createPaperRecordCriteria() {
        return sessionFactory.getCurrentSession().createCriteria(PaperRecord.class);
    }


    private void addPatientRestriction(Criteria criteria, Patient patient) {
        criteria.createCriteria("patientIdentifier", "pi").add(Restrictions.eq("pi.patient", patient));
    }

    private void addPatientIdentifierRestriction(Criteria criteria, PatientIdentifier patientIdentifier) {
        criteria.add(Restrictions.eq("patientIdentifier", patientIdentifier));
    }

    private void addRecordLocationRestriction(Criteria criteria, Location paperRecordLocation) {
        criteria.add(Restrictions.eq("recordLocation", paperRecordLocation));
    }

}
