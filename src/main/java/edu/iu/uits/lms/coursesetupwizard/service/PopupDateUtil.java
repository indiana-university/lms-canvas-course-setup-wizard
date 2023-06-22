package edu.iu.uits.lms.coursesetupwizard.service;

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
