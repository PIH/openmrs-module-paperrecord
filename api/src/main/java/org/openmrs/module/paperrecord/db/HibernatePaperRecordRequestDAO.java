/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.paperrecord.db;

import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.db.HibernateSingleClassDAO;
import org.openmrs.module.paperrecord.PaperRecord;
import org.openmrs.module.paperrecord.PaperRecordRequest;

import java.util.List;

public class HibernatePaperRecordRequestDAO extends HibernateSingleClassDAO<PaperRecordRequest> implements PaperRecordRequestDAO {

    public HibernatePaperRecordRequestDAO() {
        super(PaperRecordRequest.class);
    }

    @Override
    public List<PaperRecordRequest> findPaperRecordRequests(List<PaperRecordRequest.Status> statusList, Patient patient, Location recordLocation, String identifier) {

        Criteria criteria = createPaperRecordRequestCriteria();

        // only add the aliases/joins if necessary
        if (patient != null || identifier !=null || recordLocation != null) {
            addAliases(criteria);
        }

        if (statusList != null) {
            addStatusDisjunctionRestriction(criteria, statusList);
        }

        if (patient != null) {
            addPatientRestriction(criteria, patient);
        }

        if (recordLocation != null) {
            addRecordLocationRestriction(criteria, recordLocation);
        }

        if (identifier != null) {
            addIdentifierRestriction(criteria, identifier);
        }

        addOrderByDateCreated(criteria);

        return (List<PaperRecordRequest>) criteria.list();
    }

    @Override
    public List<PaperRecordRequest> findPaperRecordRequests(List<PaperRecordRequest.Status> statusList, PaperRecord paperRecord) {

        Criteria criteria = createPaperRecordRequestCriteria();

        if (statusList != null) {
            addStatusDisjunctionRestriction(criteria, statusList);
        }

        if (paperRecord != null) {
            addPaperRecordRestriction(criteria, paperRecord);
        }

        return (List<PaperRecordRequest>) criteria.list();
    }

    private Criteria createPaperRecordRequestCriteria() {
        return sessionFactory.getCurrentSession().createCriteria(PaperRecordRequest.class);

    }

    private void addAliases(Criteria criteria) {
        criteria.createAlias("paperRecord", "pr")
                .createAlias("pr.patientIdentifier", "pi");
    }

    private void addStatusDisjunctionRestriction(Criteria criteria, List<PaperRecordRequest.Status> statusList) {

        if (statusList.size() == 1) {
            criteria.add(Restrictions.eq("status", statusList.get(0)));
        } else {
            Disjunction statusDisjunction = Restrictions.disjunction();

            for (PaperRecordRequest.Status status : statusList) {
                statusDisjunction.add(Restrictions.eq("status", status));
            }

            criteria.add(statusDisjunction);
        }
    }

    private void addPatientRestriction(Criteria criteria, Patient patient) {
        criteria.add(Restrictions.eq("pi.patient", patient));
    }

    private void addRecordLocationRestriction(Criteria criteria, Location recordLocation) {
        criteria.add(Restrictions.eq("pr.recordLocation", recordLocation));

    }

    private void addPaperRecordRestriction(Criteria criteria, PaperRecord paperRecord) {
        criteria.add(Restrictions.eq("paperRecord", paperRecord));
    }


    private void addIdentifierRestriction(Criteria criteria, String identifier) {
        criteria.add(Restrictions.eq("pi.identifier", identifier));
    }

    private void addOrderByDateCreated(Criteria criteria) {
        criteria.addOrder(Order.asc("dateCreated"));
    }

}
