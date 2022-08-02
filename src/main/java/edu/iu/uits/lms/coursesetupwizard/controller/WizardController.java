package edu.iu.uits.lms.coursesetupwizard.controller;

import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpSession;

import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_IMPORT_MODEL;

@Controller
@RequestMapping("/app")
@Slf4j
public class WizardController extends OidcTokenAwareController {

    @Autowired
    protected ToolConfig toolConfig = null;

    @Autowired
    protected WizardService wizardService = null;

    @Autowired
    protected CanvasService canvasService = null;

    @Autowired
    protected CourseSessionService courseSessionService;

    @RequestMapping("/loading")
    public String loading(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String courseId = oidcTokenUtils.getCourseId();

        model.addAttribute("courseId", courseId);
        model.addAttribute("hideFooter", true);
        model.addAttribute("toolPath", "/app/" + courseId + "/index");
        return "loading";
    }

    @RequestMapping("/{courseId}/index")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView index(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /index");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        model.addAttribute("courseId", courseId);
        courseSessionService.removeAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL);

        // Go to the regular index page if wizard hasn't been run for this course yet, otherwise, go to the page with the warning message on it
        String viewName = wizardService.alreadyCompletedForCourse(courseId) ? "alreadyCompleted" : "index";
        return new ModelAndView(viewName);
    }

    @PostMapping("/{courseId}/menu")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView menuChoice(@PathVariable("courseId") String courseId, Model model, @RequestParam("menuChoice") String menuChoice,
                                   HttpSession httpSession) {
        log.debug("in /menu");
        OidcAuthenticationToken token = getValidatedToken(courseId);
//        model.addAttribute("courseId", courseId);
        ImportModel importModel = new ImportModel();
        importModel.setMenuChoice(menuChoice);
        importModel.setCourseId(courseId);
        courseSessionService.addAttributeToSession(httpSession, courseId, KEY_IMPORT_MODEL, importModel);

        Constants.MAIN_OPTION mainOption = Constants.MAIN_OPTION.valueOf(menuChoice);
        switch (mainOption) {
            case IMPORT:
                return new ModelAndView("redirect:/app/import/" + courseId + "/selectCourse");
            case TEMPLATE:
                return new ModelAndView("redirect:/app/template/" + courseId + "/choose");
            case HOMEPAGE:
                return new ModelAndView("redirect:/app/homepage/" + courseId + "/homePage");
            default:
                return new ModelAndView("index");
        }

    }

    @PostMapping("/{courseId}/exit")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView exit(@PathVariable("courseId") String courseId, Model model) {
        model.addAttribute("redirectUrl", getCanvasCourseToolUrl(courseId));
        // redirect to the Canvas course home page
        return new ModelAndView(redirectToCanvas());
    }

    @RequestMapping(value = "/redirectToCanvas")
    public String redirectToCanvas() {
        return "redirectToCanvas";
    }

    protected String getCanvasContentMigrationsToolUrl(String courseId) {
        return canvasService.getBaseUrl() + "/courses/" + courseId + "/content_migrations";
    }

    protected String getCanvasCourseToolUrl(String courseId) {
        return canvasService.getBaseUrl() + "/courses/" + courseId;
    }

}
