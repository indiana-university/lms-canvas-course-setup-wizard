package edu.iu.uits.lms.coursesetupwizard.controller;

import edu.iu.uits.lms.coursesetupwizard.model.SelectableCourse;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.List;

@RestController
@RequestMapping("/tool")
@Slf4j
public class BackendToolController extends WizardController {

   @GetMapping("/{courseId}/courses")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public List<SelectableCourse> getCourses(@PathVariable("courseId") String courseId) {
      log.debug("in /tool/{}/courses", courseId);
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      return wizardService.getSelectableCourses(oidcTokenUtils.getUserLoginId(), courseId);
   }
}
