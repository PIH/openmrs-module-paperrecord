package org.openmrs.module.paperrecord.reporting.library;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.emrapi.patient.reporting.library.EmrApiPatientDataLibrary;
import org.openmrs.module.paperrecord.PaperRecordProperties;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
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

        @Before
        public void setup() throws Exception {
        executeDataSet("paperRecordTestDataset.xml");
    }

        @Test
        public void shouldFetchPrimaryIdentifier() throws Exception {
        test(library.getPaperRecordIdentifier(), "101");
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


