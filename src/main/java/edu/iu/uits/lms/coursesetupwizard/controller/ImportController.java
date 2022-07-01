package edu.iu.uits.lms.coursesetupwizard.controller;

import edu.iu.uits.lms.coursesetupwizard.amqp.WizardImportMessage;
import edu.iu.uits.lms.coursesetupwizard.amqp.WizardImportMessageSender;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.coursesetupwizard.model.SelectableCourse;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_BACK;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_HOME;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_NEXT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.CONTENT_OPTION_ALL;
import static edu.iu.uits.lms.coursesetupwizard.Constants.CONTENT_OPTION_SELECT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.DATE_OPTION_ADJUST;
import static edu.iu.uits.lms.coursesetupwizard.Constants.DATE_OPTION_NOCHANGE;
import static edu.iu.uits.lms.coursesetupwizard.Constants.DATE_OPTION_REMOVE;
import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_IMPORT_MODEL;

@Controller
@RequestMapping("/app/import")
@Slf4j
public class ImportController extends WizardController {

   private static final String[] PAGES = {"/app/{0}/index", "/app/import/{0}/selectCourse",
         "/app/import/{0}/selectContent", "/app/import/{0}/adjustDates", "/app/import/{0}/classDates",
         "/app/import/{0}/dayMapping", "/app/import/{0}/review"};

   @Autowired
   private WizardImportMessageSender wizardImportMessageSender;

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
//      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      //For session tracking
      model.addAttribute("customId", httpSession.getId());

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         String selectedCourseId = importModel.getSelectedCourseId();
         model.addAttribute("selectedCourseId", selectedCourseId);
      }

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
         model.addAttribute("selectedCourseLabel", importModel.getSelectedCourseLabel());
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
         if (CONTENT_OPTION_ALL.equalsIgnoreCase(importModel.getImportContentOption())) {
            multiValueMap.add("Content selection", "Import the entire course, all content, and settings");
         } else if (CONTENT_OPTION_SELECT.equalsIgnoreCase(importModel.getImportContentOption())) {
            multiValueMap.add("Content selection", "Select specific content");
         }

         if (DATE_OPTION_REMOVE.equalsIgnoreCase(importModel.getDateOption())) {
            multiValueMap.add("Date adjustments", "Remove all dates");
         } else if (DATE_OPTION_NOCHANGE.equalsIgnoreCase(importModel.getDateOption())) {
            multiValueMap.add("Date adjustments", "Leave dates as is; do not make any changes");
         } else {
            //first/last day of class
            multiValueMap.addAll(importModel.getClassDates().getReviewableValues());

            //day of week adjustments
            multiValueMap.addAll(importModel.getDayChanges().getReviewableValues());
         }
         model.addAttribute("reviewValues", multiValueMap);
      }

      return new ModelAndView("import/review");
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

      switch (action) {
         case ACTION_HOME:
            //Reset stuff
            courseSessionService.removeAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL);
            break;
         case ACTION_BACK:
            pageIndex = currentPage - 1;
            //On the review page, go back to the first date page if we are remove or no change
            if (currentPage == 6 && sessionImportModel.getDateOption() != null &&
                  (DATE_OPTION_REMOVE.equalsIgnoreCase(sessionImportModel.getDateOption()) ||
                        DATE_OPTION_NOCHANGE.equalsIgnoreCase(sessionImportModel.getDateOption()))) {
               pageIndex = currentPage - 3;
            }
            break;
         case ACTION_NEXT:
            pageIndex = currentPage + 1;
            if (importModel.getDateOption() != null) {
               switch (importModel.getDateOption()) {
                  case DATE_OPTION_ADJUST:
                     pageIndex = currentPage + 1;
                     break;
                  case DATE_OPTION_REMOVE:
                     pageIndex = PAGES.length - 1;
                     break;
                  case DATE_OPTION_NOCHANGE:
                     pageIndex = PAGES.length - 1;
                     break;
               }
               sessionImportModel.setDateOption(importModel.getDateOption());
            }

            //Course selection page
            if (importModel.getSelectedCourseId() != null) {
               sessionImportModel.setSelectedCourseId(importModel.getSelectedCourseId());
               sessionImportModel.setSelectedCourseLabel(lookupCourseLabel(importModel.getSelectedCourseId(), oidcTokenUtils.getUserLoginId()));
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
            break;
         case ACTION_SUBMIT:
            WizardImportMessage wim = new WizardImportMessage(sessionImportModel);
            wizardImportMessageSender.send(wim);
            model.addAttribute("redirectUrl", getCanvasContentMigrationsToolUrl(courseId));
            // redirect to the Canvas tool
            return new ModelAndView(redirectToCanvas());
      }
      String url = MessageFormat.format(PAGES[pageIndex], courseId);
      return new ModelAndView("redirect:" + url);
   }

   private String lookupCourseLabel(String selectedCourseId, String userLoginId) {
      List<SelectableCourse> selectableCourses = wizardService.getSelectableCourses(userLoginId);

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

}
