package edu.iu.uits.lms.coursesetupwizard;

public class Constants {

    /**
     * Constant defining the ehcache provider
     */
    public static final String EHCACHE_PROVIDER_TYPE = "org.ehcache.jsr107.EhcacheCachingProvider";

    public static final String INSTRUCTOR_COURSES_CACHE_NAME = "InstructorCourses";

    public static final String MENU_IMPORT = "import";
    public static final String MENU_TEMPLATE = "template";
    public static final String MENU_HOMEPAGE = "homepage";

    public static final String KEY_IMPORT_MODEL = "importModel";
    public static final String ACTION_BACK = "back";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_HOME = "home";
    public static final String ACTION_SUBMIT = "submit";

    public static final String DATE_OPTION_ADJUST = "adjust";
    public static final String DATE_OPTION_REMOVE = "remove";
    public static final String DATE_OPTION_NOCHANGE = "no-change";

    public static final String CONTENT_OPTION_ALL = "all";
    public static final String CONTENT_OPTION_SELECT = "select";

    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}
