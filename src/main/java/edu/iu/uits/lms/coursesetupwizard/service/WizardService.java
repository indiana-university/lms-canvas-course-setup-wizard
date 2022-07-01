package edu.iu.uits.lms.coursesetupwizard.service;

import edu.iu.uits.lms.canvas.helpers.ContentMigrationHelper;
import edu.iu.uits.lms.canvas.model.ContentMigration;
import edu.iu.uits.lms.canvas.model.ContentMigrationCreateWrapper;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.services.ContentMigrationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import edu.iu.uits.lms.coursesetupwizard.model.SelectableCourse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.coursesetupwizard.Constants.CONTENT_OPTION_SELECT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.DATE_OPTION_ADJUST;
import static edu.iu.uits.lms.coursesetupwizard.Constants.DATE_OPTION_REMOVE;
import static edu.iu.uits.lms.coursesetupwizard.Constants.INSTRUCTOR_COURSES_CACHE_NAME;

@Service
@Slf4j
public class WizardService {

   @Autowired
   private CourseService courseService;

   @Autowired
   private ContentMigrationService contentMigrationService;

   public PopupStatus getPopupDismissedStatus(String courseId, String userId) {
      //TODO Add correct implementation (LMSA-8259)
      throw new NotImplementedException();
   }

   public PopupStatus dismissPopup(String courseId, String userId, boolean global) {
      //TODO Add correct implementation (LMSA-8259)
      throw new NotImplementedException();
   }

   @Cacheable(value = INSTRUCTOR_COURSES_CACHE_NAME, cacheManager = "CourseSetupWizardCacheManager")
   public List<SelectableCourse> getSelectableCourses(String networkId) {
      List<Course> courses = courseService.getCoursesTaughtBy(networkId, true, false, true);
      courses.sort(Comparator.comparing((Course c) -> c.getTerm().getStartAt(), Comparator.nullsFirst(Comparator.reverseOrder()))
            .thenComparing(Course::getSisCourseId, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Course::getName));

      return courses.stream().map(SelectableCourse::new)
            .collect(Collectors.toList());
   }

   public void doCourseImport(ImportModel importModel) {
      String courseId = importModel.getCourseId();
      String sourceCourseId = importModel.getSelectedCourseId();

      //Update course front page
      courseService.updateCourseFrontPage(courseId, "modules");

      //Start the content migration
      ContentMigrationCreateWrapper wrapper = new ContentMigrationCreateWrapper();
      ContentMigrationCreateWrapper.Settings settings = new ContentMigrationCreateWrapper.Settings();
      wrapper.setMigrationType(ContentMigrationHelper.MIGRATION_TYPE_COURSE_COPY);
      wrapper.setSettings(settings);
      settings.setSourceCourseId(sourceCourseId);

      //selective importing
      wrapper.setSelectiveImport(CONTENT_OPTION_SELECT.equalsIgnoreCase(importModel.getImportContentOption()));

      if (DATE_OPTION_ADJUST.equalsIgnoreCase(importModel.getDateOption()) ||
            DATE_OPTION_REMOVE.equalsIgnoreCase(importModel.getDateOption())) {
         ContentMigrationCreateWrapper.DateShiftOptions dateShifts = new ContentMigrationCreateWrapper.DateShiftOptions();
         wrapper.setDateShiftOptions(dateShifts);

         //remove dates or not
         dateShifts.setRemoveDates(DATE_OPTION_REMOVE.equalsIgnoreCase(importModel.getDateOption()));
         //shift dates or not
         dateShifts.setShiftDates(DATE_OPTION_ADJUST.equalsIgnoreCase(importModel.getDateOption()));

         //date shifting
         ImportModel.ClassDates classDates = importModel.getClassDates();
         if (classDates != null) {
            dateShifts.setOldStartDate(StringUtils.trimToNull(classDates.getOrigFirst()));
            dateShifts.setOldEndDate(StringUtils.trimToNull(classDates.getOrigLast()));
            dateShifts.setNewStartDate(StringUtils.trimToNull(classDates.getThisFirst()));
            dateShifts.setNewEndDate(StringUtils.trimToNull(classDates.getThisLast()));
         }

         //day substitutions
         ImportModel.DayChanges dayChanges = importModel.getDayChanges();
         if (dayChanges != null) {
            Map<String, String> daySubs = dayChanges.getValuesForMigration();
            dateShifts.setDaySubstitutions(daySubs);
         }
      }

      log.debug("{}", wrapper);

      ContentMigration cm = contentMigrationService.initiateContentMigration(courseId, null, wrapper);
      log.info("{}", cm);
   }

}
