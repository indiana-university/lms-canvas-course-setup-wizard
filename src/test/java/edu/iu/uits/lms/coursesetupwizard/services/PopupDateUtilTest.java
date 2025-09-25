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

import edu.iu.uits.lms.coursesetupwizard.service.PopupDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
public class PopupDateUtilTest {

   private static final String TIMEZONE = "America/Indianapolis";

   @Test
   void testMissingInputDate() {
      IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate(null, TIMEZONE));
      Assertions.assertEquals("Please provide a date", t.getMessage());
   }

   @Test
   void testBadInputDateFormat() {
      IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate("", TIMEZONE));
      Assertions.assertEquals("Please provide a date in the format: yyyy/MM/dd", t.getMessage());

      t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate("asdf", TIMEZONE));
      Assertions.assertEquals("Please provide a date in the format: yyyy/MM/dd", t.getMessage());
   }

   @Test
   void testInputDateHasPassed() {
      IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate("1999/01/01", TIMEZONE));
      Assertions.assertEquals("Please provide a date in the future", t.getMessage());
   }

   @Test
   void testGoodInputDate() {
      Date d = PopupDateUtil.validateDate("2222/01/03", TIMEZONE);
      Assertions.assertNotNull(d);

      Calendar cal = Calendar.getInstance();
      cal.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
      cal.setTime(d);

      Assertions.assertEquals(2222, cal.get(Calendar.YEAR));
      Assertions.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
      Assertions.assertEquals(3, cal.get(Calendar.DATE));
   }
}
