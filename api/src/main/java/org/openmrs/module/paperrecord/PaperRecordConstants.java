package org.openmrs.module.paperrecord;

/**
 *
 */
public class PaperRecordConstants {

    public static final String PRIVILEGE_PAPER_RECORDS_REQUEST_RECORDS = "Paper Records - Request Records";

    public static final String PRIVILEGE_PAPER_RECORDS_MANAGE_REQUESTS = "Paper Records - Manage Requests";

    public static final String GP_PAPER_RECORD_IDENTIFIER_TYPE = "emr.paperRecordIdentifierType";

    public static final String GP_EXTERNAL_DOSSIER_IDENTIFIER_TYPE = "emr.externalDossierIdentifierType";

    public static final String LOCATION_TAG_MEDICAL_RECORD_LOCATION = "Medical Record Location";

    public static final String TASK_CLOSE_STALE_PULL_REQUESTS = "EMR module - Close Stale Pull Request";

    public static final String TASK_CLOSE_STALE_PULL_REQUESTS_DESCRIPTION = "Closes any pending pull record requests older than a specified date";

    public static final String TASK_CLOSE_STALE_CREATE_REQUESTS = "Paper Records module - Close Stale Create Requests";

    public static final String TASK_CLOSE_STALE_CREATE_REQUESTS_DESCRIPTION = "Closes any pending create record requests older than a specified date";

    public final static int NUMBER_OF_LABELS_TO_PRINT_WHEN_CREATING_NEW_RECORD = 3;

    public final static int NUMBER_OF_LABELS_TO_PRINT_WHEN_PULLING_RECORD = 2;
}
