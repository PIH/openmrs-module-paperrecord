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

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CloseStalePullRequestsTaskComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private PaperRecordService paperRecordService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PersonService personService;

    @Autowired
    private PatientService patientService;

    @Before
    public void beforeAllTests() throws Exception {
        executeDataSet("paperRecordTestDataset.xml");
    }

    @Test
    public void shouldClosePullRequestsOverTwelveHoursOld() {
        // some data from standard test dataset
        Person person = personService.getPerson(3);
        Location recordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(2);

        // from the custom data set
        PatientIdentifier identifier = patientService.getPatientIdentifier(2001);

        Date now = new Date();

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(identifier);
        paperRecord.setRecordLocation(recordLocation);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);
        paperRecordService.savePaperRecord(paperRecord);

        PaperRecordRequest beforeExpireDate = new PaperRecordRequest();
        beforeExpireDate.setPaperRecord(paperRecord);
        beforeExpireDate.setRequestLocation(requestLocation);
        beforeExpireDate.setAssignee(person);
        beforeExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED);
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        beforeExpireDate.setDateCreated(DateUtils.addHours(now, -13));
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        PaperRecordRequest afterExpireDate = new PaperRecordRequest();
        afterExpireDate.setPaperRecord(paperRecord);
        afterExpireDate.setRequestLocation(requestLocation);
        afterExpireDate.setAssignee(person);
        afterExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED);
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        afterExpireDate.setDateCreated(DateUtils.addHours(now, -11));
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // sanity check
        assertThat(paperRecordService.getAssignedPaperRecordRequestsToPull().size(), is(2));

        // now test the scheduler
        CloseStalePullRequestsTask closeStalePullRequestsTask = new CloseStalePullRequestsTask();
        closeStalePullRequestsTask.initialize(new TaskDefinition());
        closeStalePullRequestsTask.execute();

        List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToPull();
        assertThat(requests.size(), is(1));
        assertThat(requests.get(0), is(afterExpireDate));
    }

    @Test
    public void shouldClosePullRequestsBasedOnCustomExpireHours() {

        // some data from standard test dataset
        Person person = personService.getPerson(3);
        Location recordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(2);

        // from the custom data set
        PatientIdentifier identifier = patientService.getPatientIdentifier(2001);

        Date now = new Date();

        PaperRecord paperRecord = new PaperRecord();
        paperRecord.setPatientIdentifier(identifier);
        paperRecord.setRecordLocation(recordLocation);
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);
        paperRecordService.savePaperRecord(paperRecord);

        PaperRecordRequest beforeExpireDate = new PaperRecordRequest();
        beforeExpireDate.setPaperRecord(paperRecord);
        beforeExpireDate.setRequestLocation(requestLocation);
        beforeExpireDate.setAssignee(person);
        beforeExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED);
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        beforeExpireDate.setDateCreated(DateUtils.addHours(now, -8));
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        PaperRecordRequest afterExpireDate = new PaperRecordRequest();
        afterExpireDate.setPaperRecord(paperRecord);
        afterExpireDate.setRequestLocation(requestLocation);
        afterExpireDate.setAssignee(person);
        afterExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED);
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        afterExpireDate.setDateCreated(DateUtils.addHours(now, -6));
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // sanity check
        assertThat(paperRecordService.getAssignedPaperRecordRequestsToPull().size(), is(2));

        // now test the scheduler
        CloseStalePullRequestsTask closeStalePullRequestsTask = new CloseStalePullRequestsTask();

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProperty("pullRecordExpireHours", "7");
        closeStalePullRequestsTask.initialize(taskDefinition);

        closeStalePullRequestsTask.execute();

        List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToPull();
        assertThat(requests.size(), is(1));
        assertThat(requests.get(0), is(afterExpireDate));
    }

}
