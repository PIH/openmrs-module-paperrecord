

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
    }, 20000)

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

        // disable button to prevent multiple submits
        jq('#assign-to-create-button').addClass('disabled');
        jq('#assign-to-create-button').attr('disabled', 'disabled');

        e.preventDefault();

        var selectedRequests = createRequestsViewModel.selectedRequests();
        var requestIds = [];

        jQuery.each(selectedRequests, function(index, request) {
            requestIds.push(request.requestId);
        });

        jQuery.ajax({
            url: emr.fragmentActionLink("paperrecord", "archivesRoom", "assignCreateRequests"),
            data: { requestId: requestIds },
            dataType: 'json',
            type: 'POST'
        })
            .success(function(data) {

                emr.successMessage(data.message);

                assignedCreateRequestsViewModel.load();

                jQuery.each(selectedRequests, function(index, request) {
                    request.visible(false);
                    request.selected(false);
                });

                // re-enable button
                jq('#assign-to-create-button').removeClass('disabled');
                jq('#assign-to-create-button').removeAttr('disabled');

            })
            .error(function(xhr) {
                emr.handleError(xhr);

                // re-enable button
                jq('#assign-to-create-button').removeClass('disabled');
                jq('#assign-to-create-button').removeAttr('disabled');
            });
    })

    jq('#assign-to-pull-button').click(function(e) {

        // disable button to prevent multiple submits
        jq('#assign-to-pull-button').addClass('disabled');
        jq('#assign-to-pull-button').attr('disabled', 'disabled');

        e.preventDefault();

        var selectedRequests = pullRequestsViewModel.selectedRequests();
        var requestIds = [];

        jQuery.each(selectedRequests, function(index, request) {
            requestIds.push(request.requestId);
        });

        jQuery.ajax({
            url: emr.fragmentActionLink("paperrecord", "archivesRoom", "assignPullRequests"),
            data: { requestId: requestIds },
            dataType: 'json',
            type: 'POST'
        })
            .success(function(data) {

                emr.successMessage(data.message);

                assignedPullRequestsViewModel.load();

                jQuery.each(selectedRequests, function(index, request) {
                    request.visible(false);
                    request.selected(false);
                })

                // re-enable button
                jq('#assign-to-pull-button').removeClass('disabled');
                jq('#assign-to-pull-button').removeAttr('disabled');


            })
            .error(function(xhr) {
                emr.handleError(xhr);

                // re-enable button
                jq('#assign-to-pull-button').removeClass('disabled');
                jq('#assign-to-pull-button').removeAttr('disabled');
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
