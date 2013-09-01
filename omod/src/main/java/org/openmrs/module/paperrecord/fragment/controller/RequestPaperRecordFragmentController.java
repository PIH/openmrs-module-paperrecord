package org.openmrs.module.paperrecord.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.printer.Printer;
import org.openmrs.module.emrapi.printer.PrinterService;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.openmrs.module.paperrecord.UnableToPrintLabelException;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
public class RequestPaperRecordFragmentController {

    private final Log log = LogFactory.getLog(getClass());

    public SimpleObject requestPaperRecord(UiUtils ui,
                                           @RequestParam("patientId") Patient patient,
                                           @RequestParam("locationId") Location location,
                                           @SpringBean("paperRecordService") PaperRecordService service) {

        service.requestPaperRecord(patient, location, location);

        return SimpleObject.create("message", ui.message("paperrecord.patientDashBoard.requestPaperRecord.successMessage"));
    }

    public SimpleObject createDossierNumber(UiUtils ui,
                                            @RequestParam("patientId") Patient patient,
                                            @RequestParam("locationId") Location location,
                                            @SpringBean("paperRecordService") PaperRecordService service) {


        service.createPaperMedicalRecordNumber(patient, location);

        return SimpleObject.create("message", ui.message("paperrecord.patientDashBoard.createDossier.successMessage"));
    }

    public SimpleObject printPaperRecordLabel(UiUtils ui,
                                              @RequestParam("patientId") Patient patient,
                                              @RequestParam("locationId") Location location,
                                              @SpringBean("paperRecordService") PaperRecordService service,
                                              @SpringBean("printerService") PrinterService printerService,
                                              UiSessionContext uiSessionContext) throws UnableToPrintLabelException {

        try {
            service.printPaperRecordLabels(patient, location, 1);     // we print one label by default
            Printer printer = printerService.getDefaultPrinter(location, Printer.Type.LABEL);

            return SimpleObject.create("success", true, "message", ui.message("paperrecord.patientDashBoard.printLabels.successMessage") + " " + printer.getPhysicalLocation().getName());
        } catch (UnableToPrintLabelException e) {
            log.warn("User " + uiSessionContext.getCurrentUser() + " unable to print paper record label at location "
                    + uiSessionContext.getSessionLocation(), e);
            return SimpleObject.create("success", false, "message", ui.message("paperrecord.archivesRoom.error.unableToPrintLabel"));
        }
    }

    public SimpleObject printIdCardLabel(UiUtils ui,
                                         @RequestParam("patientId") Patient patient,
                                         @RequestParam("locationId") Location location,
                                         @SpringBean("paperRecordService") PaperRecordService service,
                                         @SpringBean("printerService") PrinterService printerService,
                                         UiSessionContext uiSessionContext) throws UnableToPrintLabelException {

        try {
            service.printIdCardLabel(patient, location);
            Printer printer = printerService.getDefaultPrinter(location, Printer.Type.LABEL);

            return SimpleObject.create("success", true, "message", ui.message("paperrecord.patientDashBoard.printLabels.successMessage") + " " + printer.getPhysicalLocation().getName());
        } catch (UnableToPrintLabelException e) {
            log.warn("User " + uiSessionContext.getCurrentUser() + " unable to print id card label at location "
                    + uiSessionContext.getSessionLocation(), e);
            return SimpleObject.create("success", false, "message", ui.message("paperrecord.archivesRoom.error.unableToPrintLabel"));
        }
    }
}
