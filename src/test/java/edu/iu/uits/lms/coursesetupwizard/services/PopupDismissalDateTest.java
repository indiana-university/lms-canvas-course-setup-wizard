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
import java.util.List;

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
   private static final String USER2 = "user2";
   private static final String USER3 = "user3";
   private static final String COURSE1 = "course1";
   private static final String COURSE2 = "course2";
   private static String NOTES = "foobar notes!<string>BOLD</strong>";

   private static Date lastWeek;
   private static Date nextWeek;

   @BeforeEach
   public void setUp() throws Exception {
      LocalDate now = LocalDate.now();
      LocalDate lastWeekLD = now.minusDays(7);
      LocalDate nextWeekLD = now.plusDays(7);
      lastWeek = makeDate(lastWeekLD.getDayOfMonth(), lastWeekLD.getMonth(), lastWeekLD.getYear());
      nextWeek = makeDate(nextWeekLD.getDayOfMonth(), nextWeekLD.getMonth(), nextWeekLD.getYear());

      this.popupDismissalDateRepository.save(PopupDismissalDate.builder().dismissUntil(lastWeek).build());
      this.popupDismissalDateRepository.save(PopupDismissalDate.builder().dismissUntil(nextWeek).notes(NOTES).build());
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

      // Try to dismiss, but without a date available
      status = wizardService.dismissPopup(COURSE2, USER1, true);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertNull(status.getNotes());

   }

   @Test
   void user1Checks() {
      // pre dismissal, should have popups for both courses
      PopupStatus status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // Dismiss for course 1
      status = wizardService.dismissPopup(COURSE1, USER1, false);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // User1 has dismissed for course1, no popup
      status = wizardService.getPopupDismissedStatus(COURSE1, USER1);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // User1 has not dismissed for course2, so popup will appear
      status = wizardService.getPopupDismissedStatus(COURSE2, USER1);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());
   }

   @Test
   void user2Checks() {
      // pre dismissal, should have popups for both courses
      PopupStatus status = wizardService.getPopupDismissedStatus(COURSE1, USER2);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      status = wizardService.getPopupDismissedStatus(COURSE2, USER2);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // User2 has dismissed globally, until "last week".
      wizardUserCourseRepository.save(WizardUserCourse.builder().courseId(WizardUserCourse.GLOBAL).username(USER2).dismissedUntil(lastWeek).build());

      // User2 globally dismissed until last week, so popup should be back
      status = wizardService.getPopupDismissedStatus(COURSE1, USER2);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // User2 globally dismissed until last week, so popup should be back
      status = wizardService.getPopupDismissedStatus(COURSE2, USER2);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());
   }

   @Test
   void user3Checks() {
      // pre dismissal, should have popups for both courses
      PopupStatus status = wizardService.getPopupDismissedStatus(COURSE1, USER3);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      status = wizardService.getPopupDismissedStatus(COURSE2, USER3);
      Assertions.assertTrue(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // User3 has dismissed globally, until "next week".
      status = wizardService.dismissPopup(COURSE1, USER3, true);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // User3 globally dismissed until next week, so no popup
      status = wizardService.getPopupDismissedStatus(COURSE1, USER3);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());

      // User3 globally dismissed until next week, so no popup
      status = wizardService.getPopupDismissedStatus(COURSE2, USER3);
      Assertions.assertFalse(status.isShowPopup());
      Assertions.assertEquals(NOTES, status.getNotes());
   }

   @Test
   void testAdjustDates() {
      IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            wizardService.adjustDates(null));
      Assertions.assertEquals("No dismissal date found. Please ensure one has been created via the /rest/popupDates endpoint, or directly in the DB before trying again.", t.getMessage());

      wizardUserCourseRepository.save(WizardUserCourse.builder().courseId(WizardUserCourse.GLOBAL).username(USER1).dismissedUntil(nextWeek).build());

      PopupDismissalDate dismissalDate = popupDismissalDateRepository.getNextDismissalDate();
      List<WizardUserCourse> adjustedDates = wizardService.adjustDates(dismissalDate);
      Assertions.assertEquals(1, adjustedDates.size());
      Assertions.assertEquals(dismissalDate.getDismissUntil(), adjustedDates.get(0).getDismissedUntil());

      dismissalDate = popupDismissalDateRepository.getPreviousDismissalDate();
      adjustedDates = wizardService.adjustDates(dismissalDate);
      Assertions.assertEquals(1, adjustedDates.size());
      Assertions.assertEquals(dismissalDate.getDismissUntil(), adjustedDates.get(0).getDismissedUntil());

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
