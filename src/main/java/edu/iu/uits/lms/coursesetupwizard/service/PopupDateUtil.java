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

import edu.iu.uits.lms.canvas.helpers.CanvasDateFormatUtil;
import edu.iu.uits.lms.common.date.DateFormatUtil;

import java.util.Date;

/**
 * Date utility class for the popup dismissal dates
 */
public class PopupDateUtil {

   /**
    * Date format used for the input, prior to formatting into an actual java date
    */
   public static final String INPUT_DATE_FORMAT = "yyyy/MM/dd";

   /**
    * Date format used for display purposes.  Will result in "June 22, 2023"
    */
   public static final String DISPLAY_FORMAT = "MMMM d, yyyy";

   /**
    * Validate a string date representation before turning it into a java.util.Date
    * @param dateInput String date.  Hopefully in the form of "yyyy/MM/dd"
    * @return A valid java.util.Date object
    * @throws IllegalArgumentException Throws exception if input date is missing, in the wrong format, or in the past
    */
   public static Date validateDate(String dateInput) throws IllegalArgumentException {
      if (dateInput == null) {
         throw new IllegalArgumentException("Please provide a date");
      }
      Date convertedDate = DateFormatUtil.string2Date(dateInput, INPUT_DATE_FORMAT);
      Date now = new Date();
      if (convertedDate == null) {
         throw new IllegalArgumentException("Please provide a date in the format: " + INPUT_DATE_FORMAT);
      }
      if (convertedDate.before(now)) {
         throw new IllegalArgumentException("Please provide a date in the future");
      }

      return convertedDate;
   }

   /**
    * Convert the date into a display value, in the format of MMMM d, yyyy (i.e. "June 22, 2023")
    * @param date Date value
    * @return Display value
    */
   public static String date2Display(Date date) {
      return CanvasDateFormatUtil.formatDateForDisplay(date, null, DISPLAY_FORMAT);
   }

}
