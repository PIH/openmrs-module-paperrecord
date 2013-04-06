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
import org.openmrs.module.emrapi.db.HibernateSingleClassDAO;
import org.openmrs.module.paperrecord.PaperRecordMergeRequest;

import java.util.List;

public class HibernatePaperRecordMergeRequestDAO extends HibernateSingleClassDAO<PaperRecordMergeRequest> implements PaperRecordMergeRequestDAO {

    public HibernatePaperRecordMergeRequestDAO() {
        super(PaperRecordMergeRequest.class);
    }

    @Override
    public List<PaperRecordMergeRequest> findPaperRecordMergeRequest(List<PaperRecordMergeRequest.Status> statusList) {

        Criteria criteria = createPaperRecordRequestCriteria();

        if (statusList != null) {
            addStatusDisjunctionRestriction(criteria, statusList);
        }

        addOrderByDateCreated(criteria);

        return (List<PaperRecordMergeRequest>) criteria.list();
    }

    private Criteria createPaperRecordRequestCriteria() {
        return sessionFactory.getCurrentSession().createCriteria(PaperRecordMergeRequest.class);
    }

    private void addStatusDisjunctionRestriction(Criteria criteria, List<PaperRecordMergeRequest.Status> statusList) {

        if (statusList.size() == 1) {
            criteria.add(Restrictions.eq("status", statusList.get(0)));
        } else {
            Disjunction statusDisjunction = Restrictions.disjunction();

            for (PaperRecordMergeRequest.Status status : statusList) {
                statusDisjunction.add(Restrictions.eq("status", status));
            }

            criteria.add(statusDisjunction);
        }
    }

    private void addOrderByDateCreated(Criteria criteria) {
        criteria.addOrder(Order.asc("dateCreated"));
    }


}
