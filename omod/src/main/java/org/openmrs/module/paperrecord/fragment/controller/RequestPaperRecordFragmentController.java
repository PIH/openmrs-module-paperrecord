package org.openmrs.module.paperrecord.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.paperrecord.PaperRecord;
import org.openmrs.module.paperrecord.PaperRecordConstants;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.openmrs.module.paperrecord.UnableToPrintLabelException;
import org.openmrs.module.printer.Printer;
import org.openmrs.module.printer.PrinterService;
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

    /**
     * Assigned a dossier number, and then prints out paper record label(s) and an ID card label at the specified location
     *
     * @param ui
     * @param patient
     * @param location
     * @param service
     * @param printerService
     * @param uiSessionContext
     * @return
     * @throws UnableToPrintLabelException
     */
    public SimpleObject createPaperRecord(UiUtils ui,
                                            @RequestParam("patientId") Patient patient,
                                            @RequestParam("locationId") Location location,
                                            @SpringBean("paperRecordService") PaperRecordService service,
                                            @SpringBean("printerService") PrinterService printerService,
                                            UiSessionContext uiSessionContext) throws UnableToPrintLabelException {

        if (service.paperRecordExistsForPatient(patient, location)) {
            // TODO: return error message "please place a request?"
            return null;
        }

        PaperRecord paperRecord = service.createPaperRecord(patient, location);

        try {
            service.printPaperRecordLabels(patient, location, 1);       // label for the paper record itself
            service.printPaperFormLabels(patient, location, PaperRecordConstants.NUMBER_OF_FORM_LABELS_TO_PRINT);    // labels for individual paper forms
            service.printIdCardLabel(patient, location);
         } catch (UnableToPrintLabelException e) {
            log.warn("User " + uiSessionContext.getCurrentUser() + " unable to print paper record label at location "
                    + uiSessionContext.getSessionLocation(), e);
            return SimpleObject.create("success", false, "message", ui.message("paperrecord.archivesRoom.error.unableToPrintLabel"));
        }

        // mark this record as "ACTIVE" (ie, it has been created)
        paperRecord.updateStatus(PaperRecord.Status.ACTIVE);
        service.savePaperRecord(paperRecord);

        Printer printer = printerService.getDefaultPrinter(location, Printer.Type.LABEL);
        return SimpleObject.create("success", true, "message", ui.message("paperrecord.patientDashBoard.printLabels.successMessage") + " " + printer.getPhysicalLocation().getName());

    }



    public SimpleObject printPaperRecordLabel(UiUtils ui,
                                              @RequestParam("patientId") Patient patient,
                                              @RequestParam("locationId") Location location,
                                              @SpringBean("paperRecordService") PaperRecordService service,
                                              @SpringBean("printerService") PrinterService printerService,
                                              UiSessionContext uiSessionContext) throws UnableToPrintLabelException {

        // TODO: add assure paper medical record number

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

    public SimpleObject printPaperFormLabel(UiUtils ui,
                                              @RequestParam("patientId") Patient patient,
                                              @RequestParam("locationId") Location location,
                                              @SpringBean("paperRecordService") PaperRecordService service,
                                              @SpringBean("printerService") PrinterService printerService,
                                              UiSessionContext uiSessionContext) throws UnableToPrintLabelException {

        // TODO: add assure paper medical record number

        try {
            service.printPaperFormLabels(patient, location, 1);     // we print one label by default
            Printer printer = printerService.getDefaultPrinter(location, Printer.Type.LABEL);

            return SimpleObject.create("success", true, "message", ui.message("paperrecord.patientDashBoard.printLabels.successMessage") + " " + printer.getPhysicalLocation().getName());
        } catch (UnableToPrintLabelException e) {
            log.warn("User " + uiSessionContext.getCurrentUser() + " unable to print paper form label at location "
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

        // TODO: add assure paper medical record number (?)

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
