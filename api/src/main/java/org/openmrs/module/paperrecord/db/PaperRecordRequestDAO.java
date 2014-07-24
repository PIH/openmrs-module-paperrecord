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

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.db.SingleClassDAO;
import org.openmrs.module.paperrecord.PaperRecordRequest;

import java.util.List;

public interface PaperRecordRequestDAO extends SingleClassDAO<PaperRecordRequest> {

    /**
     * Returns all the paper record requests for the given patient and given location with ANY of the specified statuses
     *
     * @param statusList
     * @param patient
     * @param recordLocation
     * @param identifier
     * @param hasIdentifier  restricts based on whether or not the identifier field null
     * @return the paper record requests for the given patient and given record location with ANY of the specified statuses
     */
    List<PaperRecordRequest> findPaperRecordRequests(List<PaperRecordRequest.Status> statusList, Patient patient,
                                                     Location recordLocation, String identifier, Boolean hasIdentifier);

}
