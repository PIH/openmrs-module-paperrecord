
var pullRequestsViewModel;
var createRequestsViewModel;
var assignedPullRequestsViewModel;
var assignedCreateRequestsViewModel;
var mergeRequestsViewModel;

var cancelPaperRecordRequestDialog = null;

function RecordRequestModel(requestId, patientName, dossierNumber, sendToLocation, timeRequested, timeRequestedSortable, dateLastSent, locationLastSent, selected) {
    var model = {};
    model.requestId = requestId;
    model.patientName = patientName;
    model.dossierNumber = dossierNumber;
    model.sendToLocation = sendToLocation;
    model.timeRequested = timeRequested;
    model.timeRequestedSortable = timeRequestedSortable;
    model.locationLastSent = locationLastSent;
    model.dateLastSent = dateLastSent;
    model.selected = ko.observable(selected === undefined ? false : selected);
    model.hovered = ko.observable(false);
    model.visible = ko.observable(true);

    return model;
}

function MergeRequestsModel(mergeRequestId, preferredName, preferredIdentifier, notPreferredIdentifier, notPreferredName, dateCreated, dateCreatedSortable){
    var model = {};
    model.mergeRequestId = mergeRequestId;
    model.preferredName = preferredName;
    model.preferredIdentifier = preferredIdentifier;
    model.notPreferredIdentifier = notPreferredIdentifier;
    model.notPreferredName = notPreferredName;
    model.dateCreated = dateCreated;
    model.dateCreatedSortable = dateCreatedSortable;

    return model;
}


function PullRequestsViewModel(recordsToPull) {
    var api = {};
    api.recordsToPull = ko.observableArray(recordsToPull);

    api.isValid = function(){
        return api.selectedRequests().length > 0;
    }

    api.selectRecordToBePulled = function(record) {
        record.hovered(false);
        record.selected(!record.selected())
    };

    api.hoverRecords = function(record) {
        record.hovered(true);
    };

    api.unHoverRecords = function(record){
        record.hovered(false);
    }

    api.selectedRequests = ko.computed(function() {
        return jQuery.grep(api.recordsToPull(), function(item) {
            return item.selected();
        });
    });

    api.hoveredRequests = ko.computed(function() {
        return jQuery.grep(api.recordsToPull(), function(item) {
            return item.hovered();
        });
    });

    api.cancelRequest = function (request) {
        openCancelPaperRecordRequestDialog(request);
    }

    api.load = function() {

        // reload via ajax
        jQuery.getJSON(emr.fragmentActionLink("paperrecord", "archivesRoom", "getOpenRecordsToPull"))
            .success(function(data) {

                // fetch all the ids of any existing selected ids (so we can hold selection state)
                var selectedRequestIds = jQuery.map(api.selectedRequests(), function(request) {
                    return request.requestId;
                });

                // remove any existing entries
                api.recordsToPull.removeAll();

                // create the new list
                jQuery.each(data, function(index, request) {
                    api.recordsToPull.push(RecordRequestModel(request.requestId, request.patient,
                        request.identifier, request.requestLocation, request.dateCreated,
                        request.dateCreatedSortable, request.dateLastSent, request.locationLastSent, selectedRequestIds.indexOf(request.requestId) == -1 ? false : true));
                });

            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });

    }

    return api;
}


function CreateRequestsViewModel(recordsToCreate) {
    var api = {};
    api.recordsToCreate = ko.observableArray(recordsToCreate);

    api.selectRecordToBeCreated = function(record) {
        record.hovered(false);
        record.selected(!record.selected());
    };

    api.isValid = function(){
        return api.selectedRequests().length > 0;
    }

    api.hoverRecords = function(record){
        record.hovered(true);
    }

    api.unHoverRecords = function(record){
        record.hovered(false);
    }

    api.selectedRequests = ko.computed(function() {
        return jQuery.grep(api.recordsToCreate(), function(item) {
            return item.selected();
        });
    });

    api.cancelRequest = function (request) {
        openCancelPaperRecordRequestDialog(request);
    }

    api.load = function() {

        // reload via ajax
        jQuery.getJSON(emr.fragmentActionLink("paperrecord", "archivesRoom", "getOpenRecordsToCreate"))
            .success(function(data) {

                // fetch all the ids of any existing selected ids (so we can hold selection state)
                var selectedRequestIds = jQuery.map(api.selectedRequests(), function(request) {
                    return request.requestId;
                });

                // remove any existing entries
                api.recordsToCreate.removeAll();

                // create the new list
                jQuery.each(data, function(index, request) {
                    api.recordsToCreate.push(RecordRequestModel(request.requestId, request.patient,
                        request.identifier, request.requestLocation, request.dateCreated,
                        request.dateCreatedSortable, undefined, undefined, selectedRequestIds.indexOf(request.requestId) == -1 ? false : true));
                });

            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });

    }

    return api;
}

function AssignedPullRequestsViewModel(assignedRecordsToPull) {
    var api = {};
    api.assignedRecordsToPull = ko.observableArray(assignedRecordsToPull);

    api.cancelRequest = function (request) {
        openCancelPaperRecordRequestDialog(request);
    }

    api.load = function() {

        // reload via ajax
        jQuery.getJSON(emr.fragmentActionLink("paperrecord", "archivesRoom", "getAssignedRecordsToPull"))
            .success(function(data) {

                // remove any existing entries
                api.assignedRecordsToPull.removeAll();

                // create the new list
                jQuery.each(data, function(index, request) {
                    api.assignedRecordsToPull.push(RecordRequestModel(request.requestId, request.patient,
                        request.identifier, request.requestLocation, request.dateCreated,
                        request.dateCreatedSortable, request.dateLastSent, request.locationLastSent));
                });

            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });

    }

    api.printLabel = function (request) {

        jQuery.ajax({
            url: emr.fragmentActionLink("paperrecord", "archivesRoom", "printLabel", { requestId: request.requestId }),
            dataType: 'json',
            type: 'POST'
        })
            .success(function(data) {
                emr.successMessage(data.message);
            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });
    }

    return api;
}

function AssignedCreateRequestsViewModel(assignedRecordsToCreate) {
    var api = {};
    api.assignedRecordsToCreate = ko.observableArray(assignedRecordsToCreate);

    api.cancelRequest = function (request) {
        openCancelPaperRecordRequestDialog(request);
    }

    api.load = function() {

        // reload via ajax
        jQuery.getJSON(emr.fragmentActionLink("paperrecord", "archivesRoom", "getAssignedRecordsToCreate"))
            .success(function(data) {

                // remove any existing entries
                api.assignedRecordsToCreate.removeAll();

                // create the new list
                jQuery.each(data, function(index, request) {
                    api.assignedRecordsToCreate.push(RecordRequestModel(request.requestId, request.patient,
                        request.identifier, request.requestLocation, request.dateCreated,
                        request.dateCreatedSortable));
                });

            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });

    }


    api.printPaperRecordLabelSet = function (request) {

        jQuery.ajax({
            url: emr.fragmentActionLink("paperrecord", "archivesRoom", "printPaperRecordLabelSet", { requestId: request.requestId }),
            dataType: 'json',
            type: 'POST'
        })
            .success(function(data) {
                emr.successMessage(data.message);
            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });
    }

    return api;
}

function MergeRequestsViewModel(requestsToMerge){
    var api = {};
    api.requestsToMerge = ko.observableArray(requestsToMerge);

    api.load = function() {

        jQuery.getJSON(emr.fragmentActionLink("paperrecord", "archivesRoom", "getOpenRecordsToMerge"))
            .success(function(data) {

                api.requestsToMerge.removeAll();

                // create the new list
                jQuery.each(data, function(index, request) {
                    api.requestsToMerge.push(MergeRequestsModel(request.mergeRequestId, request.preferredName,
                        request.preferredIdentifier, request.notPreferredIdentifier, request.notPreferredName,
                        request.dateCreated, request.dateCreatedSortable));

                });

            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });

    };

    api.remove = function(id){
        for( var i=0; i < api.requestsToMerge().length; i++) {
            if(api.requestsToMerge()[i].mergeRequestId==id){
                api.requestsToMerge().splice(i,1);
            }
        }
    }

    return api;

}

function refreshAllQueues() {
    pullRequestsViewModel.load();
    createRequestsViewModel.load()
    assignedCreateRequestsViewModel.load();
    assignedPullRequestsViewModel.load();
    mergeRequestsViewModel.load();
}

function openCancelPaperRecordRequestDialog(request) {

    cancelPaperRecordRequestDialog = emr.setupConfirmationDialog({
        selector: '#cancel-paper-record-request-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('paperrecord', 'archivesRoom', 'markPaperRecordRequestAsCancelled', { requestId: request.requestId }, function(data) {
                    request.visible(false);
                    cancelPaperRecordRequestDialog .close();
                });
            },
            cancel: function() {
                cancelPaperRecordRequestDialog.close();
            }
        }
    });

    cancelPaperRecordRequestDialog.show();
    return false;
}

