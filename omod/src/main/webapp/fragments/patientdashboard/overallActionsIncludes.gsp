<%
    ui.includeJavascript("paperrecord", "fragments/patientdashboard/overallActionsIncludes.js")
%>

<script type="text/javascript">
    jq(function(){
        createPaperRecordDialog(${patient.id});
        ko.applyBindings( sessionLocationModel, jq('#request-paper-record-dialog').get(0) );
    });
</script>

<div id="request-paper-record-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-folder-open"></i>
        <h3>${ ui.message("coreapps.patientDashBoard.requestPaperRecord.title") }</h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("coreapps.patientDashBoard.requestPaperRecord.confirmTitle") }</p>
        <ul>
            <li class="info">
                <span>${ ui.message("coreapps.patient") }</span>
                <h5>${ ui.format(patient.patient) }</h5>
            </li>
            <li class="info">
                <span>${ ui.message("coreapps.location") }</span>
                <h5 data-bind="text: text"></h5>
            </li>
        </ul>

        <button class="confirm right">${ ui.message("coreapps.yes") }</button>
        <button class="cancel">${ ui.message("coreapps.no") }</button>
    </div>
</div>