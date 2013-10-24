package org.openmrs.module.paperrecord;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Date;

/**
 * Task that can be scheduled to close out any pending (open or assigned) create requests with a date
 * created that is more than createRecordExpireHours old (default is to close out requests more than forty-eight
 * hours old).
 * <p/>
 * Note that this task is not scheduled by default, but can be scheduled in a customization module (see the
 * Mirebalais module for an example of this)
 */
public class CloseStaleCreateRequestsTask extends AbstractTask {

    private static Integer DEFAULT_CREATE_RECORD_EXPIRE_HOURS = 48;

    @Override
    public void execute() {

        Integer createRecordExpireHours = DEFAULT_CREATE_RECORD_EXPIRE_HOURS;

        if (taskDefinition.getProperty("createRecordExpireHours") != null
                && StringUtils.isNotBlank(taskDefinition.getProperty("createRecordExpireHours"))) {
            createRecordExpireHours = Integer.valueOf(taskDefinition.getProperty("createRecordExpireHours"));
        }

        Date expireDate = DateUtils.addHours(new Date(), -createRecordExpireHours);

        Context.getService(PaperRecordService.class).expirePendingCreateRequests(expireDate);
    }


}
