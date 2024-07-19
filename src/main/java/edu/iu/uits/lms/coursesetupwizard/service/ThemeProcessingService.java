package edu.iu.uits.lms.coursesetupwizard.service;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2024 Indiana University
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
import edu.iu.uits.lms.canvas.model.WikiPage;
import edu.iu.uits.lms.canvas.model.WikiPageCreateWrapper;
import edu.iu.uits.lms.canvas.services.AnnouncementService;
import edu.iu.uits.lms.canvas.services.AssignmentService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.canvas.services.DiscussionService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
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

    public WikiPage processSubmit(ThemeModel themeModel, String courseId, String userToCreateAs) {
        log.info("In process Model!!!");

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
        WikiPage nextStepsWikiPage = null;

        //  1. Create a page called Wizard Next Steps. This page will contain information only.
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_NEXT_STEPS_BODY_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_NEXT_STEPS_BODY_TEMPLATE_NAME);
            }

            nextStepsWikiPage = new WikiPage();
            nextStepsWikiPage.setTitle("Wizard Next Steps");
            nextStepsWikiPage.setPublished(false);
            nextStepsWikiPage.setFrontPage(false);
            nextStepsWikiPage.setBody(textToUse);

            nextStepsWikiPage = courseService.createWikiPage(courseId, new WikiPageCreateWrapper(nextStepsWikiPage),
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

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
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

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

        //  5. Create Assignment Groups - these will be used by the end user later when interacting with the Multi-tool
        AssignmentGroup assignmentGroup = null;

        try {
            assignmentGroup = assignmentService.createAssignmentGroup(courseId, "Templates",
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

            log.info(String.format("Successfully created Assignment Group 'Templates' for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Templates Assignment Group creation: " + e.getMessage());
        }

        try {
            assignmentGroup = assignmentService.createAssignmentGroup(courseId, "Assignments",
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

            log.info(String.format("Successfully created Assignment Group 'Assignments' for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Assignments Assignment group creation: " + e.getMessage());
        }

        if (assignmentGroup != null && assignmentGroup.getId() != null) {
            //  6. Create assignment in the Templates assignment group in the Assignments tool.
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

                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment),
                        CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

                log.info(String.format("Successfully created Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Assignment Creation: " + e.getMessage());
            }

            //  7. Create graded discussion in the Templates assignment group in the Assignments tool
            try {
                textToUse = freemarkerProcessedTextMap.get(Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);

                if (textToUse == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);
                }

                assignment = new Assignment();
                assignment.setName("[Template] Graded Discussion");
                assignment.setAssignmentGroupId(assignmentGroup.getId());
                assignment.setSubmissionTypes(List.of("discussion_topic"));
                assignment.setDescription(textToUse);

                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment),
                        CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

                log.info(String.format("Successfully created Graded discussion Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Graded Assignment Creation: " + e.getMessage());
            }

            //  8. Create quiz in the Templates assignment group in the Assignments tool
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
                        CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

                log.info(String.format("Successfully created quiz Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Quiz Assignment Creation: " + e.getMessage());
            }
        }

        //  9. Create ungraded discussion item in Discussions tool
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_DISCUSSION_TOPIC_MESSAGE_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_DISCUSSION_TOPIC_MESSAGE_TEMPLATE_NAME);
            }

            DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setTitle("[Template] Ungraded Discussion");
            discussionTopic.setMessage(textToUse);
            discussionTopic.setDiscussionType(DiscussionTopic.TYPE.THREADED);
            discussionTopic.setDelayedPostAt(Constants.THEME_DELAYED_POST_AT_STRING);

            discussionService.createDiscussionTopic(courseId, discussionTopic,
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

            log.info(String.format("Successfully created ungraded discussion topic for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Discussion Topic #1 Creation: " + e.getMessage());
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
                        CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs, null, null);

                log.info(String.format("Successfully created Announcement for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Announcement Creation: " + e.getMessage());
            }
        }

        // 11. Create [Template] Page
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_CREATE_TEMPLATE_PAGE_BODY_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_CREATE_TEMPLATE_PAGE_BODY_TEMPLATE_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle("[Template] Page");
            newWikiPage.setPublished(false);
            newWikiPage.setFrontPage(false);
            newWikiPage.setBody(textToUse);

            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage),
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

            log.info(String.format("Successfully created template page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Template Page Creation: " + e.getMessage());
        }

        // 12. Create [Template] Module Page
        try {
            textToUse = freemarkerProcessedTextMap.get(Constants.THEME_MODULE_PAGE_BODY_TEMPLATE_NAME);

            if (textToUse == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_MODULE_PAGE_BODY_TEMPLATE_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle("[Template] Module Page");
            newWikiPage.setPublished(false);
            newWikiPage.setFrontPage(false);
            newWikiPage.setBody(textToUse);

            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage),
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToCreateAs);

            log.info(String.format("Successfully created module page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Module Page Creation: " + e.getMessage());
        }

        // 13. Log any steps that fail but continue on to the next step. Send error message to our team email accounts with info on course and failed steps.
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