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

import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import jakarta.servlet.http.HttpSession;
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


import static edu.iu.uits.lms.coursesetupwizard.Constants.WizardFeature;
import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_IMPORT_MODEL;
import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_THEME_MODEL;

@Controller
@RequestMapping("/app")
@Slf4j
public class WizardController extends OidcTokenAwareController {

    @Autowired
    protected FeatureAccessServiceImpl featureAccessService;

    @Autowired
    protected ToolConfig toolConfig = null;

    @Autowired
    protected WizardService wizardService = null;

    @Autowired
    protected CanvasService canvasService = null;

    @Autowired
    protected CourseSessionService courseSessionService;

    @RequestMapping({"/launch", "/loading"})
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
        courseSessionService.removeAttributeFromSession(httpSession, courseId, KEY_THEME_MODEL);

        if (featureAccessService.isFeatureEnabledForAccount(WizardFeature.THEME_FRONTPAGE.id, canvasService.getRootAccount(), null)) {
            model.addAttribute("feature_theme_frontpage", true);
        }

        // Go to the regular index page if wizard hasn't been run for this course yet, otherwise, go to the page with the warning message on it
        String viewName = wizardService.alreadyCompletedForCourse(courseId) ? "alreadyCompleted" : "index";
        ModelAndView modelAndView = new ModelAndView(viewName);

        if (featureAccessService.isFeatureEnabledForAccount(WizardFeature.THEME_FRONTPAGE.id, canvasService.getRootAccount(), null)) {
            modelAndView.getModelMap().addAttribute("feature_theme_frontpage", true);
        }

        return modelAndView;
    }

    @PostMapping("/{courseId}/menu")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView menuChoice(@PathVariable("courseId") String courseId, Model model, @RequestParam("menuChoice") String menuChoice,
                                   HttpSession httpSession) {
        log.debug("in /menu");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
//        model.addAttribute("courseId", courseId);
        ImportModel importModel = new ImportModel();
        importModel.setMenuChoice(menuChoice);
        importModel.setCourseId(courseId);
        courseSessionService.addAttributeToSession(httpSession, courseId, KEY_IMPORT_MODEL, importModel);

        Constants.MAIN_OPTION mainOption = Constants.MAIN_OPTION.valueOf(menuChoice);
        switch (mainOption) {
            case IMPORT:
                return new ModelAndView("redirect:/app/import/" + oidcTokenUtils.getCourseId() + "/selectCourse");
            case TEMPLATE:
                return new ModelAndView("redirect:/app/template/" + oidcTokenUtils.getCourseId() + "/choose");
            case HOMEPAGE:
                return new ModelAndView("redirect:/app/homepage/" + oidcTokenUtils.getCourseId() + "/homePage");
            case THEME:
                return new ModelAndView("redirect:/app/theme/" + oidcTokenUtils.getCourseId() + "/intro");
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
