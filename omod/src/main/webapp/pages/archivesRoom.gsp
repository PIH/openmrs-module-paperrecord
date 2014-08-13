<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("paperrecord", "knockout-2.1.0.js")
    ui.includeJavascript("paperrecord", "recordRequest.js")
    ui.includeJavascript("paperrecord", "archivesRoom.js")

    ui.includeCss("mirebalais", "archivesRoom.css")

%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("paperrecord.app.archivesRoom.label")}"}
    ];
</script>
<div id="tabs" xmlns="http://www.w3.org/1999/html">

<ul>
    <li><a id="tab-selector-create" href="#tab-create">${ ui.message("paperrecord.archivesRoom.newRecords.label") }</a></li>
    <li><a id="tab-selector-pull" href="#tab-pull">${ ui.message("paperrecord.archivesRoom.existingRecords.label") }</a></li>
    <li><a id="tab-selector-return" href="#tab-return">${ ui.message("paperrecord.archivesRoom.returnRecords.label") }</a></li>
    <li><a id="tab-selector-merge" href="#tab-merge">${ ui.message("paperrecord.archivesRoom.mergingRecords.label") }</a></li>
</ul>

<div id="tab-create">
    <span id="createrequest">
        <div class="instructions">
            <span class="instruction">
                <strong>1.</strong>
                <span class="instruction-text">${ ui.message("paperrecord.archivesRoom.selectRecords.label") }</span>
            </span>
            <span class="instruction">
                <strong>2.</strong>
                <span class="instruction-text">${ ui.message("paperrecord.archivesRoom.printRecords.label") }</span>
            </span>
            <span class="instruction">
                <strong>3.</strong>
                <span class="instruction-text">${ ui.message("paperrecord.archivesRoom.sendRecords.label") }</span>
            </span>
        </div>
        <div class="box-align">
            <table id="create_requests_table">
                <thead>
                <tr>
                    <th>${ ui.message("coreapps.person.name") }</th>
                    <th>${ ui.message("coreapps.patient.paperRecordIdentifier") }</th>
                    <th>${ ui.message("coreapps.location") }</th>
                    <th>${ ui.message("coreapps.time") }</th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody data-bind="foreach: recordsToCreate">
                <tr data-bind="attr:{'class': patientId}, css:{ selected: selected(), hover: hovered() }, visible: visible()" >
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBeCreated"><span data-bind="text: patientName"></span></td>
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBeCreated"><span data-bind="text: dossierNumber"></span></td>
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBeCreated"><span data-bind="text: sendToLocation"></span></td>
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBeCreated"><span data-bind="text: timeRequested"></span></td>
                    <td><i data-bind="click: \$root.cancelRequest" class="delete-item icon-remove" title="${ui.message("paperrecord.archivesRoom.cancel")}"></i></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="box-align btn">
            <button id="assign-to-create-button" class="arrow" data-bind="css: { disabled: !isValid() }, enable: isValid()">
                <i class="icon-print"></i> <span>${ ui.message("paperrecord.archivesRoom.printSelected") }</span>
                <span class="arrow-border-button"></span>
                <span class="arrow-button"></span>
            </button>
        </div>
    </span>
    <div id="assignedcreaterequest" class="box-align">
        <div class="sending-records scan-input">
            <div id="scan-create-records" class="container">
                <form class="mark-as-pulled">
                    <input type="text" size="40" name="mark-as-pulled-identifier" class="mark-as-pulled-identifier" placeholder="${ ui.message("paperrecord.archivesRoom.typeOrIdentifyBarCode.label") }"/>
                </form>
            </div>
        </div>
        <table id="assigned_create_requests_table">
            <thead>
            <tr>
                <th>${ ui.message("coreapps.person.name") }</th>
                <th>${ ui.message("coreapps.patient.paperRecordIdentifier") }</th>
                <th>${ ui.message("coreapps.location") }</th>
                <th>${ ui.message("coreapps.time") }</th>
                <th>&nbsp;</th>
                <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody data-bind="foreach: assignedRecordsToCreate">
            <tr data-bind="attr:{'class': patientId}, css:{ selected: selected() }, visible: visible()" >
                <td><span data-bind="text: patientName"></span></td>
                <td><span data-bind="text: dossierNumber"></span></td>
                <td><span data-bind="text: sendToLocation"></span></td>
                <td><span data-bind="text: timeRequested"></span></td>
                <td><button data-bind="click: \$root.printPaperRecordLabelSet" class="print" title="${ui.message("paperrecord.archivesRoom.reprint")}"><i class="icon-print"></i> </button></td>
                <td><i data-bind="click: \$root.cancelRequest" class="delete-item icon-remove" title="${ui.message("paperrecord.archivesRoom.cancel")}"></i></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

<div id="tab-pull">
    <span id="pullrequest">
        <div class="instructions">
            <span class="instruction">
                <strong>1.</strong>
                <span class="instruction-text">${ ui.message("paperrecord.archivesRoom.selectRecordsPull.label") }</span>
            </span>
            <span class="instruction">
                <strong>2.</strong>
                <span class="instruction-text">${ ui.message("paperrecord.archivesRoom.clickRecordsPull.label") }</span>
            </span>
            <span class="instruction">
                <strong>3.</strong>
                <span class="instruction-text">${ ui.message("paperrecord.archivesRoom.sendRecords.label") }</span>
            </span>
        </div>
        <div class="box-align">
            <table id="pull_requests_table">
                <thead>
                <tr>
                    <th>${ ui.message("coreapps.person.name") }</th>
                    <th>${ ui.message("coreapps.patient.paperRecordIdentifier") }</th>
                    <th>${ ui.message("paperrecord.archivesRoom.requestedBy.label") }</th>
                    <th>${ ui.message("coreapps.time") }</th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody data-bind="foreach: recordsToPull">
                <tr data-bind="attr:{'class': patientId}, css:{selected: selected(), hover: hovered()}, visible: visible()" >
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBePulled"><span data-bind="text: patientName"></span></td>
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBePulled"><span data-bind="text: dossierNumber"></span></td>
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBePulled"><span data-bind="text: sendToLocation"></span></td>
                    <td data-bind="event: { mouseover: \$root.hoverRecords, mouseout: \$root.unHoverRecords }, click: \$root.selectRecordToBePulled"><span data-bind="text: timeRequested"></span></td>
                    <td><i data-bind="click: \$root.cancelRequest" class="delete-item icon-remove" title="${ui.message("paperrecord.archivesRoom.cancel")}"></i></td>
                </tr>
                <tr data-bind="attr:{'class': patientId}, css:{selected: selected(), hover: hovered() }">
                    <td colspan="5" data-bind="visible: locationLastSent" ><span data-bind="text: dossierNumber"></span> ${ ui.message("paperrecord.archivesRoom.sentTo") } <span data-bind="text: locationLastSent"></span> ${ ui.message("paperrecord.archivesRoom.at") } <span data-bind="text: dateLastSent"></span></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="box-align btn">
            <button id="assign-to-pull-button" class="arrow" data-bind="css: { disabled: !isValid() }, enable: isValid()">
                <i class="icon-folder-open"></i> <span>${ ui.message("paperrecord.archivesRoom.pullSelected") }</span>
                <span class="arrow-border-button"></span>
                <span class="arrow-button"></span>
            </button>
        </div>
    </span>
    <div id="assignedpullrequest" class="box-align">
        <div class="sending-records scan-input">
            <div id="scan-pull-records" class="container">
                <form class="mark-as-pulled">
                    <input type="text" size="40" name="mark-as-pulled-identifier" class="mark-as-pulled-identifier" placeholder="${ ui.message("paperrecord.archivesRoom.typeOrIdentifyBarCode.label") }"/>
                </form>
            </div>
        </div>
        <table id="assigned_pull_requests_table" >
            <thead>
            <tr>
                <th>${ ui.message("coreapps.person.name") }</th>
                <th>${ ui.message("coreapps.patient.paperRecordIdentifier") }</th>
                <th>${ ui.message("paperrecord.archivesRoom.requestedBy.label") }</th>
                <th>${ ui.message("coreapps.time") }</th>
                <th>&nbsp;</th>
                <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody data-bind="foreach: assignedRecordsToPull">
            <tr data-bind="attr:{'class': patientId}, css:{selected: selected() }, visible: visible()" >
                <td><span data-bind="text: patientName"></span></td>
                <td><span data-bind="text: dossierNumber"></span></td>
                <td><span data-bind="text: sendToLocation"></span></td>
                <td><span data-bind="text: timeRequested"></span></td>
                <td><button data-bind="click: \$root.printLabel" class="print" title="${ui.message("paperrecord.archivesRoom.reprint")}"><i class="icon-print"></i> </button></td>
                <td><i data-bind="click: \$root.cancelRequest" class="delete-item icon-remove" title="${ui.message("paperrecord.archivesRoom.cancel")}"></i></td>
            </tr>
            <tr>
                <td colspan="6" data-bind="visible: locationLastSent" ><span data-bind="text: dossierNumber"></span> ${ ui.message("paperrecord.archivesRoom.sentTo") } <span data-bind="text: locationLastSent"></span> ${ ui.message("paperrecord.archivesRoom.at") } <span data-bind="text: dateLastSent"></span></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
<div id="tab-merge">
    <div id="mergeRequests">
        <h2>${ ui.message("paperrecord.archivesRoom.recordsToMerge.label") }</h2>
        <ul data-bind="foreach: requestsToMerge">
            <li>
                <h6><span data-bind="text: dateCreated"></span></h6>
                <div class="patients-to-merge">
                    <div class="patient">
                        <div class="content"><i class="icon-folder-open medium"></i><span data-bind="text: notPreferredIdentifier"></span> <br/> <span data-bind="text: notPreferredName"></span> </div>
                    </div>
                    <div class="arrow">
                        <i class="icon-chevron-right medium"></i>
                    </div>
                    <div class="patient">
                        <div class="content"><i class="icon-folder-open medium"></i><span data-bind="text: preferredIdentifier"></span> <br/> <span data-bind="text: preferredName"></span> </div>
                    </div>
                    <div class="checkbox-done">
                        <input type="checkbox" name="mergeId" data-bind="value: mergeRequestId"/>
                        <label>${ ui.message("paperrecord.archivesRoom.recordsToMerge.done.label") }</label>
                    </div>
                </div>
            </li>
        </ul>
    </div>
</div>

<div id="tab-return">

    <h2>${ ui.message("paperrecord.archivesRoom.returningRecords.label") }</h2>

    <div id="scan-returned-records">
        ${ ui.message("paperrecord.archivesRoom.typeOrIdentifyBarCode.label") }
        <br/>
        <form class="mark-as-returned scan-input">
            <input type="text" size="40" name="mark-as-returned-identifier" class="mark-as-returned-identifier" placeholder="${ ui.message("paperrecord.archivesRoom.typeOrIdentifyBarCode.label") }"/>
        </form>
    </div>

</div>

</div>


<div id="cancel-paper-record-request-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("paperrecord.archivesRoom.cancelRequest.title") }</h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("paperrecord.archivesRoom.pleaseConfirmCancel.message") }</p>

        <button class="confirm right">${ ui.message("coreapps.yes") }</button>
        <button class="cancel">${ ui.message("coreapps.no") }</button>
    </div>
</div>
