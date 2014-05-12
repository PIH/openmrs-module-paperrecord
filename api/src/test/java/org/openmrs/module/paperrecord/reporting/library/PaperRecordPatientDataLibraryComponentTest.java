package org.openmrs.module.paperrecord.reporting.library;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.PatientService;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PaperRecordPatientDataLibraryComponentTest  extends BaseModuleContextSensitiveTest  {

        @Autowired
        private PatientDataService pds;

        @Autowired
        private PaperRecordPatientDataLibrary library;

        @Autowired
        private PatientService patientService;

        @Before
        public void setup() throws Exception {
        executeDataSet("paperRecordTestDataset.xml");
    }

        @Test
        public void shouldFetchPrimaryIdentifier() throws Exception {
        test(library.getPaperRecordIdentifier(), patientService.getPatientIdentifier(1));  // primary key of identifier "101"
    }

    private Object eval(PatientDataDefinition definition) throws EvaluationException {
        Cohort cohort = new Cohort(Arrays.asList(2));

        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(cohort);
        EvaluatedPatientData data = pds.evaluate(definition, context);
        return data.getData().get(2);
    }

    private void test(PatientDataDefinition definition, Object expectedValue) throws EvaluationException {
        Object actualValue = eval(definition);
        assertThat(actualValue, is(expectedValue));
    }

}


