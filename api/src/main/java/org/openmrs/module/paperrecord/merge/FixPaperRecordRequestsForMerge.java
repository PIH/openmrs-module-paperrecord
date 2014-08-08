package org.openmrs.module.paperrecord.merge;

import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.emrapi.merge.PatientMergeAction;
import org.openmrs.module.paperrecord.PaperRecordProperties;
import org.openmrs.module.paperrecord.PaperRecordRequest;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("fixPaperRecordRequestsForMerge")
public class FixPaperRecordRequestsForMerge implements PatientMergeAction {

    @Autowired
    PaperRecordService paperRecordService;

    @Autowired
    PaperRecordProperties paperRecordProperties;

    public void setPaperRecordService(PaperRecordService paperRecordService) {
        this.paperRecordService = paperRecordService;
    }

    public void setPaperRecordProperties(PaperRecordProperties paperRecordProperties) {
        this.paperRecordProperties = paperRecordProperties;
    }

    @Override
    public void beforeMergingPatients(Patient preferred, Patient notPreferred) {
        List<PaperRecordRequest> requestsForPreferred = paperRecordService.getPaperRecordRequestsByPatient(preferred);
        List<PaperRecordRequest> requestsForNotPreferred = paperRecordService.getPaperRecordRequestsByPatient(notPreferred);
        List<PaperRecordRequest> allRequests = ListUtils.union(requestsForPreferred, requestsForNotPreferred);

        // for now, we will just cancel any pending paper record requests for the preferred patient and non-preferred patient
        for (PaperRecordRequest request : allRequests) {
            if (PaperRecordRequest.PENDING_STATUSES.contains(request.getStatus())) {
                paperRecordService.markPaperRecordRequestAsCancelled(request);
            }
        }

        // also copy over all the non-preferred patient requests to the new patient
        for (PaperRecordRequest request : requestsForNotPreferred) {
            request.setPatient(preferred);
            paperRecordService.savePaperRecordRequest(request);
        }

        // see if we need to create any requests to merge paper records (look for paper record identifiers at the same location)
        List<PatientIdentifier> preferredPaperRecordIdentifiers = preferred.getPatientIdentifiers(paperRecordProperties.getPaperRecordIdentifierType());
        List<PatientIdentifier> notPreferredPaperRecordIdentifiers = notPreferred.getPatientIdentifiers(paperRecordProperties.getPaperRecordIdentifierType());

        for (PatientIdentifier preferredPaperRecordIdentifier : preferredPaperRecordIdentifiers) {
            for (PatientIdentifier notPreferredPaperRecordIdentifier : notPreferredPaperRecordIdentifiers) {
                if (preferredPaperRecordIdentifier.getLocation().equals(notPreferredPaperRecordIdentifier.getLocation())) {
                    paperRecordService.markPaperRecordsForMerge(preferredPaperRecordIdentifier, notPreferredPaperRecordIdentifier);
                }
            }
        }

        // TODO: this code is potential strategy #1 for improved handling of merging... we reissue cancelled requests...
        // TODO: this would be done after cancelling all the requests

        /*// now that the records have been marked for merge (and identifiers have been combined) reissue the cancelled requests, using the preferred patient
        // (note that if there happened to be pending requests for both patients at the record location, we will only end up with one request)
        for (PaperRecordRequest request : recordRequestsToReissue) {
            paperRecordService.requestPaperRecord(preferred, request.getRecordLocation(), request.getRequestLocation());
        }
*/

        // TODO: this code is potential strategy #2 for improved handling of pending paper record requests when merging two patient records
        // TODO: this would be done in lieu of cancelling all the requests

        // see if we have pending requests for both records

     /*   if (preferredRecordRequests.size() > 0 && notPreferredRecordRequests.size() > 0) {
            if (!cancelOpenCreateRequest(notPreferredRecordRequests)) {
               cancelOpenCreateRequest(preferredRecordRequests);
            }
        }

        PatientIdentifier preferredPatientIdentifier = preferred.getPatientIdentifier(
            emrApiProperties.getPaperRecordIdentifierType());
        PatientIdentifier notPreferredPatientIdentifier = notPreferred.getPatientIdentifier(
            emrApiProperties.getPaperRecordIdentifierType());

        if (notPreferredPatientIdentifier != null && preferredPatientIdentifier == null) {
            for (PaperRecordRequest request : preferredRecordRequests) {
                request.setIdentifier(notPreferredPatientIdentifier.getIdentifier());
                paperRecordService.savePaperRecordRequest(request);
            }
        }

        boolean updateNotPreferredIdentifier = notPreferredPatientIdentifier == null && preferredPatientIdentifier != null;
        // copy over any existing paper record requests to the preferred patient
        for(PaperRecordRequest request : notPreferredRecordRequests){
            request.setPatient(preferred);
            if (updateNotPreferredIdentifier) {
                request.setIdentifier(preferredPatientIdentifier.getIdentifier());
            }
            paperRecordService.savePaperRecordRequest(request);
        }
*/

    }

    // TODO: I had to comment all of this out, fix...
    private boolean cancelOpenCreateRequest(List<PaperRecordRequest> requests) {
   //     for (PaperRecordRequest request : requests) {
    //        if (request.getStatus() == PaperRecordRequest.Status.OPEN && request.getIdentifier() == null) {
   //             paperRecordService.markPaperRecordRequestAsCancelled(request);
    //            return true;
     //       }
    //    }
        return false;
    }

    @Override
    public void afterMergingPatients(Patient preferred, Patient notPreferred) {
        // do nothing
    }

}
