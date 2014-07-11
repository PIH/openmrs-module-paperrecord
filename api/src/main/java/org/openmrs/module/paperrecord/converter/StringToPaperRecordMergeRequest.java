package org.openmrs.module.paperrecord.converter;


import org.openmrs.module.paperrecord.PaperRecordMergeRequest;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPaperRecordMergeRequest implements Converter<String, PaperRecordMergeRequest> {

    @Autowired
    @Qualifier("paperRecordService")
    private PaperRecordService paperRecordService;

    @Override
    public PaperRecordMergeRequest convert(String id) {
        return paperRecordService.getPaperRecordMergeRequestById(Integer.valueOf(id));
    }
}
