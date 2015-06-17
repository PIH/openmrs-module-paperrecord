package org.openmrs.module.paperrecord.page.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.paperrecord.PaperRecord;
import org.openmrs.module.paperrecord.PaperRecordConstants;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class RequestPaperRecordPageController {

    static Predicate ANY_NOT_PENDING_CREATION = new Predicate() {
        @Override
        public boolean evaluate(Object o) {
            return !o.equals(PaperRecord.Status.PENDING_CREATION);
        }
    };

    public Redirect controller(@RequestParam("patientId") Patient patient,
                           @RequestParam(value = "returnProvider") String returnProvider,
                           @RequestParam(value = "returnPage") String returnPage,
                           @SpringBean("paperRecordService") PaperRecordService paperRecordService,
                           PageModel pageModel,
                           UiSessionContext uiSessionContext) {

        Location currentLocation = uiSessionContext.getSessionLocation();

        if (paperRecordService.locationHasAssociatedMedicalRecordLocation(currentLocation)) {

            List<PaperRecord> paperRecords = paperRecordService.getPaperRecords(patient, currentLocation);

            // only show the create paper record dialog if the patient does *not* have an existing record that is in some other state than pending creation
            // and we are not currently at an archives location
            boolean needToCreateRecord = !currentLocation.hasTag(PaperRecordConstants.LOCATION_TAG_ARCHIVES_LOCATION) &&
                    (paperRecords == null || paperRecords.size() == 0 || !CollectionUtils.exists(paperRecords, ANY_NOT_PENDING_CREATION));

            pageModel.addAttribute("patient", patient);
            pageModel.addAttribute("needToCreateRecord", needToCreateRecord);
            pageModel.addAttribute("returnProvider", returnProvider);
            pageModel.addAttribute("returnPage",returnPage);

            if (needToCreateRecord) {
                pageModel.addAttribute("associatedArchivesLocation", paperRecordService.getArchivesLocationAssociatedWith(currentLocation));
            }

            return null;

        }
        else {
            // if this location does not have an associated medical record location, do nothing, just redirect to return url
            return new Redirect(returnProvider, returnPage, "patientId=" + patient.getId());
        }

    }

}
