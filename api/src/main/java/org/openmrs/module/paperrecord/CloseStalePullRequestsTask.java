package org.openmrs.module.paperrecord;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Date;

/**
 * Task that can be scheduled to close out any pending (open or assigned) pull requests with a date
 * created that is more than pullRecordExpireHours old (default is to close out requests more than twelve
 * hours old).
 * <p/>
 * Note that this task is not scheduled by default, but can be scheduled in a customization module (see the
 * Mirebalais module for an example of this)
 */

public class CloseStalePullRequestsTask extends AbstractTask {

    private static Integer DEFAULT_PULL_RECORD_EXPIRE_HOURS = 12;

    @Override
    public void execute() {

        Integer pullRecordExpireHours = DEFAULT_PULL_RECORD_EXPIRE_HOURS;

        if (taskDefinition.getProperty("pullRecordExpireHours") != null
                && StringUtils.isNotBlank(taskDefinition.getProperty("pullRecordExpireHours"))) {
            pullRecordExpireHours = Integer.valueOf(taskDefinition.getProperty("pullRecordExpireHours"));
        }

        Date expireDate = DateUtils.addHours(new Date(), -pullRecordExpireHours);

        Context.getService(PaperRecordService.class).expirePendingPullRequests(expireDate);
    }
}
