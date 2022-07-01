package edu.iu.uits.lms.coursesetupwizard.model;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

   @Data
   public static class ClassDates implements Serializable {
      private String origFirst;
      private String origLast;
      private String thisFirst;
      private String thisLast;

      public MultiValueMap<String, String> getReviewableValues() {
         MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
         multiValueMap.add("First day of class", MessageFormat.format("Original course: {0}", formatDateForReview(getOrigFirst())));
         multiValueMap.add("First day of class", MessageFormat.format("This course: {0}", formatDateForReview(getThisFirst())));
         multiValueMap.add("Last day of class", MessageFormat.format("Original course: {0}", formatDateForReview(getOrigLast())));
         multiValueMap.add("Last day of class", MessageFormat.format("This course: {0}", formatDateForReview(getThisLast())));

         return multiValueMap;
      }

      private String formatDateForReview(String date) {
         return "".equalsIgnoreCase(date) || date == null ? "N/A" : date;
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