package edu.iu.uits.lms.coursesetupwizard;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Constants {

    /**
     * Constant defining the ehcache provider
     */
    public static final String EHCACHE_PROVIDER_TYPE = "org.ehcache.jsr107.EhcacheCachingProvider";

    public static final String INSTRUCTOR_COURSES_CACHE_NAME = "InstructorCourses";
    public static final String COURSE_TEMPLATES_CACHE_NAME = "CourseTemplates";


    public static final String KEY_IMPORT_MODEL = "importModel";
    public static final String KEY_THEME_MODEL = "themeModel";
    public static final String ACTION_BACK = "back";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_HOME = "home";
    public static final String ACTION_SUBMIT = "submit";

    public static final String FEATURE_ID_THEME_FRONTPAGE_ENABLE  = "coursesetupwizard.theme.frontpage.enable";
    public static final String FEATURE_ID_THEME_GUIDANCE_ENABLE   = "coursesetupwizard.theme.guidance.enable";
    public static final String FEATURE_ID_THEME_NAVIGATION_ENABLE = "coursesetupwizard.theme.navigation.enable";

    public static final String THEME_DELAYED_POST_AT_STRING = "2099-12-30";
    public static final String THEME_NEXT_STEPS_BODY_TEMPLATE_NAME = "theme.nextsteps.body.template";
    public static final String THEME_HOME_PAGE_BODY_TEMPLATE_NAME = "theme.homepage.body.template";
    public static final String THEME_SYLLABUS_BODY_TEMPLATE_NAME = "theme.syllabus.body.template";
    public static final String THEME_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME = "theme.assignment.description.template";
    public static final String THEME_GRADED_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME = "theme.gradedassignment.description.template";
    public static final String THEME_QUIZ_DESCRIPTION_TEMPLATE_NAME = "theme.quiz.description.template";
    public static final String THEME_DISCUSSION_TOPIC_MESSAGE_TEMPLATE_NAME = "theme.discussion.topic.message.template";
    public static final String THEME_ANNOUNCEMENT_MESSAGE_TEMPLATE_NAME = "theme.announcement.message.template";

    @AllArgsConstructor
    @Getter
    public enum MAIN_OPTION {
        IMPORT,
        TEMPLATE,
        HOMEPAGE,
        THEME
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
        ALL_WITH_BLUEPRINT_SETTINGS,
        SELECT
    }

    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}
