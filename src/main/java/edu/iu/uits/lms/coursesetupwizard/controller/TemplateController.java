package edu.iu.uits.lms.coursesetupwizard.controller;

import edu.iu.uits.lms.iuonly.model.HierarchyResource;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/app/template")
@Slf4j
public class TemplateController extends WizardController {

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
}
