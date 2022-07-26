package edu.iu.uits.lms.coursesetupwizard.controller;

import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.iuonly.model.HierarchyResource;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpSession;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_BACK;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_HOME;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_NEXT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_IMPORT_MODEL;

@Controller
@RequestMapping("/app/template")
@Slf4j
public class TemplateController extends WizardController {

   private static final String[] PAGES = {"/app/{0}/index", "/app/template/{0}/choose",
           "/app/template/{0}/review"};

   @GetMapping("/{courseId}/choose")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView choose(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /choose");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      Map<String, List<HierarchyResource>> templatesForCourse = wizardService.getTemplatesForCourse(courseId);

      model.addAttribute("templatesForCourse", templatesForCourse);

      return new ModelAndView("template/choose");
   }

   @GetMapping("/{courseId}/review")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView review(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /choose");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

//      Map<String, List<HierarchyResource>> templatesForCourse = wizardService.getTemplatesForCourse(courseId);

//      model.addAttribute("templatesForCourse", templatesForCourse);

      return new ModelAndView("template/review");
   }

   @PostMapping("/{courseId}/navigate")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView navigate(@PathVariable("courseId") String courseId, Model model, @ModelAttribute ImportModel importModel,
                                @RequestParam(name = "action") String action, @RequestParam(name = "currentPage") int currentPage,
                                HttpSession httpSession) {
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

//      ImportModel sessionImportModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);

      int pageIndex = 0;
//
//      Constants.DATE_OPTION dateOption = EnumUtils.getEnum(Constants.DATE_OPTION.class, importModel.getDateOption());
//      Constants.DATE_OPTION sessionDateOption = EnumUtils.getEnum(Constants.DATE_OPTION.class, sessionImportModel.getDateOption());

      switch (action) {
         case ACTION_HOME:
            //Reset stuff
            courseSessionService.removeAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL);
            break;
         case ACTION_BACK:
            pageIndex = currentPage - 1;
            break;
         case ACTION_NEXT:
            pageIndex = currentPage + 1;

            //Course selection page
//            if (importModel.getSelectedCourseId() != null) {
//               sessionImportModel.setSelectedCourseId(importModel.getSelectedCourseId());
//               sessionImportModel.setSelectedCourseLabel(lookupCourseLabel(courseId, importModel.getSelectedCourseId(), oidcTokenUtils.getUserLoginId()));
//            }
//            //Content selection page
//            if (importModel.getImportContentOption() != null) {
//               sessionImportModel.setImportContentOption(importModel.getImportContentOption());
//            }
//            if (importModel.getClassDates() != null) {
//               sessionImportModel.setClassDates(importModel.getClassDates());
//            }
//            if (importModel.getDayChanges() != null) {
//               sessionImportModel.setDayChanges(importModel.getDayChanges());
//            }

            //Re-save the session model
//            courseSessionService.addAttributeToSession(httpSession, courseId, KEY_IMPORT_MODEL, sessionImportModel);
            break;
//         case ACTION_SUBMIT:
//            wizardService.doCourseImport(sessionImportModel, oidcTokenUtils.getUserLoginId());
//            model.addAttribute("redirectUrl", getCanvasContentMigrationsToolUrl(courseId));
//            // redirect to the Canvas tool
//            return new ModelAndView(redirectToCanvas());
      }
      String url = MessageFormat.format(PAGES[pageIndex], courseId);
      return new ModelAndView("redirect:" + url);
   }
}
