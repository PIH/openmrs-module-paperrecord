describe("Tests for medical record requests", function() {

    var thirdRecord = RecordRequestModel(3, "Darius", 1, "A033", "Lacoline", "12:34 pm");
    var secondRecord = RecordRequestModel(2, "Mark", 2, "A021", "Lacoline", "12:34 pm");

    var recordsToPull = [
        RecordRequestModel(1, "Alex", 3, "A001", "Mirebalais", "12:34 pm"),
        secondRecord,
        thirdRecord,
        RecordRequestModel(4, "Neil", 4, "A045", "Lacoline", "12:34 pm"),
        RecordRequestModel(5, "Mario", 5, "A101", "Lacoline", "12:34 pm"),
        RecordRequestModel(6, "Renee", 6, "A121", "Lacoline", "12:34 pm"),
        RecordRequestModel(7, "Ellen", 7, "A234", "Lacoline", "12:34 pm"),
        RecordRequestModel(8, "Mike", 8, "A235", "Lacoline", "12:34 pm")
    ];
    var viewModel = PullRequestsViewModel(recordsToPull);


    it("should select two records and unselect one", function() {
        viewModel.selectRecordToBePulled(secondRecord);
        viewModel.selectRecordToBePulled(thirdRecord);
        expect(viewModel.recordsToPull()[1].selected()).toBe(true);
        expect(viewModel.recordsToPull()[2].selected()).toBe(true);

        viewModel.selectRecordToBePulled(thirdRecord);
        expect(viewModel.recordsToPull()[1].selected()).toBe(true);
        expect(viewModel.recordsToPull()[2].selected()).toBe(false);
    });

    it("should hover two hovers and unhover one", function() {
        viewModel.hoverRecords(secondRecord);
        viewModel.hoverRecords(thirdRecord);
        expect(viewModel.recordsToPull()[1].hovered()).toBe(true);
        expect(viewModel.recordsToPull()[2].hovered()).toBe(true);

        viewModel.unHoverRecords(thirdRecord);
        expect(viewModel.recordsToPull()[1].hovered()).toBe(true);
        expect(viewModel.recordsToPull()[2].hovered()).toBe(false);
    })


})
