package edu.iu.uits.lms.coursesetupwizard.services;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2023 Indiana University
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

import edu.iu.uits.lms.coursesetupwizard.config.PostgresDBConfig;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.PopupDismissalDate;
import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import edu.iu.uits.lms.coursesetupwizard.model.WizardUserCourse;
import edu.iu.uits.lms.coursesetupwizard.repository.PopupDismissalDateRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardUserCourseRepository;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

@DataJpaTest
@Import({ToolConfig.class, PostgresDBConfig.class})
@Slf4j
@ActiveProfiles("csw")
public class PopupDismissalDateTest {

   @Autowired
   private WizardService wizardService;

   @Autowired
   private WizardUserCourseRepository wizardUserCourseRepository;

   @Autowired
   private PopupDismissalDateRepository popupDismissalDateRepository;

   @MockBean
   private JwtDecoder jwtDecoder;

   private static final String USER1 = "user1";
   private static final String COURSE1 = "course1";
   private static final String COURSE2 = "course2";
   private static String NOTES1 = "foobar notes!<strong>BOLD</strong>";
   private static String NOTES2 = "notes2wow";
   private static String NOTES3 = "notes_notes_notes";

   private static Date lastWeek;
   private static Date yesterday;
   private static Date nextWeek;

   @BeforeEach
   public void setUp() throws Exception {
      LocalDate now = LocalDate.now();
      LocalDate lastWeekLD = now.minusDays(7);
      LocalDate yesterdayLD = now.minusDays(1);
      LocalDate nextWeekLD = now.plusDays(7);
      lastWeek = makeDate(lastWeekLD.getDayOfMonth(), lastWeekLD.getMonth(), lastWeekLD.getYear());
      yesterday = makeDate(yesterdayLD.getDayOfMonth(), yesterdayLD.getMonth(), yesterdayLD.getYear());
      nextWeek = makeDate(nextWeekLD.getDayOfMonth(), nextWeekLD.getMonth(), nextWeekLD.getYear());

      this.popupDismissalDateRepository.save(PopupDismissalDate.builder().showOn(lastWeek).notes(NOTES1).build());
   }

   @AfterEach
   void tearDown() {
      popupDismissalDateRepository.deleteAll();
      wizardUserCourseRepository.deleteAll();
   }

   @Test
   public void getStatusWithoutDates() {
      // Clear all dates
      popupDismissalDateRepository.deleteAll();

      // User1 has not dismissed for course2, so popup will appear, but the notes will be blank
      PopupStatus status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertNull(status.getNotes());

      // Try to dismiss, but without a date available, no popup or notes
      status = wizardService.dismissPopup(COURSE2, USER1, true);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertNull(status.getNotes());
   }

   @Test
   void userCourseDismissalChecks() {
      // pre dismissal, should have popups for both courses
      PopupStatus status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      // Dismiss for course 1
      status = wizardService.dismissPopup(COURSE1, USER1, false);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      // User1 has dismissed for course1, no popup
      status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      // User1 has not dismissed for course2, so popup will appear
      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());
   }

   @Test
   void userOldDismissalNewDateChecks() {
      // pre dismissal, should have popups for both courses
      PopupStatus status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      // User has dismissed globally "last week".
      wizardUserCourseRepository.save(WizardUserCourse.builder().courseId(WizardUserCourse.GLOBAL).username(USER1).dismissedOn(lastWeek).build());

      // User globally dismissed last week, so popup is gone
      status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      // User globally dismissed last week, so popup is gone
      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES1, status.getNotes());

      // Add new date since dismissal
      popupDismissalDateRepository.save(PopupDismissalDate.builder().showOn(yesterday).notes(NOTES2).build());

      // User globally dismissed last week, new date since, popup is back
      status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES2, status.getNotes());

      // User globally dismissed last week, so popup is gone
      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES2, status.getNotes());
   }

   @Test
   void userDismissalWithFutureDate() {
      // Clear all dates
      popupDismissalDateRepository.deleteAll();

      // pre dismissal, should have popups for both courses, but no notes
      PopupStatus status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertNull(status.getNotes());

      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertNull(status.getNotes());

      //Add future date
      popupDismissalDateRepository.save(PopupDismissalDate.builder().showOn(nextWeek).notes(NOTES3).build());

      // Still no popups for both courses, and no notes
      status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertNull(status.getNotes());

      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertNull(status.getNotes());
   }

   /**
    * Build a {@link Date} from the individual values
    * @param day Day of the month value
    * @param month {@link Month} value
    * @param year Year value
    * @return Date
    */
   @SuppressWarnings("MagicConstant")
   private static Date makeDate(int day, Month month, int year) {
      Calendar cal = Calendar.getInstance();
      cal.set(year, month.ordinal(), day, 0, 0, 0);
      return cal.getTime();
   }

   @TestConfiguration
   static class TestContextConfiguration {
      @Bean
      public WizardService wizardService() {
         return new WizardService();
      }
   }
}
