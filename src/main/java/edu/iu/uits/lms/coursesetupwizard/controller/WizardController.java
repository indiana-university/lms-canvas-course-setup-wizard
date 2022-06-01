package edu.iu.uits.lms.coursesetupwizard.controller;

import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/app")
@Slf4j
public class WizardController extends OidcTokenAwareController {

    @Autowired
    private ToolConfig toolConfig = null;

    @Autowired
    WizardService courseUnlockerService = null;

    @RequestMapping("/launch")
    public ModelAndView launch(Model model, HttpServletRequest request) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String courseId = oidcTokenUtils.getCourseId();

        return index(courseId, model, request);
    }

    @RequestMapping("/index/{courseId}")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView index(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
        log.debug("in /index");
        OidcAuthenticationToken token = getValidatedToken(courseId);

        return new ModelAndView("index");
    }

}
