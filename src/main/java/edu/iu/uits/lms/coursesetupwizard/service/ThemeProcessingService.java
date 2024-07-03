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
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeModel;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
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
    protected ToolConfig toolConfig;

    public void processSubmit(ThemeModel themeModel, String courseId, String userToSendCommunicationAs) {
        log.info("In process Model!!!");

//        Theme theme = themeRepository.findById(Long.parseLong(themeModel.getThemeId())).orElse(null);
//        BannerImage bannerImage = bannerImageRepository.findById(Long.parseLong(themeModel.getBannerImageId())).orElse(null);

//        List<WikiPage> wikiPages = courseService.getWikiPages(courseId);
//
//        log.info("Wiki pages size = " + wikiPages.size());

//        WikiPage newWikiPage = new WikiPage();
//        newWikiPage.setTitle("Test from code page");
//        newWikiPage.setPublished(false);
//        newWikiPage.setBody("From code body");
//
//        WikiPageCreateWrapper wikiPageCreateWrapper = new WikiPageCreateWrapper(newWikiPage);
//        WikiPage createdWikiPage = courseService.createWikiPage(courseId, wikiPageCreateWrapper);
//
//        log.info("Created Wiki page is " + createdWikiPage);

        List<String> exceptionMessages = new ArrayList<>();

        WikiPage newWikiPage;

        //  1. Create a page called Wizard Next Steps. This page will contain information only.
        newWikiPage = new WikiPage();
        newWikiPage.setTitle("Wizard Next Steps");
        newWikiPage.setPublished(false);
        newWikiPage.setFrontPage(false);
        newWikiPage.setBody("Body code TBD");

        try {
            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage));
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  2. Create course home page (using create process above), publish it, set as front page
        newWikiPage = new WikiPage();
        newWikiPage.setTitle("Course Home Page");
        newWikiPage.setPublished(true);
        newWikiPage.setFrontPage(true);
        newWikiPage.setBody("Body code TBD");

        try {
            courseService.createWikiPage(courseId, new WikiPageCreateWrapper(newWikiPage));
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  3. Set the wiki page created in step 2 as the default
        try {
            courseService.updateCourseFrontPage(courseId, "wiki");
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  4. Update syllabus (more content to come from project team)
        CourseSyllabusBody courseSyllabusBody = new CourseSyllabusBody();
        courseSyllabusBody.setSyllabusBody("To be determined body");

        try {
            courseService.updateCourseSyllabus(courseId, new CourseSyllabusBodyWrapper(courseSyllabusBody));
        } catch (Exception e) {
            exceptionMessages.add(e.getMessage());
        }

        //  5. Create Assignment Groups - these will be used by the end user later when interacting with the Multi-tool
        AssignmentGroup assignmentGroup = null;

        try {
            assignmentGroup = assignmentService.createAssignmentGroup(courseId, "Templates");
        } catch (Exception e) {
            exceptionMessages.add("Assignment group #1 creation: " + e.getMessage());
        }

        try {
            assignmentGroup = assignmentService.createAssignmentGroup(courseId, "Assignments");
        } catch (Exception e) {
            exceptionMessages.add("Assignment group #2 creation: " + e.getMessage());
        }

        if (assignmentGroup != null && assignmentGroup.getId() != null) {
            //  6. Create assignment in the Templates assignment group in the Assignments tool.
            Assignment assignment = new Assignment();
            assignment.setName("[Template] Assignment");
            assignment.setAssignmentGroupId(assignmentGroup.getId());
            assignment.setDescription("<content to come from project team>");

            try {
                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment));
            } catch (Exception e) {
                exceptionMessages.add("Assignment #1 Creation: " + e.getMessage());
            }

            //  7. Create graded discussion in the Templates assignment group in the Assignments tool
            assignment = new Assignment();
            assignment.setName("[Template] Graded Discussion");
            assignment.setAssignmentGroupId(assignmentGroup.getId());
            assignment.setSubmissionTypes(List.of("discussion_topic"));
            assignment.setDescription("<content to come from project team>");

            try {
                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment));
            } catch (Exception e) {
                exceptionMessages.add("Assignment #2 Creation: " + e.getMessage());
            }

            //  8. Create quiz in the Templates assignment group in the Assignments tool
            assignment = new Assignment();
            assignment.setName("[Template] Quiz");
            assignment.setAssignmentGroupId(assignmentGroup.getId());
            assignment.setSubmissionTypes(List.of("online_quiz"));
            assignment.setDescription("<content to come from project team>");

            try {
                assignmentService.createAssignment(courseId, new AssignmentCreateWrapper(assignment));
            } catch (Exception e) {
                exceptionMessages.add("Assignment #3 Creation: " + e.getMessage());
            }
        }

        final String DELAYED_POST_AT_STRING = "2099-12-30";

        //  9. Create ungraded discussion item in Discussions tool
        DiscussionTopic discussionTopic = new DiscussionTopic();
        discussionTopic.setTitle("[Template] Ungraded Discussion");
        discussionTopic.setMessage("Here's all the things you should discuss here");
        discussionTopic.setDiscussionType(DiscussionTopic.TYPE.THREADED);
        discussionTopic.setDelayedPostAt(DELAYED_POST_AT_STRING);

        try {
            discussionService.createDiscussionTopic(courseId, discussionTopic,
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToSendCommunicationAs);
        } catch (Exception e) {
            exceptionMessages.add("Discussion Topic #1 Creation: " + e.getMessage());
        }

        // 10. Create items in the Announcements tool (step 9 in Lynn’s stuff) ** still being worked on
        Announcement announcement = new Announcement();
        announcement.setTitle("[Template] Announcement");
        announcement.setPublished(true);
        announcement.setDiscussionType(DiscussionTopic.TYPE.SIDE_COMMENT);
        announcement.setDelayedPostAt(DELAYED_POST_AT_STRING);
        announcement.setMessage("Here's all the things you should discuss here");

        try {
            announcementService.createAnnouncement(courseId, announcement, false,
                    CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + userToSendCommunicationAs, null, null);
        } catch (Exception e) {
            exceptionMessages.add("Announcement Creation: " + e.getMessage());
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
    }

}
