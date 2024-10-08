package edu.iu.uits.lms.coursesetupwizard.controller;

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

import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.coursesetupwizard.model.SelectableCourse;
import edu.iu.uits.lms.coursesetupwizard.service.WizardServiceException;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.io.Serializable;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_BACK;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_HOME;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_NEXT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_IMPORT_MODEL;

@Controller
@RequestMapping("/app/import")
@Slf4j
public class ImportController extends WizardController {

   private static final String[] PAGES = {"/app/{0}/index", "/app/import/{0}/selectCourse",
         "/app/import/{0}/selectContent", "/app/import/{0}/adjustDates", "/app/import/{0}/classDates",
         "/app/import/{0}/dayMapping", "/app/import/{0}/review", "/app/import/{0}/submit"};

   /**
    * Gets called before EVERY controller method
    * @param courseId Expected to be extracted as a path variable
    * @param model model
    */
   @ModelAttribute
   public void addCommonAttributesToModel(@PathVariable("courseId") String courseId, Model model) {
      model.addAttribute("courseId", courseId);
      model.addAttribute("importSteps", getImportSteps(courseId));
   }

   /**
    * Get the display values and links for the import wizard step indicator
    * @param courseId Course ID to insert into the links
    * @return
    */
   private List<ImportStep> getImportSteps(String courseId) {
      List<ImportStep> steps = new ArrayList<>();
      steps.add(new ImportStep("Select course", MessageFormat.format(PAGES[1], courseId)));
      steps.add(new ImportStep("Select content", MessageFormat.format(PAGES[2], courseId)));
      steps.add(new ImportStep("Adjust dates", MessageFormat.format(PAGES[3], courseId)));
      steps.add(new ImportStep("Review and submit", MessageFormat.format(PAGES[6], courseId)));
      return steps;
   }

   @GetMapping("/{courseId}/selectCourse")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView selectCourse(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /selectCourse");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         String selectedCourseId = importModel.getSelectedCourseId();
         model.addAttribute("selectedCourseId", selectedCourseId);
      }

      // Look up courses in here instead of a browser call
      List<SelectableCourse> coursesList = wizardService.getSelectableCourses(oidcTokenUtils.getUserLoginId(), courseId);
      model.addAttribute("courses", coursesList);

      return new ModelAndView("import/selectCourse");
   }

   @GetMapping("/{courseId}/selectContent")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView selectContent(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /selectContent");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         // in case the user gets past the select content screen without selecting a course, add this attribute
         // and redirect to selectCourse again
         if (importModel.getSelectedCourseLabel() == null) {
            model.addAttribute("noImportSelected", true);

            // Look up courses in here instead of a browser call
            // Unlikely this would have anything if we're in this block of code, but look anyway
            List<SelectableCourse> coursesList = wizardService.getSelectableCourses(oidcTokenUtils.getUserLoginId(), courseId);
            model.addAttribute("courses", coursesList);

            return new ModelAndView("import/selectCourse");
         } else {
            model.addAttribute("selectedCourseLabel", importModel.getSelectedCourseLabel());

            if (wizardService.isEligibleBlueprintSettingsDestination(courseId)) {
               model.addAttribute("isBlueprintFromCourse", wizardService.isBlueprintCourse(importModel.getSelectedCourseId()));
            }
         }
      }

      return new ModelAndView("import/selectContent");
   }

   @GetMapping("/{courseId}/adjustDates")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView adjustDates(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /adjustDates");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         model.addAttribute("selectedCourseLabel", importModel.getSelectedCourseLabel());
      }

      return new ModelAndView("import/configureDates1");
   }

   @GetMapping("/{courseId}/classDates")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView classDates(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /adjustDates");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         model.addAttribute("selectedCourseLabel", importModel.getSelectedCourseLabel());
         model.addAttribute("selectedClassDates", importModel.getClassDates());
      }

      return new ModelAndView("import/configureDates2");
   }

   @GetMapping("/{courseId}/dayMapping")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView dayMapping(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /dayMapping");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      Map<String, String> selectedDayChanges = new HashMap<>();

      if (importModel != null) {
         // validate the dates
         ImportModel.ClassDates classDates = importModel.getClassDates();
         boolean isError = false;
         if (!isValid(classDates.getCurrentFirst(), ImportModel.ClassDates.DATE_FORMAT)) {
            isError = true;
            model.addAttribute("currentStartError", true);
         }

         if (!isValid(classDates.getCurrentLast(), ImportModel.ClassDates.DATE_FORMAT)) {
            isError = true;
            model.addAttribute("currentLastError", true);
         }

         if (!isValid(classDates.getOrigFirst(), ImportModel.ClassDates.DATE_FORMAT)) {
            isError = true;
            model.addAttribute("origStartError", true);
         }

         if (!isValid(classDates.getOrigLast(), ImportModel.ClassDates.DATE_FORMAT)) {
            isError = true;
            model.addAttribute("origLastError", true);
         }

         if (isError) {
            return classDates(courseId, model, httpSession);
         }

         model.addAttribute("selectedCourseLabel", importModel.getSelectedCourseLabel());

         ImportModel.DayChanges dc = importModel.getDayChanges();

         if (dc != null) {
            selectedDayChanges.put("sunday", dc.getSundayChangeTo());
            selectedDayChanges.put("monday", dc.getMondayChangeTo());
            selectedDayChanges.put("tuesday", dc.getTuesdayChangeTo());
            selectedDayChanges.put("wednesday", dc.getWednesdayChangeTo());
            selectedDayChanges.put("thursday", dc.getThursdayChangeTo());
            selectedDayChanges.put("friday", dc.getFridayChangeTo());
            selectedDayChanges.put("saturday", dc.getSaturdayChangeTo());
         }
      }
      model.addAttribute("selectedDayChanges", selectedDayChanges);

      return new ModelAndView("import/configureDates3");
   }

   @GetMapping("/{courseId}/review")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView review(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /review");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         model.addAttribute("selectedCourseLabel", importModel.getSelectedCourseLabel());

         MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
         //Content selection
         Constants.CONTENT_OPTION contentOption = Constants.CONTENT_OPTION.valueOf(importModel.getImportContentOption());
         if (Constants.CONTENT_OPTION.ALL.equals(contentOption)) {
            multiValueMap.add("Content selection", "Import course content only");
         } else if(Constants.CONTENT_OPTION.ALL_WITH_BLUEPRINT_SETTINGS.equals(contentOption)) {
            multiValueMap.add("Content selection", "Import course content with Blueprint Course settings");
         } else if(Constants.CONTENT_OPTION.SELECT.equals(contentOption)) {
            multiValueMap.add("Content selection", "Select specific content");
         }

         Constants.DATE_OPTION dateOption = Constants.DATE_OPTION.valueOf(importModel.getDateOption());
         switch (dateOption) {
            case REMOVE:
               multiValueMap.add("Date adjustments", "Remove all dates");
               break;
            case NOCHANGE:
               multiValueMap.add("Date adjustments", "Leave dates as is; do not make any changes");
               break;
            default:
               //first/last day of class
               multiValueMap.addAll(importModel.getClassDates().getReviewableValues());

               //day of week adjustments
               multiValueMap.addAll(importModel.getDayChanges().getReviewableValues());
               break;
         }
         model.addAttribute("reviewValues", multiValueMap);
      }

      return new ModelAndView("import/review");
   }

   @GetMapping("/{courseId}/submit")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView submit(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /submit");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         model.addAttribute("selectedCourseLabel", importModel.getSelectedCourseLabel());

         MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
         //Content selection
         Constants.CONTENT_OPTION contentOption = Constants.CONTENT_OPTION.valueOf(importModel.getImportContentOption());
         model.addAttribute("selectedContentOption", contentOption.name());

         //Date option selection
         Constants.DATE_OPTION dateOption = Constants.DATE_OPTION.valueOf(importModel.getDateOption());
         model.addAttribute("selectedDateOption", dateOption.name());
      }
      return new ModelAndView("import/submit");

   }

   @PostMapping("/{courseId}/navigate")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView navigate(@PathVariable("courseId") String courseId, Model model, @ModelAttribute ImportModel importModel,
                                @RequestParam(name = "action") String action, @RequestParam(name = "currentPage") int currentPage,
                                HttpSession httpSession) {
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel sessionImportModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);

      int pageIndex = 0;

      Constants.DATE_OPTION dateOption = EnumUtils.getEnum(Constants.DATE_OPTION.class, importModel.getDateOption());
      Constants.DATE_OPTION sessionDateOption = EnumUtils.getEnum(Constants.DATE_OPTION.class, sessionImportModel.getDateOption());

      switch (action) {
         case ACTION_HOME:
            //Reset stuff
            courseSessionService.removeAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL);
            break;
         case ACTION_BACK:
            pageIndex = currentPage - 1;
            //On the review page, go back to the first date page if we are remove or no change
            if (currentPage == 6 && sessionImportModel.getDateOption() != null &&
                  (Constants.DATE_OPTION.REMOVE.equals(sessionDateOption) ||
                        Constants.DATE_OPTION.NOCHANGE.equals(sessionDateOption))) {
               pageIndex = currentPage - 3;
            }
            break;
         case ACTION_NEXT:
            pageIndex = currentPage + 1;
            if (dateOption != null) {
               switch (dateOption) {
                  case ADJUST:
                     pageIndex = currentPage + 1;
                     break;
                  case REMOVE:
                  case NOCHANGE:
                     pageIndex = PAGES.length - 2;
                     //Clear out the values if we don't need them (in case we already set them, then went back and changed the option)
                     sessionImportModel.setClassDates(null);
                     sessionImportModel.setDayChanges(null);
                     break;
               }
               sessionImportModel.setDateOption(dateOption.name());
            }

            //Course selection page
            if (importModel.getSelectedCourseId() != null) {
               sessionImportModel.setSelectedCourseId(importModel.getSelectedCourseId());
               sessionImportModel.setSelectedCourseLabel(lookupCourseLabel(courseId, importModel.getSelectedCourseId(), oidcTokenUtils.getUserLoginId()));
            }
            //Content selection page
            if (importModel.getImportContentOption() != null) {
               sessionImportModel.setImportContentOption(importModel.getImportContentOption());
            }
            if (importModel.getClassDates() != null) {
               sessionImportModel.setClassDates(importModel.getClassDates());
            }
            if (importModel.getDayChanges() != null) {
               sessionImportModel.setDayChanges(importModel.getDayChanges());
            }

            //Re-save the session model
            courseSessionService.addAttributeToSession(httpSession, courseId, KEY_IMPORT_MODEL, sessionImportModel);
            break;
         case ACTION_SUBMIT:
            try {
               wizardService.doCourseImport(sessionImportModel, oidcTokenUtils.getUserLoginId());
               model.addAttribute("redirectUrl", getCanvasContentMigrationsToolUrl(courseId));
               // redirect to the Canvas tool
               return new ModelAndView(redirectToCanvas());
            } catch (WizardServiceException e) {
               model.addAttribute("submitError", e.getMessage());
               return review(courseId, model, httpSession);
            }
      }
      String url = MessageFormat.format(PAGES[pageIndex], courseId);
      return new ModelAndView("redirect:" + url);
   }

   private String lookupCourseLabel(String currentCourseId, String selectedCourseId, String userLoginId) {
      List<SelectableCourse> selectableCourses = wizardService.getSelectableCourses(userLoginId, currentCourseId);

      Map<String, String> courseMap = selectableCourses.stream()
            .collect(Collectors.toMap(SelectableCourse::getValue, SelectableCourse::getLabel, (a, b) -> b));

      return courseMap.get(selectedCourseId);
   }

   @Data
   @AllArgsConstructor
   public static class ImportStep implements Serializable {
      private String name;
      private String link;
   }

   /**
    *
    * @param date
    * @param dateFormat
    * @return true if the given date string is a valid date
    */
   public boolean isValid(String date, String dateFormat) {
      boolean valid = false;

      // empty dates are allowed
      if (date == null || date.isEmpty()) {
         valid = true;
      } else {
         try {
            // ResolverStyle.STRICT for 30, 31 days checking, and also leap year.
            LocalDate.parse(date,
                    DateTimeFormatter.ofPattern(dateFormat)
                            .withResolverStyle(ResolverStyle.STRICT)
            );
            valid = true;

         } catch (DateTimeParseException e) {
            valid = false;
         }
      }

      return valid;
   }

}
