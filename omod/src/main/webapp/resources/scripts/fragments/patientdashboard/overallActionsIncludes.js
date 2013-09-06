var requestPaperRecordDialog = null;

function showRequestChartDialog () {
    requestPaperRecordDialog.show();
    return false;
}

function createPaperRecordDialog(patientId) {
    requestPaperRecordDialog = emr.setupConfirmationDialog({
        selector: '#request-paper-record-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('paperrecord', 'requestPaperRecord', 'requestPaperRecord'
                    , { patientId: patientId, locationId: sessionLocationModel.id() }
                    , function(data) {
                        emr.successMessage(data.message);
                        requestPaperRecordDialog.close();
                    });
            },
            cancel: function() {
                requestPaperRecordDialog.close();
            }
        }
    });
}

function printIdCardLabel() {
    emr.getFragmentActionWithCallback('paperrecord', 'requestPaperRecord', 'printIdCardLabel'
        , { patientId: patient.id, locationId: sessionLocationModel.id() }
        , function(data) {
            if(data.success) {
                emr.successMessage(data.message);
            } else {
                emr.errorMessage(data.message);
            }
        }
    );
}

function printPaperRecordLabel() {
    emr.getFragmentActionWithCallback('paperrecord', 'requestPaperRecord', 'printPaperRecordLabel'
        , { patientId: patient.id, locationId: sessionLocationModel.id() }
        , function(data) {
            if(data.success) {
                emr.successMessage(data.message);
            } else {
                emr.errorMessage(data.message);
            }
        }
    );
}