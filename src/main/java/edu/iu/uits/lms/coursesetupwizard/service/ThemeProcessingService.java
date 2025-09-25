package edu.iu.uits.lms.coursesetupwizard.service;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2025 Indiana University
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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.Announcement;
import edu.iu.uits.lms.canvas.model.Assignment;
import edu.iu.uits.lms.canvas.model.AssignmentCreateWrapper;
import edu.iu.uits.lms.canvas.model.AssignmentGroup;
import edu.iu.uits.lms.canvas.model.CourseSyllabusBody;
import edu.iu.uits.lms.canvas.model.CourseSyllabusBodyWrapper;
import edu.iu.uits.lms.canvas.model.DiscussionTopic;
import edu.iu.uits.lms.canvas.model.Module;
import edu.iu.uits.lms.canvas.model.ModuleCreateWrapper;
import edu.iu.uits.lms.canvas.model.ModuleItem;
import edu.iu.uits.lms.canvas.model.ModuleItemCreateWrapper;
import edu.iu.uits.lms.canvas.model.WikiPage;
import edu.iu.uits.lms.canvas.model.WikiPageCreateWrapper;
import edu.iu.uits.lms.canvas.services.AnnouncementService;
import edu.iu.uits.lms.canvas.services.AssignmentService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.canvas.services.DiscussionService;
import edu.iu.uits.lms.canvas.services.ModuleService;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.BannerImage;
import edu.iu.uits.lms.coursesetupwizard.model.Theme;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeContent;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeLog;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeModel;
import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeContentRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeLogRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.model.Priority;
import edu.iu.uits.lms.email.service.EmailService;
import edu.iu.uits.lms.email.service.LmsEmailTooBigException;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ThemeProcessingService {
    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    protected BannerImageCategoryRepository bannerImageCategoryRepository;

    @Autowired
    protected BannerImageRepository bannerImageRepository;

    @Autowired
    protected ThemeRepository themeRepository;

    @Autowired
    protected ThemeContentRepository themeContentRepository;

    @Autowired
    protected ThemeLogRepository themeLogRepository;

    @Autowired
    protected ToolConfig toolConfig;

    @Autowired
    protected WizardCourseStatusRepository wizardCourseStatusRepository;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfigurer;

    /**
     *
     * @param themeModel - the form backing model that stores the UI choices that the user selected
     * @param courseId - the Canvas courseId that one creates the items in
     * @param userToCreateAs - the IU networkId user that the items will be created/owned as in Canvas
     * @return - the Wizard Next Steps wiki page that was created
     */
    public WikiPage processSubmit(ThemeModel themeModel, String courseId, String userToCreateAs) {
        List<String> exceptionMessages = new ArrayList<>();

        Map<String, String> freemarkerProcessedTextMap = null;

        try {
            freemarkerProcessedTextMap = loadFreemarkerTemplatesFromTheDatabase(themeModel, courseId);
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not read freemarker templates from the database. This is a terminating event to the theme processing.\r\n\r\n");
            stringBuilder.append("The following exception happened during the freemarker template processing: \r\n\r\n");
            stringBuilder.append(e.getMessage());

            sendEmail(stringBuilder.toString());
            return null;
        }

        String textToUse;
        final String AS_USER_STRING = CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs;
        WikiPage nextStepsWikiPage = null;

        //  1. Create a page called Wizard Next Steps. This page will contain information only.
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_NEXT_STEPS_BODY_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_NEXT_STEPS_BODY_TEMPLATE_NAME);
            }

            nextStepsWikiPage = new WikiPage();
            nextStepsWikiPage.setTitle(Constants.THEME_WIZARD_NEXT_STEPS_TITLE);
            nextStepsWikiPage.setPublished(false);
            nextStepsWikiPage.setFrontPage(false);
            nextStepsWikiPage.setBody(textToUse);

            nextStepsWikiPage = courseService.createWikiPage(courseId, new WikiPageCreateWrapper(nextStepsWikiPage),
                    AS_USER_STRING);

            log.info(String.format("Successfully created Wizard Next Steps Wiki page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Create Wizard Next Steps Wiki page: " + e.getMessage());
        }

        //  2. Create course home page (using create process above), publish it, set as front page
        WikiPage newWikiPage;

        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_HOME_PAGE_BODY_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_HOME_PAGE_BODY_TEMPLATE_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle("Course Home Page");
            newWikiPage.setPublished(true);
            newWikiPage.setFrontPage(true);
            newWikiPage.setBody(textToUse);

            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage),
                    AS_USER_STRING);

            log.info(String.format("Successfully created course home page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Create Course Home Page: " + e.getMessage());
        }

        //  3. Set the wiki page created in step 2 as the default
        try {
            courseService.updateCourseFrontPage(courseId, "wiki");

            log.info(String.format("Successfully set wiki as the course front page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Set use Wiki as Home Page: " + e.getMessage());
        }

        //  4. Update syllabus (more content to come from project team)

        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_SYLLABUS_BODY_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_SYLLABUS_BODY_TEMPLATE_NAME);
            }

            CourseSyllabusBody courseSyllabusBody = new CourseSyllabusBody();
            courseSyllabusBody.setSyllabusBody(textToUse);

            courseService.updateCourseSyllabus(courseId, new CourseSyllabusBodyWrapper(courseSyllabusBody));

            log.info(String.format("Updated syllabus for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Update Syllabus: " + e.getMessage());
        }

        // 5. Create a new Module if one with the proper name doesn't already exist in the course.
        //     If one does exist with that name, use that.
        List<Module> modules = moduleService.getModules(courseId, null);
        String usedModuleId = null;

        for (Module module : modules) {
            if (Constants.THEME_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                usedModuleId = module.getId();
                break;
            }
        }

        if (usedModuleId == null) {
            Module newModule = new Module();
            newModule.setName(Constants.THEME_MODULE_NAME);
            newModule.setPosition("1");

            ModuleCreateWrapper newModuleCreateWrapper = new ModuleCreateWrapper();
            newModuleCreateWrapper.setModule(newModule);

            newModule = moduleService.createModule(courseId, newModuleCreateWrapper,
                    AS_USER_STRING);

            if (newModule == null || newModule.getId() == null) {
                exceptionMessages.add("Could not create new module");
            } else {
                usedModuleId = newModule.getId();
            }
        }

        //  6. Create Assignment Groups - these will be used by the end user later when interacting with the Multi-tool
        AssignmentGroup assignmentGroup = null;

        // See if Assignment Group named Assignments already exists.  If not, create it.
        List<AssignmentGroup> assignmentGroups = assignmentService.getAssignmentGroups(courseId);

        boolean doesGroupAlreadyExist = false;

        for (AssignmentGroup existingGroup : assignmentGroups) {
            if (Constants.THEME_ASSIGNMENTS_GROUP_NAME.equalsIgnoreCase(existingGroup.getName())) {
                doesGroupAlreadyExist = true;
                assignmentGroup = existingGroup;
                log.info(String.format("%s group already exists so not creating", Constants.THEME_ASSIGNMENTS_GROUP_NAME));
                break;
            }
        }

        if (! doesGroupAlreadyExist) {
            try {
                assignmentGroup = assignmentService.createAssignmentGroup(courseId, Constants.THEME_ASSIGNMENTS_GROUP_NAME,
                        AS_USER_STRING);

                log.info(String.format("Successfully created Assignment Group '%s' for courseId %s",
                        Constants.THEME_ASSIGNMENTS_GROUP_NAME, courseId));
            } catch (Exception e) {
                exceptionMessages.add("Assignments Assignment group creation: " + e.getMessage());
            }
        }

        // Create Templates Assignment Group if it doesn't exist
        doesGroupAlreadyExist = false;

        for (AssignmentGroup existingGroup : assignmentGroups) {
            if (Constants.THEME_TEMPLATES_GROUP_NAME.equalsIgnoreCase(existingGroup.getName())) {
                doesGroupAlreadyExist = true;
                assignmentGroup = existingGroup;
                log.info(String.format("%s group already exists so not creating", Constants.THEME_TEMPLATES_GROUP_NAME));
                break;
            }
        }

        if (! doesGroupAlreadyExist) {
            try {
                assignmentGroup = assignmentService.createAssignmentGroup(courseId, Constants.THEME_TEMPLATES_GROUP_NAME,
                        AS_USER_STRING);

                log.info(String.format("Successfully created Assignment Group '%s' for courseId %s", Constants.THEME_TEMPLATES_GROUP_NAME, courseId));
            } catch (Exception e) {
                exceptionMessages.add("Templates Assignment Group creation: " + e.getMessage());
            }
        }

        if (assignmentGroup != null && assignmentGroup.getId() != null) {
            //  7. Create assignment in the Templates assignment group in the Assignments tool as well as in the module created in step 5
            Assignment assignment;

            try {
                textToUse = freemarkerProcessedTextMap.get(Constants.THEME_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);

                if (textToUse == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);
                }

                assignment = new Assignment();
                assignment.setName("[Template] Assignment");
                assignment.setAssignmentGroupId(assignmentGroup.getId());
                assignment.setDescription(textToUse);

                assignment = assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment),
                        AS_USER_STRING);

                ModuleItem moduleAssignmentModuleItem = new ModuleItem();
                moduleAssignmentModuleItem.setTitle(assignment.getName());
                moduleAssignmentModuleItem.setType("Assignment");
                moduleAssignmentModuleItem.setContentId(assignment.getId());
                moduleAssignmentModuleItem.setPosition("5");
                moduleAssignmentModuleItem.setPageUrl("template-assignment");

                ModuleItemCreateWrapper moduleOverviewPageModuleItemCreateWrapper = new ModuleItemCreateWrapper();
                moduleOverviewPageModuleItemCreateWrapper.setModuleItem(moduleAssignmentModuleItem);

                moduleService.createModuleItem(courseId, usedModuleId, moduleOverviewPageModuleItemCreateWrapper,
                        AS_USER_STRING);

                log.info(String.format("Successfully created Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Assignment Creation: " + e.getMessage());
            }

            //  8. Create graded discussion in the Templates assignment group in the Assignments tool
            // NOTE!!!! If we decide to re-enable this, also re-enable the test testFailure_processSubmit_graded_assignment_templateNotFound()
//            try {
//                textToUse = freemarkerProcessedTextMap.get(Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);
//
//                if (textToUse == null) {
//                    throw new RuntimeException("Could not find value for " + Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);
//                }
//
//                assignment = new Assignment();
//                assignment.setName("[Template] Graded Discussion");
//                assignment.setAssignmentGroupId(assignmentGroup.getId());
//                assignment.setSubmissionTypes(List.of("discussion_topic"));
//                assignment.setDescription(textToUse);
//
//                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment),
//                        AS_USER_STRING);
//
//                log.info(String.format("Successfully created Graded discussion Assignment for courseId %s", courseId));
//            } catch (Exception e) {
//                exceptionMessages.add("Graded Assignment Creation: " + e.getMessage());
//            }

            //  9. Create quiz in the Templates assignment group in the Assignments tool
            try {
                textToUse = freemarkerProcessedTextMap.get(Constants.THEME_QUIZ_DESCRIPTION_TEMPLATE_NAME);

                if (textToUse == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_QUIZ_DESCRIPTION_TEMPLATE_NAME);
                }

                assignment = new Assignment();
                assignment.setName("[Template] Quiz");
                assignment.setAssignmentGroupId(assignmentGroup.getId());
                assignment.setSubmissionTypes(List.of("online_quiz"));
                assignment.setDescription(textToUse);

                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment),
                        AS_USER_STRING);

                log.info(String.format("Successfully created quiz Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Quiz Assignment Creation: " + e.getMessage());
            }
        }

        // 10. Create items in the Announcements tool (step 9 in Lynn’s stuff) ** still being worked on
        if (themeModel != null && themeModel.getIncludeGuidance() != null && themeModel.getIncludeGuidance()) {
            try {
                textToUse = freemarkerProcessedTextMap.get(Constants.THEME_ANNOUNCEMENT_MESSAGE_TEMPLATE_NAME);

                if (textToUse == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_ANNOUNCEMENT_MESSAGE_TEMPLATE_NAME);
                }

                Announcement announcement = new Announcement();
                announcement.setTitle("[Template] Announcement");
                announcement.setPublished(true);
                announcement.setDiscussionType(DiscussionTopic.TYPE.SIDE_COMMENT);
                announcement.setDelayedPostAt(Constants.THEME_DELAYED_POST_AT_STRING);
                announcement.setMessage(textToUse);

                announcementService.createAnnouncement(courseId, announcement, false,
                        AS_USER_STRING, null, null);

                log.info(String.format("Successfully created Announcement for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Announcement Creation: " + e.getMessage());
            }
        }

        // 11. Create [Template] Module Overview Page
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_MODULE_OVERVIEW_PAGE_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_MODULE_OVERVIEW_PAGE_TEMPLATE_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle(Constants.THEME_MODULE_OVERVIEW_TITLE);
            newWikiPage.setPublished(false);
            newWikiPage.setFrontPage(false);
            newWikiPage.setBody(textToUse);

            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage),
                    AS_USER_STRING);

            ModuleItem moduleOverviewPageModuleItem = new ModuleItem();
            moduleOverviewPageModuleItem.setTitle(Constants.THEME_MODULE_OVERVIEW_TITLE);
            moduleOverviewPageModuleItem.setType("Page");
            moduleOverviewPageModuleItem.setContentId(newWikiPage.getPageId());
            moduleOverviewPageModuleItem.setPosition("1");
            moduleOverviewPageModuleItem.setPageUrl("template-module-overview");

            ModuleItemCreateWrapper moduleOverviewPageModuleItemCreateWrapper = new ModuleItemCreateWrapper();
            moduleOverviewPageModuleItemCreateWrapper.setModuleItem(moduleOverviewPageModuleItem);

            moduleService.createModuleItem(courseId, usedModuleId, moduleOverviewPageModuleItemCreateWrapper,
                    AS_USER_STRING);

            log.info(String.format("Successfully created module overview page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Module Overview Page Creation: " + e.getMessage());
        }


        // 12. Create [Template] Instructor Lecture and Notes Page in the module created in step 5
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_CREATE_TEMPLATE_INSTRUCTOR_AND_NOTES_PAGE_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_CREATE_TEMPLATE_INSTRUCTOR_AND_NOTES_PAGE_TEMPLATE_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle(Constants.THEME_INSTRUCTOR_LECTURE_AND_NOTES_TITLE);
            newWikiPage.setPublished(false);
            newWikiPage.setFrontPage(false);
            newWikiPage.setBody(textToUse);

            newWikiPage = courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage),
                    AS_USER_STRING);

            ModuleItem lectureAndNotesPageModuleItem = new ModuleItem();
            lectureAndNotesPageModuleItem.setTitle(Constants.THEME_INSTRUCTOR_LECTURE_AND_NOTES_TITLE);
            lectureAndNotesPageModuleItem.setType("Page");
            lectureAndNotesPageModuleItem.setContentId(newWikiPage.getPageId());
            lectureAndNotesPageModuleItem.setPosition("2");
            lectureAndNotesPageModuleItem.setPageUrl("template-instructor-lecture-and-notes");

            ModuleItemCreateWrapper lectureAndNotesPageModuleItemCreateWrapper = new ModuleItemCreateWrapper();
            lectureAndNotesPageModuleItemCreateWrapper.setModuleItem(lectureAndNotesPageModuleItem);

            moduleService.createModuleItem(courseId, usedModuleId, lectureAndNotesPageModuleItemCreateWrapper,
                    AS_USER_STRING);


            log.info(String.format("Successfully created template instructor lecture and notes page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Template Instructor Lecture and Notes Page Creation: " + e.getMessage());
        }

        // 13. Create [Template] Generic Content Page in the module created in step 5
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_GENERIC_CONTENT_PAGE_THEME_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_GENERIC_CONTENT_PAGE_THEME_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle(Constants.THEME_GENERIC_CONTENT_PAGE_TITLE);
            newWikiPage.setPublished(false);
            newWikiPage.setFrontPage(false);
            newWikiPage.setBody(textToUse);

            newWikiPage = courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage),
                    AS_USER_STRING);


            ModuleItem genericPageModuleItem = new ModuleItem();
            genericPageModuleItem.setTitle(Constants.THEME_GENERIC_CONTENT_PAGE_TITLE);
            genericPageModuleItem.setType("Page");
            genericPageModuleItem.setContentId(newWikiPage.getPageId());
            genericPageModuleItem.setPosition("3");
            genericPageModuleItem.setPageUrl("template-generic-content-page");

            ModuleItemCreateWrapper genericPageModuleItemCreateWrapper = new ModuleItemCreateWrapper();
            genericPageModuleItemCreateWrapper.setModuleItem(genericPageModuleItem);

            moduleService.createModuleItem(courseId, usedModuleId, genericPageModuleItemCreateWrapper,
                    AS_USER_STRING);


            log.info(String.format("Successfully created generic content page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Generic Content Page Creation: " + e.getMessage());
        }

        //  14. Create ungraded discussion item in the module created in step 5
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_DISCUSSION_TOPIC_MESSAGE_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_DISCUSSION_TOPIC_MESSAGE_TEMPLATE_NAME);
            }

            DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setTitle("[Template] Discussion Topic");
            discussionTopic.setMessage(textToUse);
            discussionTopic.setDiscussionType(DiscussionTopic.TYPE.THREADED);
            discussionTopic.setDelayedPostAt(Constants.THEME_DELAYED_POST_AT_STRING);

            discussionTopic = discussionService.createDiscussionTopic(courseId, discussionTopic,
                    AS_USER_STRING);

            ModuleItem discussionModuleItem = new ModuleItem();
            discussionModuleItem.setTitle("[Template] Discussion Topic");
            discussionModuleItem.setType("Discussion");
            discussionModuleItem.setContentId(discussionTopic.getId());
            discussionModuleItem.setPosition("4");

            ModuleItemCreateWrapper discussionModuleItemCreateWrapper = new ModuleItemCreateWrapper();
            discussionModuleItemCreateWrapper.setModuleItem(discussionModuleItem);

            moduleService.createModuleItem(courseId, usedModuleId, discussionModuleItemCreateWrapper,
                    AS_USER_STRING);


            log.info(String.format("Successfully created ungraded discussion topic for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Discussion Topic #1 Creation: " + e.getMessage());
        }

        // 15. Log any steps that fail but continue on to the next step. Send error message to our team email accounts with info on course and failed steps.
        if (!exceptionMessages.isEmpty()) {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("The following exceptions happened during theme processing: \r\n\r\n");

            for (String exceptionMessage : exceptionMessages) {
                stringBuilder.append(exceptionMessage);
                stringBuilder.append("\r\n\r\n");
            }

            sendEmail(stringBuilder.toString());
        } else {
            log.info("ALL worked WITHOUT error!!!!");
        }

        WizardCourseStatus wizardCourseStatus = new WizardCourseStatus();
        wizardCourseStatus.setCourseId(courseId);
        wizardCourseStatus.setCompletedBy(userToCreateAs);
        wizardCourseStatus.setMainOption(Constants.MAIN_OPTION.THEME);
        wizardCourseStatusRepository.save(wizardCourseStatus);

        ThemeLog themeLog = new ThemeLog();
        themeLog.setCourseId(courseId);
        themeLog.setLoginId(userToCreateAs);
        themeLog.setIncludeBannerImage(themeModel.getIncludeBannerImage() != null && themeModel.getIncludeBannerImage());
        themeLog.setBannerImageId(themeModel.getBannerImageId());
        themeLog.setThemeId(themeModel.getThemeId());
        themeLog.setBannerImageCategoryId(themeModel.getBannerImageCategoryId());
        themeLog.setIncludeNavigation(themeModel.getIncludeNavigation() != null && themeModel.getIncludeNavigation());
        themeLog.setIncludeGuidance(themeModel.getIncludeGuidance() != null && themeModel.getIncludeGuidance());
        themeLog.setErrors(exceptionMessages.isEmpty() ? null : exceptionMessages.stream().collect(Collectors.joining()));

        themeLog = themeLogRepository.save(themeLog);

        log.info(String.format("Saved theme log with id %d", themeLog.getId()));

        return nextStepsWikiPage;
    }

    /**
     * Loads the freemarker template text from the database into a map
     * @param themeModel theme model which has the form values a user selected
     * @param courseId the courseId to use for processing
     * @return a map which contains the processed template text with values filled in
     */
    private Map<String, String> loadFreemarkerTemplatesFromTheDatabase(ThemeModel themeModel, String courseId) {
        Map<String, Object> freemarkerModel = new HashMap<>();

        String bannerImageAltText = null;
        String bannerImageUrl = null;
        String bannerImageCssClasses = null;
        String headerCssClasses = null;
        String navigationCssClasses = null;
        String wrapperCssClasses = null;

        if ((themeModel.getIncludeBannerImage() != null && themeModel.getIncludeBannerImage()) &&
                (themeModel.getBannerImageId() != null && ! themeModel.getBannerImageId().isEmpty())) {
            Long bannerImageId = Long.valueOf(themeModel.getBannerImageId());
            Optional<BannerImage> bannerImage = bannerImageRepository.findById(bannerImageId);

            if (bannerImage.isPresent()) {
                bannerImageAltText = bannerImage.get().getAltText();
                bannerImageUrl = bannerImage.get().getBannerImageUrl();
            }
        }

        if (themeModel.getThemeId() != null) {
            Long themeId = Long.valueOf(themeModel.getThemeId());
            Optional<Theme> theme = themeRepository.findById(themeId);

            if (theme.isPresent()) {
                bannerImageCssClasses = theme.get().getBannerImageCssClasses();
                headerCssClasses = theme.get().getHeaderCssClasses();
                navigationCssClasses = theme.get().getNavigationCssClasses();
                wrapperCssClasses = theme.get().getWrapperCssClasses();
            }
        }

        freemarkerModel.put("bannerImageAltText", bannerImageAltText);
        freemarkerModel.put("bannerImageCssClasses", bannerImageCssClasses);
        freemarkerModel.put("bannerImageUrl", bannerImageUrl);
        freemarkerModel.put("courseId", courseId);
        freemarkerModel.put("headerCssClasses", headerCssClasses);
        freemarkerModel.put("includeBannerImage", themeModel.getIncludeBannerImage() != null && themeModel.getIncludeBannerImage());
        freemarkerModel.put("includeGuidance", themeModel.getIncludeGuidance() != null && themeModel.getIncludeGuidance());
        freemarkerModel.put("includeNavigation", themeModel.getIncludeNavigation() != null && themeModel.getIncludeNavigation());
        freemarkerModel.put("navigationCssClasses", navigationCssClasses);
        freemarkerModel.put("wrapperCssClasses", wrapperCssClasses);

        Map<String, String> freemarkerProcessedTextMap = new HashMap<>();
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

        Iterable<ThemeContent> allThemeContent = themeContentRepository.findAll();
//        List<ThemeContent> allThemeContent = List.of(themeContentRepository.findById(Constants.THEME_NEXT_STEPS_BODY_TEMPLATE_NAME).get());

        for(ThemeContent themeContent : allThemeContent) {
            stringTemplateLoader.putTemplate(themeContent.getName(), themeContent.getTemplateText());
        }

        try {
            Configuration freemarkerTemplateConfiguration = freemarkerConfigurer.createConfiguration();
            freemarkerTemplateConfiguration.setTemplateLoader(stringTemplateLoader);

            for (ThemeContent themeContent : allThemeContent) {
                Template freemarkerTemplate = freemarkerTemplateConfiguration.getTemplate(themeContent.getName());

                freemarkerProcessedTextMap.put(themeContent.getName(),
                        FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, freemarkerModel));
            }
        } catch (Exception e) {
            log.error("Error processing templates: ", e);
            throw new RuntimeException("Error processing templates from database: " + e.getMessage());
        }

        return  freemarkerProcessedTextMap;
    }

    private void sendEmail(String body) {
        try {
            EmailDetails emailDetails = new EmailDetails();

            emailDetails.setPriority(Priority.HIGH);
            emailDetails.setSubject(emailService.getStandardHeader() + " Course Setup Wizard Theme Error");

            emailDetails.setBody(body);
            emailDetails.setRecipients(toolConfig.getNotificationEmail());
            emailService.sendEmail(emailDetails);
        } catch (LmsEmailTooBigException | MessagingException e) {
            log.error("Error sending email");
        }
    }
}
