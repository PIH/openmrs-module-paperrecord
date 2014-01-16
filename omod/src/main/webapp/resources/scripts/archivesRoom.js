

jq(document).ready( function() {

    // set up tabs
    jq("#tabs").tabs();

    // set up models for the table
    pullRequestsViewModel = PullRequestsViewModel([]);
    ko.applyBindings(pullRequestsViewModel, document.getElementById('pullrequest'));

    createRequestsViewModel = CreateRequestsViewModel([]);
    ko.applyBindings(createRequestsViewModel, document.getElementById('createrequest'));

    assignedPullRequestsViewModel = AssignedPullRequestsViewModel([]);
    ko.applyBindings(assignedPullRequestsViewModel, document.getElementById('assignedpullrequest'));

    assignedCreateRequestsViewModel = AssignedCreateRequestsViewModel([]);
    ko.applyBindings(assignedCreateRequestsViewModel, document.getElementById('assignedcreaterequest'));

    mergeRequestsViewModel = MergeRequestsViewModel([]);
    ko.applyBindings(mergeRequestsViewModel, document.getElementById('mergeRequests'));


    // load the tables
    refreshAllQueues();

    // set up auto-refresh of tables every 30 seconds
    setInterval(function() {
        refreshAllQueues();
    }, 30000)

    // handle entering identifiers to mark records as pulled
    jq('.mark-as-pulled').submit(function (e) {

        e.preventDefault();

        var identifier = jq.trim(jq(this).children('.mark-as-pulled-identifier').val());

        if (identifier) {
            jq.ajax({
                url: emr.fragmentActionLink("paperrecord", "archivesRoom", "markPaperRecordRequestAsSent"),
                data: { identifier: identifier },
                dataType: 'json',
                type: 'POST'
            })
                .success(function(data) {
                    // clear out the input box
                    jq('.mark-as-pulled-identifier:visible').val('');

                    // reload the lists
                    refreshAllQueues();

                    emr.successAlert(data.message);
                })
                .error(function(xhr, status, err) {
                    jq('.mark-as-pulled-identifier:visible').val('');
                    emr.handleError(xhr);
                })
        }
    });

    // handle entering identifiers to mark records as sent
    jq('.mark-as-returned').submit(function (e) {

        e.preventDefault();

        var identifier = jq.trim(jq(this).children('.mark-as-returned-identifier').val());

        if (identifier) {
            jq.ajax({
                url: emr.fragmentActionLink("paperrecord", "archivesRoom", "markPaperRecordRequestAsReturned"),
                data: { identifier: identifier },
                dataType: 'json',
                type: 'POST'
            })
                .success(function(data) {
                    // clear out the input box
                    jq('.mark-as-returned-identifier:visible').val('');
                    emr.successAlert(data.message);
                })
                .error(function(xhr, status, err) {
                    jq('.mark-as-returned-identifier:visible').val('');
                    emr.handleError(xhr);
                })
        }

    });


    // handle assignment buttons
    jq('#assign-to-create-button').click(function(e) {

        e.preventDefault();

        var requestIds = [];

        jQuery.each(createRequestsViewModel.selectedRequests(), function(index, request) {
            request.visible(false);
            requestIds.push(request.requestId);
        });

        jQuery.ajax({
            url: emr.fragmentActionLink("paperrecord", "archivesRoom", "assignCreateRequests"),
            data: { requestId: requestIds },
            dataType: 'json',
            type: 'POST'
        })
            .success(function(data) {
                assignedCreateRequestsViewModel.load();

                emr.successMessage(data.message);
            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });
    })

    jq('#assign-to-pull-button').click(function(e) {

        e.preventDefault();

        var requestIds = [];

        jQuery.each(pullRequestsViewModel.selectedRequests(), function(index, request) {
            request.visible(false);
            requestIds.push(request.requestId);
        });

        jQuery.ajax({
            url: emr.fragmentActionLink("paperrecord", "archivesRoom", "assignPullRequests"),
            data: { requestId: requestIds },
            dataType: 'json',
            type: 'POST'
        })
            .success(function(data) {
                assignedPullRequestsViewModel.load();

                emr.successMessage(data.message);
            })
            .error(function(xhr) {
                emr.handleError(xhr);
            });
    })

    jq(document).on('click','[name=mergeId]', function(){
        var mergeId = jq(this).val();
        var row = jq(this).closest('li');
        jQuery.ajax({
            url: emr.fragmentActionLink("paperrecord", "archivesRoom", "markPaperRecordsAsMerged"),
            data: { mergeId: mergeId },
            dataType: 'json',
            type: 'POST'
        })
            .success(function(data) {
                row.hide("slide", {direction: "up"}, 500);
                //   mergeRequestsViewModel.load();
            });
    })

    // if an alphanumeric character is pressed, send focus to the appropriate mark-as-pulled-identifier input box
    // or the mark-as-returned input box (since only one input box will ever be visible, we can simply call the focus
    // for both of them types and only one will be picked up)
    jq(document).keydown(function(event) {
        if (event.which > 47 && event.which < 91) {
            jq(".mark-as-pulled-identifier:visible").focus();
            jq(".mark-as-returned-identifier:visible").focus();
        }
    })

});
