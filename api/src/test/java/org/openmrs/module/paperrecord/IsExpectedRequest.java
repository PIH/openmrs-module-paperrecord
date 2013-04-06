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

package org.openmrs.module.paperrecord;

import org.mockito.ArgumentMatcher;
import org.openmrs.Patient;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IsExpectedRequest extends ArgumentMatcher<PaperRecordRequest> {

    private PaperRecordRequest expectedRequest;

    public IsExpectedRequest(PaperRecordRequest expectedRequest) {
        this.expectedRequest = expectedRequest;
    }

    public IsExpectedRequest(Patient patient, PaperRecordRequest.Status status, String identifier) {
        expectedRequest = new PaperRecordRequest();
        expectedRequest.setPatient(patient);
        expectedRequest.setIdentifier(identifier);
        expectedRequest.updateStatus(status);
    }

    @Override
    public boolean matches(Object o) {

        PaperRecordRequest actualRequest = (PaperRecordRequest) o;

        assertThat(actualRequest.getId(), is(expectedRequest.getId()));
        assertThat(actualRequest.getAssignee(), is(expectedRequest.getAssignee()));
        assertThat(actualRequest.getCreator(), is(expectedRequest.getCreator()));
        assertThat(actualRequest.getIdentifier(), is(expectedRequest.getIdentifier()));
        assertThat(actualRequest.getRecordLocation(), is(expectedRequest.getRecordLocation()));
        assertThat(actualRequest.getPatient(), is(expectedRequest.getPatient()));
        assertThat(actualRequest.getStatus(), is(expectedRequest.getStatus()));
        if (expectedRequest.getDateCreated() != null) {
            assertThat(actualRequest.getDateCreated(), notNullValue());
        }

        return true;
    }

}
