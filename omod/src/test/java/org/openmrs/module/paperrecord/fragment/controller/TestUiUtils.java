package org.openmrs.module.paperrecord.fragment.controller;

import org.openmrs.Concept;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.ui.framework.BasicUiUtils;
import org.openmrs.ui.framework.FormatterImpl;

/**
 * Implementation of UiUtils suitable for use in non-context-sensitive unit tests.
 * This doesn't have a MessageSource configured, so it won't do localization
 */
public class TestUiUtils extends BasicUiUtils {

    private AdministrationService administrationService;

    private boolean mockFormattingConcepts = false;

    /**
     * If you use this constructor, the UiUtils will have no AdministrationService so it won't do date formatting
     */
    public TestUiUtils() {
        this.formatter = new FormatterImpl(null, null);
    }

    /**
     * Provides an AdministrationService that provides global properties for date formatting
     * @param administrationService
     */
    public TestUiUtils(AdministrationService administrationService) {
        this.administrationService = administrationService;
        this.formatter = new FormatterImpl(null, this.administrationService);
    }

    /**
     * If you set this to true, then calling the #format(Object) method on a concept will just print
     * and arbitrary name, instead of going to the context to check locales
     * @param mockFormattingConcepts
     */
    public void setMockFormattingConcepts(boolean mockFormattingConcepts) {
        this.mockFormattingConcepts = mockFormattingConcepts;
    }

    @Override
    public String format(Object o) {
        if (mockFormattingConcepts && o instanceof Concept) {
            Concept concept = (Concept) o;
            return concept.getNames().iterator().next().getName();
        } else if (o instanceof Person) {
            // skip using the name support bean
            return ((Person) o).getPersonName().getFullName();
        }

        else {
            return super.format(o);
        }
    }

    @Override
    public String message(String code, Object... args) {
        String ret = code;
        if (args.length > 0) {
            ret += ":";
        }
        ret += join(args, ",");
        return ret;
    }

    private String join(Object[] array, String separator) {
        StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (Object o : array) {
            if (!first) {
                ret.append(separator);
            } else {
                first = false;
            }
            ret.append(o);
        }
        return ret.toString();
    }


}
