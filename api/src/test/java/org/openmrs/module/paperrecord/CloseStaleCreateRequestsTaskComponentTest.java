package org.openmrs.module.paperrecord;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CloseStaleCreateRequestsTaskComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private PaperRecordService paperRecordService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PersonService personService;

    @Before
    public void beforeAllTests() throws Exception {
        executeDataSet("paperRecordServiceComponentTestDataset.xml");
    }

    @Test
    public void shouldCloseCreateRequestsOverFortyEightOld() {
        // some data from standard test dataset
        Person person = personService.getPerson(3);
        Location recordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(2);

        Date now = new Date();

        PaperRecordRequest beforeExpireDate = new PaperRecordRequest();
        beforeExpireDate.setIdentifier("ABC");
        beforeExpireDate.setRequestLocation(requestLocation);
        beforeExpireDate.setRecordLocation(recordLocation);
        beforeExpireDate.setAssignee(person);
        beforeExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_CREATE);
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        beforeExpireDate.setDateCreated(DateUtils.addHours(now, -49));
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        PaperRecordRequest afterExpireDate = new PaperRecordRequest();
        afterExpireDate.setIdentifier("DEF");
        afterExpireDate.setRequestLocation(requestLocation);
        afterExpireDate.setRecordLocation(recordLocation);
        afterExpireDate.setAssignee(person);
        afterExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_CREATE);
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        afterExpireDate.setDateCreated(DateUtils.addHours(now, -47));
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // sanity check
        assertThat(paperRecordService.getAssignedPaperRecordRequestsToCreate().size(), is(2));

        // now test the scheduler
        CloseStaleCreateRequestsTask closeStaleCreateRequestsTask = new CloseStaleCreateRequestsTask();
        closeStaleCreateRequestsTask.initialize(new TaskDefinition());
        closeStaleCreateRequestsTask.execute();

        List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToCreate();
        assertThat(requests.size(), is(1));
        assertThat(requests.get(0), is(afterExpireDate));
    }

    @Test
    public void shouldCloseCreateRequestsBasedOnCustomExpireHours() {

        // some data from standard test dataset
        Person person = personService.getPerson(3);
        Location recordLocation = locationService.getLocation(1);
        Location requestLocation = locationService.getLocation(2);

        Date now = new Date();

        PaperRecordRequest beforeExpireDate = new PaperRecordRequest();
        beforeExpireDate.setIdentifier("ABC");
        beforeExpireDate.setRequestLocation(requestLocation);
        beforeExpireDate.setRecordLocation(recordLocation);
        beforeExpireDate.setAssignee(person);
        beforeExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_CREATE);
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        beforeExpireDate.setDateCreated(DateUtils.addHours(now, -8));
        paperRecordService.savePaperRecordRequest(beforeExpireDate);

        PaperRecordRequest afterExpireDate = new PaperRecordRequest();
        afterExpireDate.setIdentifier("DEF");
        afterExpireDate.setRequestLocation(requestLocation);
        afterExpireDate.setRecordLocation(recordLocation);
        afterExpireDate.setAssignee(person);
        afterExpireDate.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_CREATE);
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // change the date created (which we can't do when we first persist it since it is set automatically)
        afterExpireDate.setDateCreated(DateUtils.addHours(now, -6));
        paperRecordService.savePaperRecordRequest(afterExpireDate);

        // sanity check
        assertThat(paperRecordService.getAssignedPaperRecordRequestsToCreate().size(), is(2));

        // now test the scheduler
        CloseStaleCreateRequestsTask closeStaleCreateRequestsTask = new CloseStaleCreateRequestsTask();

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProperty("createRecordExpireHours", "7");
        closeStaleCreateRequestsTask.initialize(taskDefinition);

        closeStaleCreateRequestsTask.execute();

        List<PaperRecordRequest> requests = paperRecordService.getAssignedPaperRecordRequestsToCreate();
        assertThat(requests.size(), is(1));
        assertThat(requests.get(0), is(afterExpireDate));
    }



}
