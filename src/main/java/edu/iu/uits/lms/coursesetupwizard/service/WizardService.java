package edu.iu.uits.lms.coursesetupwizard.service;

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

import edu.iu.uits.lms.canvas.helpers.ContentMigrationHelper;
import edu.iu.uits.lms.canvas.helpers.EnrollmentHelper;
import edu.iu.uits.lms.canvas.model.BlueprintAssociatedCourse;
import edu.iu.uits.lms.canvas.model.BlueprintSubscription;
import edu.iu.uits.lms.canvas.model.ContentMigration;
import edu.iu.uits.lms.canvas.model.ContentMigrationCreateWrapper;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.model.User;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.BlueprintService;
import edu.iu.uits.lms.canvas.services.ContentMigrationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.coursesetupwizard.model.PopupDismissalDate;
import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import edu.iu.uits.lms.coursesetupwizard.model.SelectableCourse;
import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
import edu.iu.uits.lms.coursesetupwizard.model.WizardUserCourse;
import edu.iu.uits.lms.coursesetupwizard.repository.PopupDismissalDateRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardUserCourseRepository;
import edu.iu.uits.lms.iuonly.model.HierarchyResource;
import edu.iu.uits.lms.iuonly.model.StoredFile;
import edu.iu.uits.lms.iuonly.model.coursetemplating.CourseTemplatesWrapper;
import edu.iu.uits.lms.iuonly.services.HierarchyResourceException;
import edu.iu.uits.lms.iuonly.services.HierarchyResourceService;
import edu.iu.uits.lms.iuonly.services.TemplateAuditService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.coursesetupwizard.Constants.COURSE_TEMPLATES_CACHE_NAME;
import static edu.iu.uits.lms.coursesetupwizard.Constants.INSTRUCTOR_COURSES_CACHE_NAME;

@Service
@Slf4j
public class WizardService {

   @Autowired
   private CourseService courseService;

   @Autowired
   private ContentMigrationService contentMigrationService;

   @Autowired
   private WizardUserCourseRepository wizardUserCourseRepository;

   @Autowired
   private WizardCourseStatusRepository wizardCourseStatusRepository;

   @Autowired
   private AccountService accountService;

   @Autowired
   private BlueprintService blueprintService;

   @Autowired
   private HierarchyResourceService hierarchyResourceService;

   @Autowired
   private TemplateAuditService templateAuditService;

   @Autowired
   private PopupDismissalDateRepository popupDismissalDateRepository;

   @Autowired
   private ToolConfig toolConfig;

   @Transactional(transactionManager = "cswTransactionMgr")
   public PopupStatus getPopupDismissedStatus(String courseId, String userId) {
      WizardUserCourse courseRecord = wizardUserCourseRepository.findByUsernameAndCourseId(userId, courseId);
      WizardUserCourse globalRecord = wizardUserCourseRepository.findByUsernameAndCourseId(userId, WizardUserCourse.GLOBAL);
      boolean alreadyCompleted = alreadyCompletedForCourse(courseId);
      PopupDismissalDate pdd = getPreviousDismissalDate();

      boolean noDismissals = courseRecord == null && globalRecord == null;
      boolean courseDismissal = courseRecord != null;
      boolean expiredDismissal = globalRecord != null && pdd != null && globalRecord.getDismissedOn().before(pdd.getShowOn()) && pdd.getShowOn().before(new Date());

      /*
      Popup shows if:
      - There are no dismissals at all (course or global)
      - Wizard has not already been completed for this course
      - No course specific dismissal
      - No pdd date
      - There is a global dismissal, but it happened before a previous pdd date
       */
      boolean showPopup = (noDismissals || expiredDismissal) && !courseDismissal && !alreadyCompleted;

      String notes = pdd != null ? pdd.getNotes() : null;
      return new PopupStatus(courseId, userId, showPopup, notes);
   }

   @Transactional(transactionManager = "cswTransactionMgr")
   public PopupStatus dismissPopup(String courseId, String userId, boolean global) {
      String courseIdToCheck = global ? WizardUserCourse.GLOBAL : courseId;
      WizardUserCourse record = wizardUserCourseRepository.findByUsernameAndCourseId(userId, courseIdToCheck);

      if (record == null) {
         record = WizardUserCourse.builder().username(userId).courseId(courseIdToCheck).build();
      }

      if (WizardUserCourse.GLOBAL.equals(courseIdToCheck)) {
         record.setDismissedOn(new Date());
      }
      wizardUserCourseRepository.save(record);

      return getPopupDismissedStatus(courseId, userId);
   }

   @Cacheable(value = INSTRUCTOR_COURSES_CACHE_NAME, cacheManager = "CourseSetupWizardCacheManager")
   public List<SelectableCourse> getSelectableCourses(String networkId, String currentCourseId) {
      List<String> courseStates = Arrays.asList("available", "unpublished", "completed");

      List<Course> courses = courseService.getCoursesForUser(networkId, false, true, false, courseStates);
      List<String> wantedEnrollments = Arrays.asList("teacher", "ta", "designer");
      List<String> wantedEnrollmentStates = Arrays.asList("active");

      // Filter out current course, then
      // filter it to keep courses where the user is enrolled as an enrollment type in wantedEnrollments
      return courses.stream()
            .filter(c -> !currentCourseId.equals(c.getId()))
            .filter(c -> !c.isAccessRestrictedByDate())
            .filter(c->c.getEnrollments().stream().anyMatch(enr -> wantedEnrollments.contains(enr.getType())))
            .filter(c->c.getEnrollments().stream().anyMatch(enr -> wantedEnrollmentStates.contains(enr.getEnrollmentState())))
            .sorted(Comparator.comparing((Course c) -> c.getTerm() == null ? null : c.getTerm().getStartAt(), Comparator.nullsFirst(Comparator.reverseOrder()))
              .thenComparing(Course::getSisCourseId, Comparator.nullsLast(Comparator.naturalOrder()))
              .thenComparing(Course::getName))
            .map(SelectableCourse::new)
            .collect(Collectors.toList());
   }

   public boolean isBlueprintCourse(String courseId) {
      boolean isBpCourse = false;

      if (courseId != null) {
         Course course = courseService.getCourse(courseId);

         if (course != null) {
            isBpCourse = course.isBlueprint();
         }
      }

      return isBpCourse;
   }

   /**
    * Returns whether this courseId is eligible to have Blueprint settings copied
    * in to.
    * @param courseId
    * @return
    */
   public boolean isEligibleBlueprintSettingsDestination(String courseId) {
      if (courseId == null || courseId.isEmpty()) {
         return false;
      }

      Course course = courseService.getCourse(courseId);

      if (course == null) {
         return false;
      }

      final String courseLogFormat = "\"%s (id = %s)\"";

      if (course.isBlueprint()) {
         log.debug("Course {} is ineligible for blueprint settings copy into it because it is already a blueprint course",
                 String.format(courseLogFormat, course.getName(), courseId ));
         return false;
      }

      String sisCourseId = course.getSisCourseId();

      if (sisCourseId != null && ! sisCourseId.isEmpty()) {
         log.debug("Course {} is ineligible for blueprint settings copy into it because it is an SIS course",
                 String.format(courseLogFormat, course.getName(), courseId ));
         return false;
      }

      List<BlueprintSubscription> blueprintCourseSubscriptions = blueprintService.getSubscriptions(courseId)
              .stream()
              .filter(bps -> bps.getBlueprintCourse() != null)
              .sorted(Comparator.comparing(BlueprintSubscription::getId))
              .toList();

      if (! blueprintCourseSubscriptions.isEmpty()) {
         BlueprintAssociatedCourse blueprintAssociatedCourse = blueprintCourseSubscriptions.get(0).getBlueprintCourse();
         log.debug("Course {} is ineligible for blueprint settings copy into it because it is already associated with blueprint course {}",
                 String.format(courseLogFormat, course.getName(), courseId ),
                 String.format(courseLogFormat, blueprintAssociatedCourse.getName(), blueprintAssociatedCourse.getId()));
         return false;
      }

      List<User> ineligibleEnrollmentsUsers = courseService.getUsersForCourseByType(courseId,
              List.of(EnrollmentHelper.TYPE_STUDENT, EnrollmentHelper.TYPE_OBSERVER),
              List.of(EnrollmentHelper.STATE.active.name(),
                      EnrollmentHelper.STATE.completed.name(),
                      EnrollmentHelper.STATE.creation_pending.name(),
                      EnrollmentHelper.STATE.current_and_concluded.name(),
                      EnrollmentHelper.STATE.current_and_future.name(),
                      EnrollmentHelper.STATE.current_and_invited.name(),
                      EnrollmentHelper.STATE.invited.name()
              ));

      if (ineligibleEnrollmentsUsers != null && ! ineligibleEnrollmentsUsers.isEmpty()) {
         log.debug("Course {} is ineligible for blueprint settings copy into it because it has students or observers enrolled in it",
                 String.format(courseLogFormat, course.getName(), courseId ));
         return false;
      }

       return true;
   }

   public void doCourseImport(ImportModel importModel, String userLoginId) throws WizardServiceException {
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

      if (Constants.CONTENT_OPTION.ALL_WITH_BLUEPRINT_SETTINGS.name().equalsIgnoreCase(importModel.getImportContentOption())) {
         log.info("Import Blueprint settings = true");
         settings.setImportBlueprintSettings(true);
      }

      //selective importing
      wrapper.setSelectiveImport(Constants.CONTENT_OPTION.SELECT.name().equalsIgnoreCase(importModel.getImportContentOption()));

      if (Constants.DATE_OPTION.ADJUST.name().equalsIgnoreCase(importModel.getDateOption()) ||
            Constants.DATE_OPTION.REMOVE.name().equalsIgnoreCase(importModel.getDateOption())) {
         ContentMigrationCreateWrapper.DateShiftOptions dateShifts = new ContentMigrationCreateWrapper.DateShiftOptions();
         wrapper.setDateShiftOptions(dateShifts);

         //remove dates or not
         dateShifts.setRemoveDates(Constants.DATE_OPTION.REMOVE.name().equalsIgnoreCase(importModel.getDateOption()));
         //shift dates or not
         dateShifts.setShiftDates(Constants.DATE_OPTION.ADJUST.name().equalsIgnoreCase(importModel.getDateOption()));

         //Only set the stuff if we are adjusting.
         // This handles the case where user went through the wizard, but went back and chose other options that were different before submitting
         if (Constants.DATE_OPTION.ADJUST.name().equalsIgnoreCase(importModel.getDateOption())) {
            //date shifting
            ImportModel.ClassDates classDates = importModel.getClassDates();
            if (classDates != null) {
               dateShifts.setOldStartDate(formatDateForSubmit(classDates.getOrigFirst()));
               dateShifts.setOldEndDate(formatDateForSubmit(classDates.getOrigLast()));
               dateShifts.setNewStartDate(formatDateForSubmit(classDates.getCurrentFirst()));
               dateShifts.setNewEndDate(formatDateForSubmit(classDates.getCurrentLast()));
            }

            //day substitutions
            ImportModel.DayChanges dayChanges = importModel.getDayChanges();
            if (dayChanges != null) {
               Map<String, String> daySubs = dayChanges.getValuesForMigration();
               dateShifts.setDaySubstitutions(daySubs);
            }
         }
      }

      log.debug("{}", wrapper);

      ContentMigration cm = contentMigrationService.initiateContentMigration(courseId, null, wrapper);
      log.info("{}", cm);

      if (cm != null) {
         Constants.MAIN_OPTION mainOption = Constants.MAIN_OPTION.valueOf(importModel.getMenuChoice());
         Constants.CONTENT_OPTION contentOption = EnumUtils.getEnum(Constants.CONTENT_OPTION.class, importModel.getImportContentOption());
         Constants.DATE_OPTION dateOption = EnumUtils.getEnum(Constants.DATE_OPTION.class, importModel.getDateOption());

         WizardCourseStatus wizardCourseStatus = new WizardCourseStatus();
         wizardCourseStatus.setCourseId(courseId);
         wizardCourseStatus.setCompletedBy(userLoginId);
         wizardCourseStatus.setContentOption(contentOption);
         wizardCourseStatus.setDateAdjustmentOption(dateOption);
         wizardCourseStatus.setMainOption(mainOption);
         wizardCourseStatus.setContentMigrationId(cm.getId());
         wizardCourseStatusRepository.save(wizardCourseStatus);

         //Do full audit
         templateAuditService.audit(courseId, sourceCourseId, "WIZARD_COURSE_IMPORT", userLoginId);
      } else {
         throw new WizardServiceException("Unable to perform course import");
      }
   }

   public void doApplyTemplate(ImportModel importModel, String userLoginId, String appBaseUrl) throws WizardServiceException {
      String courseId = importModel.getCourseId();

      //Update course front page
      courseService.updateCourseFrontPage(courseId, "modules");

      HierarchyResource templateForCourse = null;
      try {
         templateForCourse = hierarchyResourceService.getTemplate(Long.parseLong(importModel.getSelectedTemplateId()));
      } catch (HierarchyResourceException e) {
         throw new WizardServiceException("Unable to get template");
      }

      if (templateForCourse != null) {
         StoredFile storedFile = templateForCourse.getStoredFile();

         //Url for the file download.  Should come from iu-custom, but might be self-hosted, or from some other app (api-portal, etc)
         String fileUrl = MessageFormat.format("{0}/rest/iu/file/download/{1}/{2}", appBaseUrl, storedFile.getId(), storedFile.getDisplayName());

         ContentMigrationCreateWrapper wrapper = new ContentMigrationCreateWrapper();
         ContentMigrationCreateWrapper.Settings settings = new ContentMigrationCreateWrapper.Settings();
         wrapper.setMigrationType(ContentMigrationHelper.MIGRATION_TYPE_CC);
         wrapper.setSettings(settings);

         settings.setFileUrl(fileUrl);

         ContentMigration cm = contentMigrationService.initiateContentMigration(courseId, null, wrapper);
         log.info("{}", cm);

         if (cm != null) {
            Constants.MAIN_OPTION mainOption = Constants.MAIN_OPTION.valueOf(importModel.getMenuChoice());

            WizardCourseStatus wizardCourseStatus = new WizardCourseStatus();
            wizardCourseStatus.setCourseId(courseId);
            wizardCourseStatus.setCompletedBy(userLoginId);
            wizardCourseStatus.setMainOption(mainOption);
            wizardCourseStatus.setSelectedTemplateId(importModel.getSelectedTemplateId());
            wizardCourseStatus.setContentMigrationId(cm.getId());
            wizardCourseStatusRepository.save(wizardCourseStatus);

            //Do full audit
            templateAuditService.audit(courseId, templateForCourse, "WIZARD_TEMPLATE", userLoginId);
         } else {
            throw new WizardServiceException("Unable to apply template to course");
         }
      }
   }

   public void doSetHomepage(ImportModel importModel, String userLoginId, String appBaseUrl) throws WizardServiceException {
      String courseId = importModel.getCourseId();

      //Update course front page
      courseService.updateCourseFrontPage(courseId, "modules");

      HierarchyResource templateForCourse = null;
      try {
         templateForCourse = hierarchyResourceService.getHomePageTemplate();
      } catch (HierarchyResourceException e) {
         throw new WizardServiceException("Unable to get template");
      }

      if (templateForCourse != null) {
         StoredFile storedFile = templateForCourse.getStoredFile();

         //Url for the file download.  Should come from iu-custom, but might be self-hosted, or from some other app (api-portal, etc)
         String fileUrl = MessageFormat.format("{0}/rest/iu/file/download/{1}/{2}", appBaseUrl, storedFile.getId(), storedFile.getDisplayName());

         ContentMigrationCreateWrapper wrapper = new ContentMigrationCreateWrapper();
         ContentMigrationCreateWrapper.Settings settings = new ContentMigrationCreateWrapper.Settings();
         wrapper.setMigrationType(ContentMigrationHelper.MIGRATION_TYPE_CC);
         wrapper.setSettings(settings);

         settings.setFileUrl(fileUrl);

         ContentMigration cm = contentMigrationService.initiateContentMigration(courseId, null, wrapper);
         log.info("{}", cm);

         if (cm != null) {
            Constants.MAIN_OPTION mainOption = Constants.MAIN_OPTION.valueOf(importModel.getMenuChoice());

            WizardCourseStatus wizardCourseStatus = new WizardCourseStatus();
            wizardCourseStatus.setCourseId(courseId);
            wizardCourseStatus.setCompletedBy(userLoginId);
            wizardCourseStatus.setMainOption(mainOption);
            wizardCourseStatus.setSelectedTemplateId(templateForCourse.getId().toString());
            wizardCourseStatus.setContentMigrationId(cm.getId());
            wizardCourseStatusRepository.save(wizardCourseStatus);

            //Do full audit
            templateAuditService.audit(courseId, templateForCourse, "WIZARD_HOMEPAGE", userLoginId);
         } else {
            throw new WizardServiceException("Unable to set course homepage");
         }
      }
   }

   public boolean alreadyCompletedForCourse(String courseId) {
      WizardCourseStatus wcs = wizardCourseStatusRepository.findByCourseId(courseId);
      return wcs != null;
   }

   @Cacheable(value = COURSE_TEMPLATES_CACHE_NAME, cacheManager = "CourseSetupWizardCacheManager")
   public Map<String, List<HierarchyResource>> getTemplatesForCourse(String courseId) {
      Map<String, List<HierarchyResource>> nodeMap = new HashMap<>();
      CourseTemplatesWrapper wrapper = null;
      try {
         wrapper = hierarchyResourceService.getAvailableTemplatesForCanvasCourse(courseId);
      } catch (HierarchyResourceException e) {
         log.error("error getting templates for course", e);
      }

      if (wrapper != null) {
         List<HierarchyResource> resources = wrapper.getTemplates();

         nodeMap = resources.stream()
               .filter(t-> !(t.isDefaultTemplate() && toolConfig.getHierarchyRootNodeName().equals(t.getNode())))
               .sorted(Comparator.comparing(HierarchyResource::getDisplayName))
               .collect(Collectors.groupingBy(HierarchyResource::getNode));
      }
      return nodeMap;
   }


   private String formatDateForSubmit(String date) {
      String canvasDate = StringUtils.trimToNull(date);
      if (canvasDate != null && !"".equals(canvasDate)) {
         // change to expected Canvas format
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ImportModel.ClassDates.DATE_FORMAT, Locale.getDefault());
         LocalDate myLocalDate = LocalDate.parse(canvasDate, dtf);

         canvasDate = myLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
      }

      return canvasDate;
   }

   /**
    * Get the most recent, previous PopupDismissalDate.
    * @return PopupDismissalDate
    */
   private PopupDismissalDate getPreviousDismissalDate() {
      List<PopupDismissalDate> dates = popupDismissalDateRepository.getPreviousDismissalDates();
      if (dates != null && !dates.isEmpty()) {
         // Should already be ordered, but just in case!
         dates.sort(Comparator.comparing(PopupDismissalDate::getShowOn).reversed());
         return dates.get(0);
      }
      return null;

   }

}
