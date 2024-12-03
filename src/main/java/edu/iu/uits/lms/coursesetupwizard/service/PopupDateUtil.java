package edu.iu.uits.lms.coursesetupwizard.service;

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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date utility class for the popup dismissal dates
 */
@Slf4j
public class PopupDateUtil {

   /**
    * Date format used for the input, prior to formatting into an actual java date
    */
   public static final String INPUT_DATE_FORMAT = "yyyy/MM/dd";
   public static final String INPUT_DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";

   /**
    * Validate a string date representation before turning it into a java.util.Date
    * @param dateInput String date.  Hopefully in the form of "yyyy/MM/dd"
    * @return A valid java.util.Date object
    * @throws IllegalArgumentException Throws exception if input date is missing, in the wrong format, or in the past
    */
   public static Date validateDate(String dateInput, String timezone) throws IllegalArgumentException {
      return validateDate(dateInput, INPUT_DATE_FORMAT, timezone);
   }

   /**
    * Validate a string date representation before turning it into a java.util.Date
    * @param dateInput String date.  Hopefully in the form of "yyyy/MM/dd"
    * @param dateFormat
    * @return A valid java.util.Date object
    * @throws IllegalArgumentException Throws exception if input date is missing, in the wrong format, or in the past
    */
   public static Date validateDate(String dateInput, String dateFormat, String timezone) throws IllegalArgumentException {
      if (dateInput == null) {
         throw new IllegalArgumentException("Please provide a date");
      }
      Date convertedDate = string2Date(dateInput, dateFormat, timezone);
      validate(convertedDate, dateFormat);

      return convertedDate;
   }

   /**
    * Check if valid popup date. Throws IllegalArgumentException with validation failure details
    * @param popupDate
    * @param dateFormat - used for the returned error message only to describe your expected format
    */
   public static void validate(Date popupDate, String dateFormat) {
      Date now = new Date();
      if (popupDate == null) {
         throw new IllegalArgumentException("Please provide a date in the format: " + dateFormat);
      }
      if (popupDate.before(now)) {
         throw new IllegalArgumentException("Please provide a date in the future");
      }
   }

   /**
    * Turn a string into a Date
    * @param dateString Input date string
    * @param formatString Format of the input date string
    * @param timezone Timezone used in the SimpleDateFormat
    * @return Date representation of the input string
    */
   private static Date string2Date(String dateString, @NonNull String formatString, String timezone) {
      if (dateString != null) {
         DateFormat format = new SimpleDateFormat(formatString);
         format.setTimeZone(TimeZone.getTimeZone(ZoneId.of(timezone)));
         try {
            return format.parse(dateString);
         } catch (ParseException pe) {
            log.error("Error parsing date string", pe);
         }
      }
      return null;
   }

}
