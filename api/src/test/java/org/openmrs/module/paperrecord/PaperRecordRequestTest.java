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

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class PaperRecordRequestTest {

    @Test
    public void testUpdateStatusShouldUpdateStatusLastUpdated() throws InterruptedException {

        PaperRecordRequest request = new PaperRecordRequest();

        request.updateStatus(PaperRecordRequest.Status.OPEN);
        Date date = request.getDateStatusChanged();
        Assert.assertNotNull(date);

        Thread.sleep(1);

        request.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_CREATE);
        Date updatedDate = request.getDateStatusChanged();
        Assert.assertTrue(updatedDate.after(date));

    }

}
