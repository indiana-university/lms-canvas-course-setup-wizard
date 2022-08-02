package edu.iu.uits.lms.coursesetupwizard.service;

import edu.iu.uits.lms.canvas.helpers.ContentMigrationHelper;
import edu.iu.uits.lms.canvas.model.ContentMigration;
import edu.iu.uits.lms.canvas.model.ContentMigrationCreateWrapper;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.ContentMigrationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import edu.iu.uits.lms.coursesetupwizard.model.SelectableCourse;
import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
import edu.iu.uits.lms.coursesetupwizard.model.WizardUserCourse;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardUserCourseRepository;
import edu.iu.uits.lms.iuonly.model.HierarchyResource;
import edu.iu.uits.lms.iuonly.model.StoredFile;
import edu.iu.uits.lms.iuonly.model.coursetemplating.CourseTemplatesWrapper;
import edu.iu.uits.lms.iuonly.services.HierarchyResourceException;
import edu.iu.uits.lms.iuonly.services.HierarchyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
   private HierarchyResourceService hierarchyResourceService;

   @Autowired
   private ToolConfig toolConfig;

   public PopupStatus getPopupDismissedStatus(String courseId, String userId) {
      List<WizardUserCourse> records = wizardUserCourseRepository.findByUsernameAndCourseIdOrGlobal(userId, courseId);
      boolean alreadyCompleted = alreadyCompletedForCourse(courseId);
      return new PopupStatus(courseId, userId, (!CollectionUtils.isEmpty(records) && !alreadyCompleted));
   }

   public PopupStatus dismissPopup(String courseId, String userId, boolean global) {
      String coureIdToCheck = global ? WizardUserCourse.GLOBAL : courseId;
      WizardUserCourse record = wizardUserCourseRepository.findByUsernameAndCourseId(userId, coureIdToCheck);

      if (record == null) {
         record = WizardUserCourse.builder().username(userId).courseId(coureIdToCheck).build();
      }
      wizardUserCourseRepository.save(record);

      return new PopupStatus(courseId, userId, true);
   }

   @Cacheable(value = INSTRUCTOR_COURSES_CACHE_NAME, cacheManager = "CourseSetupWizardCacheManager")
   public List<SelectableCourse> getSelectableCourses(String networkId, String currentCourseId) {
      List<Course> courses = courseService.getCoursesTaughtBy(networkId, true, false, true);
      courses.sort(Comparator.comparing((Course c) -> c.getTerm().getStartAt(), Comparator.nullsFirst(Comparator.reverseOrder()))
            .thenComparing(Course::getSisCourseId, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Course::getName));

      //Filter out current course
      return courses.stream()
            .filter(c -> !currentCourseId.equals(c.getId()))
            .map(SelectableCourse::new)
            .collect(Collectors.toList());
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
               dateShifts.setOldStartDate(StringUtils.trimToNull(classDates.getOrigFirst()));
               dateShifts.setOldEndDate(StringUtils.trimToNull(classDates.getOrigLast()));
               dateShifts.setNewStartDate(StringUtils.trimToNull(classDates.getCurrentFirst()));
               dateShifts.setNewEndDate(StringUtils.trimToNull(classDates.getCurrentLast()));
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
      } else {
         throw new WizardServiceException("Unable to perform course import");
      }
   }

   public void doApplyTemplate(ImportModel importModel, String userLoginId, String appBaseUrl) throws WizardServiceException {
      String courseId = importModel.getCourseId();

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
         } else {
            throw new WizardServiceException("Unable to apply template to course");
         }
      }
   }

   public void doSetHomepage(ImportModel importModel, String userLoginId, String appBaseUrl) throws WizardServiceException {
      String courseId = importModel.getCourseId();

      //Update course front page
      courseService.updateCourseFrontPage(courseId, "modules");

      //Get the specific template
      String homepageTemplateId = toolConfig.getHomepageTemplateId();

      HierarchyResource templateForCourse = null;
      try {
         templateForCourse = hierarchyResourceService.getTemplate(Long.parseLong(homepageTemplateId));
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
            wizardCourseStatus.setSelectedTemplateId(homepageTemplateId);
            wizardCourseStatus.setContentMigrationId(cm.getId());
            wizardCourseStatusRepository.save(wizardCourseStatus);
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
               .collect(Collectors.groupingBy(HierarchyResource::getNode));
      }
      return nodeMap;
   }

}
