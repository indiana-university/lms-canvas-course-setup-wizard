package edu.iu.uits.lms.coursesetupwizard.services;

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

import edu.iu.uits.lms.canvas.model.BlueprintAssociatedCourse;
import edu.iu.uits.lms.canvas.model.BlueprintSubscription;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.model.Enrollment;
import edu.iu.uits.lms.canvas.model.User;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.BlueprintService;
import edu.iu.uits.lms.canvas.services.ContentMigrationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import edu.iu.uits.lms.coursesetupwizard.model.SelectableCourse;
import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
import edu.iu.uits.lms.coursesetupwizard.model.WizardUserCourse;
import edu.iu.uits.lms.coursesetupwizard.repository.PopupDismissalDateRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardUserCourseRepository;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.iuonly.services.HierarchyResourceService;
import edu.iu.uits.lms.iuonly.services.TemplateAuditService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes={WizardService.class})
@SpringBootTest
@Slf4j
public class WizardServiceTest {

   @Autowired
   private WizardService wizardService;

   @MockitoBean
   private ToolConfig toolConfig;

   @MockitoBean
   private CourseService courseService;

   @MockitoBean
   private BlueprintService blueprintService;

   @MockitoBean
   private WizardUserCourseRepository wizardUserCourseRepository;

   @MockitoBean
   private WizardCourseStatusRepository wizardCourseStatusRepository;

   @MockitoBean
   private PopupDismissalDateRepository popupDismissalDateRepository = null;

   @MockitoBean
   private ContentMigrationService contentMigrationService;

   @MockitoBean
   private AccountService accountService;

   @MockitoBean
   private HierarchyResourceService hierarchyResourceService;

   @MockitoBean
   private TemplateAuditService templateAuditService;

   @Test
   void testFailureWithNullCourseId_CourseIdEligibleBlueprintSettingsDestination() {
      boolean result = wizardService.isEligibleBlueprintSettingsDestination(null);

      Assertions.assertFalse(result);
   }

   @Test
   void testSuccess_CourseIdEligibleBlueprintSettingsDestination() {
      final String courseId = "12345";

      Course course = new Course();
      course.setId(courseId);

      when(courseService.getCourse(courseId)).thenReturn(course);

      boolean result = wizardService.isEligibleBlueprintSettingsDestination(courseId);

      Assertions.assertTrue(result);
   }

   @Test
   void testFailureWithEnrollments_CourseIdEligibleBlueprintSettingsDestination() {
      final String courseId = "12345";

      Course course = new Course();
      course.setId(courseId);

      when(courseService.getCourse(courseId)).thenReturn(course);
      when(courseService.getUsersForCourseByType(any(), any(), any()))
              .thenReturn(List.of(new User()));

      boolean result = wizardService.isEligibleBlueprintSettingsDestination(courseId);

      Assertions.assertFalse(result);
   }

   @Test
   void testFailureDestinationIsAlreadyAssociatedWithBlueprint_CourseIdEligibleBlueprintSettingsDestination() {
      final String courseId = "12345";

      Course course = new Course();
      course.setId(courseId);

      BlueprintSubscription blueprintSubscription = new BlueprintSubscription();
      blueprintSubscription.setId("1");
      blueprintSubscription.setBlueprintCourse(new BlueprintAssociatedCourse());

      when(courseService.getCourse(courseId)).thenReturn(course);
      when(blueprintService.getSubscriptions(courseId)).thenReturn(List.of(blueprintSubscription));

      boolean result = wizardService.isEligibleBlueprintSettingsDestination(courseId);

      Assertions.assertFalse(result);
   }

   @Test
   void testFailureDestinationIsAlreadyABlueprint_CourseIdEligibleBlueprintSettingsDestination() {
      final String courseId = "12345";

      Course course = new Course();
      course.setId(courseId);
      course.setBlueprint(true);

      when(courseService.getCourse(courseId)).thenReturn(course);

      boolean result = wizardService.isEligibleBlueprintSettingsDestination(courseId);

      Assertions.assertFalse(result);
   }

   @Test
   void testFailureWithSisCourse_CourseIdEligibleBlueprintSettingsDestination() {
      final String courseId = "12345";

      Course course = new Course();
      course.setId(courseId);
      course.setSisCourseId("abcd");

      when(courseService.getCourse(courseId)).thenReturn(course);

      boolean result = wizardService.isEligibleBlueprintSettingsDestination(courseId);

      Assertions.assertFalse(result);
   }

   @Test
   void testPopupShown() {
      when(wizardUserCourseRepository.findByUsernameAndCourseId(anyString(), anyString())).thenReturn(null);
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(null);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertTrue(status.isShowPopup());
   }

   @Test
   void testPopupNotShownBecauseComplete() {
      when(wizardUserCourseRepository.findByUsernameAndCourseId(anyString(), anyString())).thenReturn(null);

      WizardCourseStatus wcs = new WizardCourseStatus();
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(wcs);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertFalse(status.isShowPopup());
   }

   @Test
   void testPopupNotShownBecauseDismissed() {
      WizardUserCourse wcu = new WizardUserCourse();

      when(wizardUserCourseRepository.findByUsernameAndCourseId(anyString(), anyString())).thenReturn(wcu);
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(null);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertFalse(status.isShowPopup());
   }

   @Test
   void testPopupNotShownBecauseEither() {
      WizardUserCourse wcu = new WizardUserCourse();

      when(wizardUserCourseRepository.findByUsernameAndCourseId(anyString(), anyString())).thenReturn(wcu);

      WizardCourseStatus wcs = new WizardCourseStatus();
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(wcs);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertFalse(status.isShowPopup());
   }

   @Test
   void testUserEnrollmentStateActiveOnly_getSelectableCourses() {
      final String networkId = "me";
      final List<String> courseStates = Arrays.asList("available", "unpublished", "completed");

      final Enrollment goodEnrollment = new Enrollment();
      goodEnrollment.setType("teacher");
      goodEnrollment.setEnrollmentState("active");

      final Enrollment badEnrollment = new Enrollment();
      badEnrollment.setType("teacher");
      badEnrollment.setEnrollmentState("invited");

      final String badCourseId    = "Bad CourseId";
      final String badCourseName  = "Bad Course Name";
      final String goodCourseId   = "Good CourseId";
      final String goodCourseName = "Good Course Name";

      final Course badCourse = new Course();
      badCourse.setId(badCourseId);
      badCourse.setName(badCourseName);
      badCourse.setEnrollments(List.of(badEnrollment));

      final Course goodCourse = new Course();
      goodCourse.setId(goodCourseId);
      goodCourse.setName(goodCourseName);
      goodCourse.setEnrollments(List.of(goodEnrollment));

      when(courseService.getCoursesForUser(networkId, false, true, false, courseStates))
              .thenReturn(List.of(badCourse, goodCourse));

      final List<SelectableCourse> badListSelectableCourses = wizardService.getSelectableCourses(networkId, "courseId");

      Assertions.assertNotNull(badListSelectableCourses);
      Assertions.assertEquals(1, badListSelectableCourses.size());
      Assertions.assertEquals(goodCourseId, badListSelectableCourses.get(0).getValue());
      Assertions.assertEquals(goodCourseName, badListSelectableCourses.get(0).getLabel());
   }
}
