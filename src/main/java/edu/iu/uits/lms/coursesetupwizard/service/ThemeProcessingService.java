package edu.iu.uits.lms.coursesetupwizard.service;

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
import edu.iu.uits.lms.coursesetupwizard.model.ThemeContent;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeModel;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeContentRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeRepository;
import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.model.Priority;
import edu.iu.uits.lms.email.service.EmailService;
import edu.iu.uits.lms.email.service.LmsEmailTooBigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;

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
    protected ToolConfig toolConfig;

    public List<String> processSubmit(ThemeModel themeModel, String courseId, String userToSendCommunicationAs) {
        log.info("In process Model!!!");

        List<String> exceptionMessages = new ArrayList<>();

        WikiPage newWikiPage;
        ThemeContent themeContent;

        //  1. Create a page called Wizard Next Steps. This page will contain information only.
        try {
            themeContent = themeContentRepository.findByName(Constants.THEME_NEXT_STEPS_BODY_NAME);

            if (themeContent == null || themeContent.getValue() == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_NEXT_STEPS_BODY_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle("Wizard Next Steps");
            newWikiPage.setPublished(false);
            newWikiPage.setFrontPage(false);
            newWikiPage.setBody(themeContent.getValue());

            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage));

            log.info(String.format("Successfully created Wiki page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  2. Create course home page (using create process above), publish it, set as front page
        try {
            themeContent = themeContentRepository.findByName(Constants.THEME_HOME_PAGE_BODY_NAME);

            if (themeContent == null || themeContent.getValue() == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_HOME_PAGE_BODY_NAME);
            }

            newWikiPage = new WikiPage();
            newWikiPage.setTitle("Course Home Page");
            newWikiPage.setPublished(true);
            newWikiPage.setFrontPage(true);
            newWikiPage.setBody(themeContent.getValue());

            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage));

            log.info(String.format("Successfully created course home page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  3. Set the wiki page created in step 2 as the default
        try {
            courseService.updateCourseFrontPage(courseId, "wiki");

            log.info(String.format("Successfully set wiki as the course front page for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  4. Update syllabus (more content to come from project team)

        try {
            themeContent = themeContentRepository.findByName(Constants.THEME_SYLLABUS_BODY_NAME);

            if (themeContent == null || themeContent.getValue() == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_SYLLABUS_BODY_NAME);
            }

            CourseSyllabusBody courseSyllabusBody = new CourseSyllabusBody();
            courseSyllabusBody.setSyllabusBody(themeContent.getValue());

            courseService.updateCourseSyllabus(courseId, new CourseSyllabusBodyWrapper(courseSyllabusBody));

            log.info(String.format("Updated syllabus for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  5. Create Assignment Groups - these will be used by the end user later when interacting with the Multi-tool
        AssignmentGroup assignmentGroup = null;

        try {
            assignmentGroup = assignmentService.createAssignmentGroup(courseId, "Templates");

            log.info(String.format("Successfully created Assignment Group 'Templates' for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Assignment group #1 creation: " + e.getMessage());
        }

        try {
            assignmentGroup = assignmentService.createAssignmentGroup(courseId, "Assignments");

            log.info(String.format("Successfully created Assignment Group 'Assignments' for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Assignment group #2 creation: " + e.getMessage());
        }

        if (assignmentGroup != null && assignmentGroup.getId() != null) {
            //  6. Create assignment in the Templates assignment group in the Assignments tool.
            Assignment assignment;

            try {
                themeContent = themeContentRepository.findByName(Constants.THEME_ASSIGNMENT_DESCRIPTION_NAME);

                if (themeContent == null || themeContent.getValue() == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_ASSIGNMENT_DESCRIPTION_NAME);
                }

                assignment = new Assignment();
                assignment.setName("[Template] Assignment");
                assignment.setAssignmentGroupId(assignmentGroup.getId());
                assignment.setDescription(themeContent.getValue());

                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment));

                log.info(String.format("Successfully created Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Assignment #1 Creation: " + e.getMessage());
            }

            //  7. Create graded discussion in the Templates assignment group in the Assignments tool
            try {
                themeContent = themeContentRepository.findByName(Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_NAME);

                if (themeContent == null || themeContent.getValue() == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_NAME);
                }

                assignment = new Assignment();
                assignment.setName("[Template] Graded Discussion");
                assignment.setAssignmentGroupId(assignmentGroup.getId());
                assignment.setSubmissionTypes(List.of("discussion_topic"));
                assignment.setDescription(themeContent.getValue());

                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment));

                log.info(String.format("Successfully created Graded discussion Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Assignment #2 Creation: " + e.getMessage());
            }

            //  8. Create quiz in the Templates assignment group in the Assignments tool
            try {
                themeContent = themeContentRepository.findByName(Constants.THEME_QUIZ_DESCRIPTION_NAME);

                if (themeContent == null || themeContent.getValue() == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_QUIZ_DESCRIPTION_NAME);
                }

                assignment = new Assignment();
                assignment.setName("[Template] Quiz");
                assignment.setAssignmentGroupId(assignmentGroup.getId());
                assignment.setSubmissionTypes(List.of("online_quiz"));
                assignment.setDescription(themeContent.getValue());

                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment));

                log.info(String.format("Successfully created quiz Assignment for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Assignment #3 Creation: " + e.getMessage());
            }
        }

        //  9. Create ungraded discussion item in Discussions tool
        try {
            themeContent = themeContentRepository.findByName(Constants.THEME_DISCUSSION_TOPIC_MESSAGE_NAME);

            if (themeContent == null || themeContent.getValue() == null) {
                throw new RuntimeException("Could not find value for " + Constants.THEME_DISCUSSION_TOPIC_MESSAGE_NAME);
            }

            DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setTitle("[Template] Ungraded Discussion");
            discussionTopic.setMessage(themeContent.getValue());
            discussionTopic.setDiscussionType(DiscussionTopic.TYPE.THREADED);
            discussionTopic.setDelayedPostAt(Constants.THEME_DELAYED_POST_AT_STRING);

            discussionService.createDiscussionTopic(courseId, discussionTopic,
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToSendCommunicationAs);

            log.info(String.format("Successfully created ungraded discussion topic for courseId %s", courseId));
        } catch (Exception e) {
            exceptionMessages.add("Discussion Topic #1 Creation: " + e.getMessage());
        }

        // 10. Create items in the Announcements tool (step 9 in Lynn’s stuff) ** still being worked on
        if (themeModel != null && themeModel.getIncludeGuidance()) {
            try {
                themeContent = themeContentRepository.findByName(Constants.THEME_ANNOUNCEMENT_MESSAGE_NAME);

                if (themeContent == null || themeContent.getValue() == null) {
                    throw new RuntimeException("Could not find value for " + Constants.THEME_ANNOUNCEMENT_MESSAGE_NAME);
                }

                Announcement announcement = new Announcement();
                announcement.setTitle("[Template] Announcement");
                announcement.setPublished(true);
                announcement.setDiscussionType(DiscussionTopic.TYPE.SIDE_COMMENT);
                announcement.setDelayedPostAt(Constants.THEME_DELAYED_POST_AT_STRING);
                announcement.setMessage(themeContent.getValue());

                announcementService.createAnnouncement(courseId, announcement, false,
                        CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToSendCommunicationAs, null, null);

                log.info(String.format("Successfully created Announcement for courseId %s", courseId));
            } catch (Exception e) {
                exceptionMessages.add("Announcement Creation: " + e.getMessage());
            }
        }

        // 11. Log any steps that fail but continue on to the next step. Send error message to our team email accounts with info on course and failed steps.

        if (! exceptionMessages.isEmpty()) {
            try {
                EmailDetails emailDetails = new EmailDetails();

                emailDetails.setPriority(Priority.HIGH);
                emailDetails.setSubject(emailService.getStandardHeader() + " Course Setup Wizard Theme Error");

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("The following exceptions happened during theme processing: \r\n\r\n");

                for (String exceptionMessage : exceptionMessages) {
                    stringBuilder.append(exceptionMessage);
                    stringBuilder.append("\r\n\r\n");
                }

                emailDetails.setBody(stringBuilder.toString());
                emailDetails.setRecipients(toolConfig.getNotificationEmail());
                emailService.sendEmail(emailDetails);
            } catch (LmsEmailTooBigException | MessagingException e) {
                log.error("Error sending email");
            }
        } else {
            log.info("ALL worked WITHOUT error!!!!");
        }

        // 12. Once all steps above are completed, drop the user on the Next Steps page

        return exceptionMessages;
    }

}
