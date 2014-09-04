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

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IsExpectedRequest extends ArgumentMatcher<PaperRecordRequest> {

    private PaperRecordRequest expectedRequest;

    public IsExpectedRequest(PaperRecordRequest expectedRequest) {
        this.expectedRequest = expectedRequest;
    }

    @Override
    public boolean matches(Object o) {

        PaperRecordRequest actualRequest = (PaperRecordRequest) o;

        assertThat(actualRequest.getId(), is(expectedRequest.getId()));
        assertThat(actualRequest.getAssignee(), is(expectedRequest.getAssignee()));
        assertThat(actualRequest.getCreator(), is(expectedRequest.getCreator()));
        assertThat(actualRequest.getStatus(), is(expectedRequest.getStatus()));
        assertThat(actualRequest.getPaperRecord().getPatientIdentifier().getIdentifier(), is(expectedRequest.getPaperRecord().getPatientIdentifier().getIdentifier()));
        if (expectedRequest.getDateCreated() != null) {
            assertThat(actualRequest.getDateCreated(), notNullValue());
        }

        return true;
    }

}
