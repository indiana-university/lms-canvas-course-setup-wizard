package edu.iu.uits.lms.coursesetupwizard.services;

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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.AssignmentGroup;
import edu.iu.uits.lms.canvas.model.WikiPage;
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
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeContentRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeLogRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import edu.iu.uits.lms.coursesetupwizard.service.ThemeProcessingService;
import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.service.EmailService;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes={ThemeProcessingService.class})
@SpringBootTest
@Slf4j
public class ThemeProcessingServiceTest {

   @Autowired
   private ThemeProcessingService themeProcessingService;

   @MockBean
   private AnnouncementService announcementService;

   @MockBean
   private AssignmentService assignmentService;

   @MockBean
   private CourseService courseService;

   @MockBean
   private DiscussionService discussionService;

   @MockBean
   private EmailService emailService;

   @Captor
   ArgumentCaptor<EmailDetails> emailCaptor;

   @MockBean
   protected BannerImageCategoryRepository bannerImageCategoryRepository;

   @MockBean
   protected BannerImageRepository bannerImageRepository;

   @MockBean
   protected ThemeRepository themeRepository;

   @MockBean
   protected ThemeContentRepository themeContentRepository;

   @MockBean
   protected ThemeLogRepository themeLogRepository;

   @MockBean
   protected ToolConfig toolConfig;

   @MockBean
   protected WizardCourseStatusRepository wizardCourseStatusRepository;

   @MockBean
   private FreeMarkerConfigurer freemarkerConfigurer;

   @BeforeEach
   public void globalMocks() throws Exception {
      ThemeContent themeContent01 = new ThemeContent();
      themeContent01.setName(Constants.THEME_NEXT_STEPS_BODY_TEMPLATE_NAME);
      themeContent01.setTemplateText("");

      ThemeContent themeContent02 = new ThemeContent();
      themeContent02.setName(Constants.THEME_HOME_PAGE_BODY_TEMPLATE_NAME);
      themeContent02.setTemplateText("");

      ThemeContent themeContent03 = new ThemeContent();
      themeContent03.setName(Constants.THEME_SYLLABUS_BODY_TEMPLATE_NAME);
      themeContent03.setTemplateText("");

      ThemeContent themeContent04 = new ThemeContent();
      themeContent04.setName(Constants.THEME_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);
      themeContent04.setTemplateText("");

      ThemeContent themeContent05 = new ThemeContent();
      themeContent05.setName(Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME);
      themeContent05.setTemplateText("");

      ThemeContent themeContent06 = new ThemeContent();
      themeContent06.setName(Constants.THEME_QUIZ_DESCRIPTION_TEMPLATE_NAME);
      themeContent06.setTemplateText("");

      ThemeContent themeContent07 = new ThemeContent();
      themeContent07.setName(Constants.THEME_DISCUSSION_TOPIC_MESSAGE_TEMPLATE_NAME);
      themeContent07.setTemplateText("");

      ThemeContent themeContent08 = new ThemeContent();
      themeContent08.setName(Constants.THEME_ANNOUNCEMENT_MESSAGE_TEMPLATE_NAME);
      themeContent08.setTemplateText("");

      ThemeContent themeContent09 = new ThemeContent();
      themeContent09.setName(Constants.THEME_CREATE_TEMPLATE_PAGE_BODY_TEMPLATE_NAME);
      themeContent09.setTemplateText("");

      ThemeContent themeContent10 = new ThemeContent();
      themeContent10.setName(Constants.THEME_MODULE_PAGE_BODY_TEMPLATE_NAME);
      themeContent10.setTemplateText("");

      List<ThemeContent> themeContents = Arrays.asList(themeContent01, themeContent02, themeContent03, themeContent04, themeContent05,
              themeContent06, themeContent07, themeContent08, themeContent09, themeContent10);

      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(themeContents));
      when(freemarkerConfigurer.createConfiguration()).thenReturn(new Configuration());

      ThemeLog themeLog = new ThemeLog();
      themeLog.setId(1L);
      when(themeLogRepository.save(any())).thenReturn(themeLog);
   }

   @Test
   void testSuccess_loadFreemarkerTemplatesFromTheDatabase() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Method method = themeProcessingService.getClass().
              getDeclaredMethod("loadFreemarkerTemplatesFromTheDatabase", ThemeModel.class, String.class);
      method.setAccessible(true);

      Map<String, String> freemarkerProcessedTextMap =
              (Map<String, String>) method.invoke(themeProcessingService, themeModel, courseId);

      Assertions.assertNotNull(freemarkerProcessedTextMap);
      Assertions.assertFalse(freemarkerProcessedTextMap.isEmpty());
      Assertions.assertEquals(10, freemarkerProcessedTextMap.size());
   }

   @Test
   void testSuccess_processSubmit() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService, times(0)).sendEmail(any());

      Assertions.assertNotNull(nextStepsWikiPage);
   }

   @Test
   void testFailure_processSubmit_nextStep_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_NEXT_STEPS_BODY_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Create Wizard Next Steps Wiki page: Could not find value for theme.nextsteps.body.template"));
   }

   @Test
   void testFailure_processSubmit_homePage_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_HOME_PAGE_BODY_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Create Course Home Page: Could not find value for theme.homepage.body.template"));
   }

   @Test
   void testFailure_processSubmit_syllabus_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_SYLLABUS_BODY_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Update Syllabus: Could not find value for theme.syllabus.body.template"));
   }

   @Test
   void testFailure_processSubmit_assignment_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      AssignmentGroup assignmentGroup = new AssignmentGroup();
      assignmentGroup.setId(1);

      when(assignmentService.createAssignmentGroup(courseId, "Assignments",
              CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + "me"))
              .thenReturn(assignmentGroup);

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Assignment Creation: Could not find value for theme.assignment.description.template"));
   }

   @Test
   void testFailure_processSubmit_graded_assignment_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_GRADED_ASSIGNMENT_DESCRIPTION_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      AssignmentGroup assignmentGroup = new AssignmentGroup();
      assignmentGroup.setId(1);

      when(assignmentService.createAssignmentGroup(courseId, "Assignments",
              CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + "me"))
              .thenReturn(assignmentGroup);

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Graded Assignment Creation: Could not find value for theme.gradedassignment.description.template"));
   }

   @Test
   void testFailure_processSubmit_quiz_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_QUIZ_DESCRIPTION_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      AssignmentGroup assignmentGroup = new AssignmentGroup();
      assignmentGroup.setId(1);

      when(assignmentService.createAssignmentGroup(courseId, "Assignments",
              CanvasConstants.API_FIELD_SIS_LOGIN_ID + ":" + "me"))
              .thenReturn(assignmentGroup);

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Quiz Assignment Creation: Could not find value for theme.quiz.description.template"));
   }

   @Test
   void testFailure_processSubmit_discussionTopic_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_DISCUSSION_TOPIC_MESSAGE_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Discussion Topic #1 Creation: Could not find value for theme.discussion.topic.message.template"));
   }

   @Test
   void testFailure_processSubmit_announcement_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();
      themeModel.setIncludeGuidance(true);

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_ANNOUNCEMENT_MESSAGE_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Announcement Creation: Could not find value for theme.announcement.message.template"));
   }

   @Test
   void testFailure_processSubmit_createTemplate_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_CREATE_TEMPLATE_PAGE_BODY_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Template Page Creation: Could not find value for theme.template.body.template"));
   }

   @Test
   void testFailure_processSubmit_module_templateNotFound() throws Exception {
      final String courseId = "12345";

      final Theme theme = new Theme();
      theme.setId(2L);

      ThemeModel themeModel = new ThemeModel();

      Iterable<ThemeContent> themeContents = themeContentRepository.findAll();
      List<ThemeContent> newThemeContent = new ArrayList<>();

      for (ThemeContent themeContent : themeContents) {
         if (! themeContent.getName().equals(Constants.THEME_MODULE_PAGE_BODY_TEMPLATE_NAME)) {
            newThemeContent.add(themeContent);
         }
      }

      reset(themeContentRepository);
      when(themeContentRepository.findAll()).thenReturn(new CustomIterable<>(newThemeContent));

      WikiPage nextStepsWikiPage = themeProcessingService.processSubmit(themeModel, courseId, "me");

      verify(emailService).sendEmail(emailCaptor.capture());

      EmailDetails emailDetails = emailCaptor.getValue();
      Assertions.assertNotNull(emailDetails);

      final String body = emailDetails.getBody();
      Assertions.assertNotNull(body);

      Assertions.assertNotNull(nextStepsWikiPage);
      Assertions.assertTrue(body.contains("Module Page Creation: Could not find value for theme.module.body.template"));
   }

   private static class CustomIterable<T> implements Iterable<T> {
      private List<T> elements;

      public CustomIterable(List<T> elements) {
         this.elements = elements;
      }

      @Override
      public Iterator<T> iterator() {
         return elements.iterator();
      }
   }

}