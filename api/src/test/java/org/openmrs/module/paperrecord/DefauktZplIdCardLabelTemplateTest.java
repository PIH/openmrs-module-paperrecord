package org.openmrs.module.paperrecord;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.printer.Printer;
import org.openmrs.module.emrapi.printer.PrinterServiceImpl;
import org.openmrs.module.emrapi.printer.UnableToPrintViaSocketException;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class DefauktZplIdCardLabelTemplateTest {

    private DefaultZplIdCardLabelTemplate template;

    private PatientIdentifierType primaryIdentifierType;

    private PatientIdentifierType paperRecordIdentifierType;

    private PatientIdentifierType externalDossierIdentifierType;

    @Before
    public void setup() {
        primaryIdentifierType = new PatientIdentifierType();
        primaryIdentifierType.setUuid("e0987dc0-460f-11e2-bcfd-0800200c9a66");

        paperRecordIdentifierType = new PatientIdentifierType();
        paperRecordIdentifierType.setUuid("097fbd00-81e0-11e2-9e96-0800200c9a66");


        EmrApiProperties emrApiProperties = mock(EmrApiProperties.class);
        when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(primaryIdentifierType);

        PaperRecordProperties paperRecordProperties = mock(PaperRecordProperties.class);
        when(paperRecordProperties.getPaperRecordIdentifierType()).thenReturn(paperRecordIdentifierType);

        externalDossierIdentifierType = new PatientIdentifierType();
        externalDossierIdentifierType.setId(4);
        when(paperRecordProperties.getExternalDossierIdentifierType()).thenReturn(externalDossierIdentifierType);


        MessageSourceService messageSourceService = mock(MessageSourceService.class);
        when(messageSourceService.getMessage("emr.archivesRoom.recordNumber.label")).thenReturn("Dossier ID");

        template = new DefaultZplIdCardLabelTemplate();
        template.setEmrApiProperties(emrApiProperties);
        template.setPaperRecordProperties(paperRecordProperties);
        template.setMessageSourceService(messageSourceService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateLabelShouldFailIfPatientHasNoName() {

        Patient patient = new Patient();

        PatientIdentifier primaryIdentifier = new PatientIdentifier();
        primaryIdentifier.setIdentifierType(primaryIdentifierType);
        primaryIdentifier.setIdentifier("ABC");
        patient.addIdentifier(primaryIdentifier);

        template.generateLabel(patient);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGenerateLabelShouldFailIfNoPrimaryIdentifier() {

        Patient patient = new Patient();

        PersonName personName = new PersonName();
        patient.addName(personName);

        template.generateLabel(patient);
    }

    @Test
    public void testGenerateLabelShouldNotFailWithMinimumValidPatientRecord() {

        Patient patient = new Patient();

        PatientIdentifier primaryIdentifier = new PatientIdentifier();
        primaryIdentifier.setIdentifierType(primaryIdentifierType);
        primaryIdentifier.setIdentifier("ABC");
        patient.addIdentifier(primaryIdentifier);

        PersonName personName = new PersonName();
        patient.addName(personName);

        String result = template.generateLabel(patient);
        Assert.assertTrue(result.contains("ABC"));
    }

    @Test
    public void testGenerateLabelShouldGenerateLabel() {

        Patient patient = new Patient();

        PatientIdentifier primaryIdentifier = new PatientIdentifier();
        primaryIdentifier.setIdentifierType(primaryIdentifierType);
        primaryIdentifier.setIdentifier("2F1406");
        patient.addIdentifier(primaryIdentifier);

        PatientIdentifier paperRecordIdentifier1 = new PatientIdentifier();
        paperRecordIdentifier1.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier1.setIdentifier("A002300");
        Location location1 = new Location(1);
        location1.setName("Mirebalais");
        paperRecordIdentifier1.setLocation(location1);
        patient.addIdentifier(paperRecordIdentifier1);

        PersonName name = new PersonName();
        name.setFamilyName("Jazayeri");
        name.setGivenName("Ellen");
        patient.addName(name);

        String data = template.generateLabel(patient);

        System.out.println(data);
        Assert.assertTrue(data.equals("^XA^CI28^FO100,40^AUN^FDJazayeri, Ellen^FS^FO480,40^FB520,1,0,R,0^AUN^FD2F1406^FS^FO100,110^AUN^FDA002300^FS^FO100,160^ATN^FDMirebalais Dossier ID^FS^FO1025,10^GB0,590,10^FS^XZ"));

    }


    // the following test requires that the label printer actually be online and available
    // (and that the ip address and port are set properly)

    @Test
    @Ignore
    public void testPrintingLabel() throws UnableToPrintViaSocketException {

        Patient patient = new Patient();

        PatientIdentifier primaryIdentifier = new PatientIdentifier();
        primaryIdentifier.setIdentifierType(primaryIdentifierType);
        primaryIdentifier.setIdentifier("2F1406");
        patient.addIdentifier(primaryIdentifier);

        PatientIdentifier paperRecordIdentifier1 = new PatientIdentifier();
        paperRecordIdentifier1.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier1.setIdentifier("A002300");
        Location location1 = new Location(1);
        location1.setName("Mirebalais");
        paperRecordIdentifier1.setLocation(location1);
        patient.addIdentifier(paperRecordIdentifier1);

        PatientIdentifier paperRecordIdentifier2 = new PatientIdentifier();
        paperRecordIdentifier2.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier2.setIdentifier("A002312");
        Location location2 = new Location(2);
        location2.setName("Cange");
        paperRecordIdentifier2.setLocation(location2);
        patient.addIdentifier(paperRecordIdentifier2);

        PatientIdentifier paperRecordIdentifier3 = new PatientIdentifier();
        paperRecordIdentifier3.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier3.setIdentifier("A003111");
        Location location3 = new Location(3);
        location3.setName("Hinche");
        paperRecordIdentifier3.setLocation(location3);
        patient.addIdentifier(paperRecordIdentifier3);

        PatientIdentifier paperRecordIdentifier4 = new PatientIdentifier();
        paperRecordIdentifier4.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier4.setIdentifier("A005431");
        Location location4 = new Location(4);
        location4.setName("Lacolline");
        paperRecordIdentifier4.setLocation(location4);
        patient.addIdentifier(paperRecordIdentifier4);

        PatientIdentifier paperRecordIdentifier5 = new PatientIdentifier();
        paperRecordIdentifier5.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier5.setIdentifier("A012321");
        Location location5 = new Location(5);
        location5.setName("Boston");
        paperRecordIdentifier5.setLocation(location5);
        patient.addIdentifier(paperRecordIdentifier5);

        PatientIdentifier paperRecordIdentifier6 = new PatientIdentifier();
        paperRecordIdentifier6.setIdentifierType(paperRecordIdentifierType);
        paperRecordIdentifier6.setIdentifier("A012321");
        Location location6 = new Location(6);
        location6.setName("Portland");
        paperRecordIdentifier6.setLocation(location6);
        patient.addIdentifier(paperRecordIdentifier6);


        PersonName name = new PersonName();
        name.setFamilyName("Jazayeri");
        name.setGivenName("Ellen");
        patient.addName(name);

        String data = template.generateLabel(patient);

        Printer printer = new Printer();
        printer.setIpAddress("10.3.18.100");
        printer.setPort("9100");

        new PrinterServiceImpl().printViaSocket(data, printer, "UTF-8");

    }


}
