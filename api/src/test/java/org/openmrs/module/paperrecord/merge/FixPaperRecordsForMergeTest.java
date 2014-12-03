package org.openmrs.module.paperrecord.merge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtServiceImpl;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.merge.PatientMergeAction;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.patient.PatientDomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapperFactory;
import org.openmrs.module.paperrecord.PaperRecord;
import org.openmrs.module.paperrecord.PaperRecordProperties;
import org.openmrs.module.paperrecord.PaperRecordService;
import org.openmrs.module.reporting.query.visit.service.VisitQueryService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class FixPaperRecordsForMergeTest {

    private AdtServiceImpl service;

    private VisitService mockVisitService;
    private PaperRecordService mockPaperRecordService;
    private EncounterService mockEncounterService;
    private ProviderService mockProviderService;
    private PatientService mockPatientService;
    private DispositionService mockDispositionService;
    private VisitQueryService mockVisitQueryService;
    private EmrApiProperties emrApiProperties;
    private PaperRecordProperties paperRecordProperties;
    private VisitDomainWrapperFactory mockVisitDomainWrapperFactory;
    private PatientDomainWrapperFactory mockPatientDomainWrapperFactory;

    private Person personForCurrentUser;
    private Provider providerForCurrentUser;

    private PatientIdentifierType paperRecordIdentifierType;


    @Before
    public void setup() throws Exception {
        personForCurrentUser = new Person();
        personForCurrentUser.addName(new PersonName("Current", "User", "Person"));

        User authenticatedUser = new User();
        authenticatedUser.setPerson(personForCurrentUser);

        UserContext userContext = mock(UserContext.class);
        when(userContext.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Context.setUserContext(userContext);

        providerForCurrentUser = new Provider();
        providerForCurrentUser.setPerson(personForCurrentUser);
        mockProviderService = mock(ProviderService.class);
        when(mockProviderService.getProvidersByPerson(personForCurrentUser, false)).thenReturn(Collections.singletonList(providerForCurrentUser));

        mockVisitService = mock(VisitService.class);
        mockEncounterService = mock(EncounterService.class);
        mockPatientService = mock(PatientService.class);
        mockPaperRecordService = mock(PaperRecordService.class);
        mockDispositionService = mock(DispositionService.class);
        mockVisitQueryService = mock(VisitQueryService.class);

        mockPatientDomainWrapperFactory = new MockPatientDomainWrapperFactory();
        mockVisitDomainWrapperFactory = new MockVisitDomainWrapperFactory();

        paperRecordIdentifierType = new PatientIdentifierType();

        emrApiProperties = mock(EmrApiProperties.class);
        when(emrApiProperties.getVisitExpireHours()).thenReturn(10);

        paperRecordProperties = mock(PaperRecordProperties.class);
        when(paperRecordProperties.getPaperRecordIdentifierType()).thenReturn(paperRecordIdentifierType);

        FixPaperRecordsForMerge fixPaperRecordsForMerge = new FixPaperRecordsForMerge();
        fixPaperRecordsForMerge.setPaperRecordService(mockPaperRecordService);
        fixPaperRecordsForMerge.setPaperRecordProperties(paperRecordProperties);

        service = new AdtServiceImpl();
        service.setPatientService(mockPatientService);
        service.setVisitService(mockVisitService);
        service.setEncounterService(mockEncounterService);
        service.setProviderService(mockProviderService);
        service.setEmrApiProperties(emrApiProperties);
        service.setPatientMergeActions(Arrays.<PatientMergeAction>asList(fixPaperRecordsForMerge));
        service.setPatientDomainWrapperFactory(mockPatientDomainWrapperFactory);
        service.setVisitDomainWrapperFactory(mockVisitDomainWrapperFactory);
    }


    @After
    public void tearDown() throws Exception {
        // This test is not context-sensitive, but it may be run between two other context-sensitive tests, and our setting up a
        // mock UserContext breaks things in that case.
        Context.clearUserContext();
    }


    @Test
    public void testThatMergingTwoPatientsWithMedicalRecordIdentifierAtSameLocationMarksPaperRecordsForMerge() {

        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Location someLocation = new Location();
        Location anotherLocation = new Location();

        PatientIdentifier preferredIdentifier = new PatientIdentifier("123", paperRecordIdentifierType, someLocation);
        PaperRecord preferredPaperRecord = new PaperRecord();
        preferredPaperRecord.setPatientIdentifier(preferredIdentifier);
        preferredPaperRecord.setRecordLocation(someLocation);

        PatientIdentifier anotherPreferredIdentifier = new PatientIdentifier("789", paperRecordIdentifierType, anotherLocation); // this is a "fake out" one
        PaperRecord anotherPreferredPaperRecord = new PaperRecord();
        anotherPreferredPaperRecord.setPatientIdentifier(anotherPreferredIdentifier);
        anotherPreferredPaperRecord.setRecordLocation(anotherLocation);

        PatientIdentifier notPreferredIdentifier = new PatientIdentifier("456", paperRecordIdentifierType, someLocation);
        PaperRecord notPreferredPaperRecord = new PaperRecord();
        notPreferredPaperRecord.setPatientIdentifier(notPreferredIdentifier);
        notPreferredPaperRecord.setRecordLocation(someLocation);

        preferred.addIdentifier(anotherPreferredIdentifier);
        preferred.addIdentifier(preferredIdentifier);
        notPreferred.addIdentifier(notPreferredIdentifier);

        when(mockPaperRecordService.getPaperRecords(preferred)).thenReturn(Arrays.asList(preferredPaperRecord, anotherPreferredPaperRecord));
        when(mockPaperRecordService.getPaperRecords(notPreferred)).thenReturn(Collections.singletonList(notPreferredPaperRecord));

        service.mergePatients(preferred, notPreferred);

        verify(mockPaperRecordService).markPaperRecordsForMerge(preferredPaperRecord, notPreferredPaperRecord);

        // make sure a merge request is not created for the record at the other location
        verify(mockPaperRecordService, never()).markPaperRecordsForMerge(anotherPreferredPaperRecord, notPreferredPaperRecord);
    }

    private class MockVisitDomainWrapperFactory extends VisitDomainWrapperFactory{

        @Override
        public VisitDomainWrapper newVisitDomainWrapper() {
            VisitDomainWrapper visitDomainWrapper = new VisitDomainWrapper();
            visitDomainWrapper.setVisitQueryService(mockVisitQueryService);
            visitDomainWrapper.setEmrApiProperties(emrApiProperties);
            visitDomainWrapper.setDispositionService(mockDispositionService);
            return visitDomainWrapper;
        }

        @Override
        public VisitDomainWrapper newVisitDomainWrapper(Visit visit) {
            VisitDomainWrapper visitDomainWrapper = newVisitDomainWrapper();
            visitDomainWrapper.setVisit(visit);
            return visitDomainWrapper;
        }
    }

    private class MockPatientDomainWrapperFactory extends PatientDomainWrapperFactory{

        @Override
        public PatientDomainWrapper newPatientDomainWrapper() {
            PatientDomainWrapper patientDomainWrapper = new PatientDomainWrapper();
            patientDomainWrapper.setEmrApiProperties(emrApiProperties);
            patientDomainWrapper.setVisitQueryService(mockVisitQueryService);
            patientDomainWrapper.setAdtService(service);
            return patientDomainWrapper;
        }

        @Override
        public PatientDomainWrapper newPatientDomainWrapper(Patient patient) {
            PatientDomainWrapper patientDomainWrapper = newPatientDomainWrapper();
            patientDomainWrapper.setPatient(patient);
            return patientDomainWrapper;
        }
    }
}
