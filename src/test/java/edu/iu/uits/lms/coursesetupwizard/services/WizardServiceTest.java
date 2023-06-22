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

import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.ContentMigrationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes={WizardService.class})
@SpringBootTest
@Slf4j
public class WizardServiceTest {

   @Autowired
   private WizardService wizardService;

   @MockBean
   private ToolConfig toolConfig;

   @MockBean
   private CourseService courseService;

   @MockBean
   private WizardUserCourseRepository wizardUserCourseRepository;

   @MockBean
   private WizardCourseStatusRepository wizardCourseStatusRepository;

   @MockBean
   private PopupDismissalDateRepository popupDismissalDateRepository = null;

   @MockBean
   private ContentMigrationService contentMigrationService;

   @MockBean
   private AccountService accountService;

   @MockBean
   private HierarchyResourceService hierarchyResourceService;

   @MockBean
   private TemplateAuditService templateAuditService;

   @Test
   void testPopupShown() {
      when(wizardUserCourseRepository.findByUsernameAndCourseIdOrGlobal(anyString(), anyString())).thenReturn(Collections.EMPTY_LIST);
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(null);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertTrue(status.isShowPopup());
   }

   @Test
   void testPopupNotShownBecauseComplete() {
      when(wizardUserCourseRepository.findByUsernameAndCourseIdOrGlobal(anyString(), anyString())).thenReturn(Collections.EMPTY_LIST);

      WizardCourseStatus wcs = new WizardCourseStatus();
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(wcs);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertFalse(status.isShowPopup());
   }

   @Test
   void testPopupNotShownBecauseDismissed() {
      List<WizardUserCourse> list = new ArrayList<>();
      list.add(new WizardUserCourse());

      when(wizardUserCourseRepository.findByUsernameAndCourseIdOrGlobal(anyString(), anyString())).thenReturn(list);
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(null);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertFalse(status.isShowPopup());
   }

   @Test
   void testPopupNotShownBecauseEither() {
      List<WizardUserCourse> list = new ArrayList<>();
      list.add(new WizardUserCourse());

      when(wizardUserCourseRepository.findByUsernameAndCourseIdOrGlobal(anyString(), anyString())).thenReturn(list);

      WizardCourseStatus wcs = new WizardCourseStatus();
      when(wizardCourseStatusRepository.findByCourseId(anyString())).thenReturn(wcs);

      PopupStatus status = wizardService.getPopupDismissedStatus("foo", "bar");
      Assertions.assertFalse(status.isShowPopup());
   }
}
