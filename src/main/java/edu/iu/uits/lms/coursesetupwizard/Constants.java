package edu.iu.uits.lms.coursesetupwizard;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Constants {

    /**
     * Constant defining the ehcache provider
     */
    public static final String EHCACHE_PROVIDER_TYPE = "org.ehcache.jsr107.EhcacheCachingProvider";

    public static final String INSTRUCTOR_COURSES_CACHE_NAME = "InstructorCourses";

    public static final String KEY_IMPORT_MODEL = "importModel";
    public static final String ACTION_BACK = "back";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_HOME = "home";
    public static final String ACTION_SUBMIT = "submit";

    @AllArgsConstructor
    @Getter
    public enum MAIN_OPTION {
        IMPORT,
        TEMPLATE,
        HOMEPAGE
    }

    @AllArgsConstructor
    @Getter
    public enum DATE_OPTION {
        ADJUST,
        REMOVE,
        NOCHANGE
    }

    @AllArgsConstructor
    @Getter
    public enum CONTENT_OPTION {
        ALL,
        SELECT
    }

    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}
