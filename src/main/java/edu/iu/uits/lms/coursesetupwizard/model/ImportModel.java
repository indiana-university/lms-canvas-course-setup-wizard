package edu.iu.uits.lms.coursesetupwizard.model;

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

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class ImportModel implements Serializable {
   private String courseId;
   private String menuChoice;
   private String selectedCourseId;
   private String selectedCourseLabel;
   private String importContentOption;
   private String dateOption;
   private ClassDates classDates;
   private DayChanges dayChanges;
   private String selectedTemplateId;
   private String selectedTemplateName;

   @Data
   /**
    * ClassDates are in DATE_FORMAT format and need to be converted to Canvas format before submission
    */
   public static class ClassDates implements Serializable {
      private String origFirst;
      private String origLast;
      private String currentFirst;
      private String currentLast;

      public static final String DATE_FORMAT = "M/d/uuuu";

      public MultiValueMap<String, String> getReviewableValues() {
         MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
         multiValueMap.add("Original course", MessageFormat.format("First day of class: {0}", formatDateForReview(getOrigFirst())));
         multiValueMap.add("Original course", MessageFormat.format("Last day of class: {0}", formatDateForReview(getOrigLast())));
         multiValueMap.add("Current course", MessageFormat.format("First day of class: {0}", formatDateForReview(getCurrentFirst())));
         multiValueMap.add("Current course", MessageFormat.format("Last day of class: {0}", formatDateForReview(getCurrentLast())));

         return multiValueMap;
      }

      private String formatDateForReview(String date) {
         return date != null && !date.isBlank() ? date : "Nothing entered";
      }

   }

   @Data
   public static class DayChanges implements Serializable {
      private static final String NOCHANGE = "nochange";

      private String sundayChangeTo;
      private String mondayChangeTo;
      private String tuesdayChangeTo;
      private String wednesdayChangeTo;
      private String thursdayChangeTo;
      private String fridayChangeTo;
      private String saturdayChangeTo;

      private Map<String, String> getDayMappings() {
         Map<String, String> dayMap = new HashMap<>();
         dayMap.put("0", "Sunday");
         dayMap.put("1", "Monday");
         dayMap.put("2", "Tuesday");
         dayMap.put("3", "Wednesday");
         dayMap.put("4", "Thursday");
         dayMap.put("5", "Friday");
         dayMap.put("6", "Saturday");
         return dayMap;
      }

      public MultiValueMap<String, String> getReviewableValues() {
         MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
         List<String> results = new ArrayList<>();

         if (!NOCHANGE.equalsIgnoreCase(sundayChangeTo)) {
            results.add(MessageFormat.format("Sunday changing to {0}", getDayMappings().get(sundayChangeTo)));
         }
         if (!NOCHANGE.equalsIgnoreCase(mondayChangeTo)) {
            results.add(MessageFormat.format("Monday changing to {0}", getDayMappings().get(mondayChangeTo)));
         }
         if (!NOCHANGE.equalsIgnoreCase(tuesdayChangeTo)) {
            results.add(MessageFormat.format("Tuesday changing to {0}", getDayMappings().get(tuesdayChangeTo)));
         }
         if (!NOCHANGE.equalsIgnoreCase(wednesdayChangeTo)) {
            results.add(MessageFormat.format("Wednesday changing to {0}", getDayMappings().get(wednesdayChangeTo)));
         }
         if (!NOCHANGE.equalsIgnoreCase(thursdayChangeTo)) {
            results.add(MessageFormat.format("Thursday changing to {0}", getDayMappings().get(thursdayChangeTo)));
         }
         if (!NOCHANGE.equalsIgnoreCase(fridayChangeTo)) {
            results.add(MessageFormat.format("Friday changing to {0}", getDayMappings().get(fridayChangeTo)));
         }
         if (!NOCHANGE.equalsIgnoreCase(saturdayChangeTo)) {
            results.add(MessageFormat.format("Saturday changing to {0}", getDayMappings().get(saturdayChangeTo)));
         }

         // In case no days were changed
         if (results.size() == 0) {
            results.add("No changes");
         }

         multiValueMap.addAll("Day of the week adjustments", results);
         return multiValueMap;
      }

      public Map<String, String> getValuesForMigration() {
         Map<String, String> map = new HashMap<>();

         if (!NOCHANGE.equalsIgnoreCase(sundayChangeTo)) {
            map.put("0", sundayChangeTo);
         }
         if (!NOCHANGE.equalsIgnoreCase(mondayChangeTo)) {
            map.put("1", mondayChangeTo);
         }
         if (!NOCHANGE.equalsIgnoreCase(tuesdayChangeTo)) {
            map.put("2", tuesdayChangeTo);
         }
         if (!NOCHANGE.equalsIgnoreCase(wednesdayChangeTo)) {
            map.put("3", wednesdayChangeTo);
         }
         if (!NOCHANGE.equalsIgnoreCase(thursdayChangeTo)) {
            map.put("4", thursdayChangeTo);
         }
         if (!NOCHANGE.equalsIgnoreCase(fridayChangeTo)) {
            map.put("5", fridayChangeTo);
         }
         if (!NOCHANGE.equalsIgnoreCase(saturdayChangeTo)) {
            map.put("6", saturdayChangeTo);
         }

         return map;
      }
   }
}
