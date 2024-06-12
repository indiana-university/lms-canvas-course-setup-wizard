package edu.iu.uits.lms.coursesetupwizard.controller;

import edu.iu.uits.lms.coursesetupwizard.model.BannerImageCategory;
import edu.iu.uits.lms.coursesetupwizard.model.Theme;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeModel;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeRepository;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_BACK;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_HOME;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_NEXT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_THEME_MODEL;

@Controller
@RequestMapping("/app/theme")
@Slf4j
public class ThemeController extends WizardController {
    @Autowired
    private BannerImageCategoryRepository bannerImageCategoryRepository;

    @Autowired
    private BannerImageRepository bannerImageRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private static final String[] PAGES = {"/app/{0}/index", "/app/theme/{0}/intro", "/app/theme/{0}/selectTheme",
            "/app/theme/{0}/selectBanner", "/app/theme/{0}/navigation", "/app/theme/{0}/guidance",
            "/app/theme/{0}/review", "/app/theme/{0}/submit"};

    @Data
    @AllArgsConstructor
    public static class ThemeStep implements Serializable {
        private String name;
        private String link;
    }

    /**
     * Gets called before EVERY controller method
     * @param courseId Expected to be extracted as a path variable
     * @param model model
     */
    @ModelAttribute
    public void addCommonAttributesToModel(@PathVariable("courseId") String courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("themeSteps", getThemeSteps(courseId));
    }

    /**
     * Get the display values and links for the import wizard step indicator
     * @param courseId Course ID to insert into the links
     * @return
     */
    private List<ThemeStep> getThemeSteps(String courseId) {
        List<ThemeStep> steps = new ArrayList<>();
        steps.add(new ThemeStep("Intro", MessageFormat.format(PAGES[1], courseId)));
        steps.add(new ThemeStep("Select theme", MessageFormat.format(PAGES[2], courseId)));
        steps.add(new ThemeStep("Select banner", MessageFormat.format(PAGES[3], courseId)));
        steps.add(new ThemeStep("Include navigation", MessageFormat.format(PAGES[4], courseId)));
        steps.add(new ThemeStep("Include guidance", MessageFormat.format(PAGES[5], courseId)));
        steps.add(new ThemeStep("Review", MessageFormat.format(PAGES[6], courseId)));
        steps.add(new ThemeStep("Submit", MessageFormat.format(PAGES[7], courseId)));
        return steps;
    }

    @GetMapping("/{courseId}/intro")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView intro(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /intro");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        ThemeModel themeModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_THEME_MODEL, ThemeModel.class);
        if (themeModel == null) {
            themeModel = new ThemeModel();
            courseSessionService.addAttributeToSession(httpSession, courseId, KEY_THEME_MODEL, themeModel);
        }

        model.addAttribute("themeForm", themeModel);

        return new ModelAndView("theme/intro");
    }

    @GetMapping("/{courseId}/navigation")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView navigation(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /navigation");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        return new ModelAndView("theme/navigation");
    }

    @GetMapping("/{courseId}/selectTheme")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView selectTheme(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /selectTheme");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        ThemeModel themeModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_THEME_MODEL, ThemeModel.class);
        List<Theme> themes =  themeRepository.findByActiveTrueOrderByName();

        model.addAttribute("themes", themes);
        model.addAttribute("themeForm", themeModel);

        return new ModelAndView("theme/selectTheme");
    }

    @GetMapping("/{courseId}/selectBanner")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView selectBanner(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /selectBanner");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        ThemeModel themeModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_THEME_MODEL, ThemeModel.class);
        List<BannerImageCategory> bannerImageCategories =  bannerImageCategoryRepository.findByActiveTrueOrderByName();

        if (themeModel.getIncludeBannerImage() == null) {
            themeModel.setIncludeBannerImage(true);
        }

        model.addAttribute("bannerImageCategories", bannerImageCategories);
        model.addAttribute("courseId", courseId);
        model.addAttribute("themeForm", themeModel);

        log.info("courseId = " + courseId);
        return new ModelAndView("theme/selectBanner");
    }

    @GetMapping("/{courseId}/guidance")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView guidance(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /guidance");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        return new ModelAndView("theme/guidance");
    }

    @GetMapping("/{courseId}/review")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView review(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /review");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        return new ModelAndView("theme/review");
    }

    @GetMapping("/{courseId}/submit")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView submit(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /submit");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        return new ModelAndView("theme/submit");
    }

    @PostMapping("/{courseId}/navigate")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView navigate(@PathVariable("courseId") String courseId, Model model, @ModelAttribute ThemeModel themeModel,
                                 @RequestParam(name = "action") String action, @RequestParam(name = "currentPage") int currentPage,
                                 HttpSession httpSession) {
        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        ThemeModel sessionThemeModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_THEME_MODEL, ThemeModel.class);

        int pageIndex = 0;

        switch (action) {
            case ACTION_HOME:
                //Reset stuff
                courseSessionService.removeAttributeFromSession(httpSession, courseId, KEY_THEME_MODEL);
                break;
            case ACTION_BACK:
                pageIndex = currentPage - 1;

                //Re-save the session model
                courseSessionService.addAttributeToSession(httpSession, courseId, KEY_THEME_MODEL,
                        updateThemeModelFields(sessionThemeModel, themeModel));

                break;
            case ACTION_NEXT:
                pageIndex = currentPage + 1;

                //Re-save the session model
                courseSessionService.addAttributeToSession(httpSession, courseId, KEY_THEME_MODEL,
                        updateThemeModelFields(sessionThemeModel, themeModel));

                //Template selection page
//                if (importModel.getSelectedTemplateId() != null) {
//                    sessionImportModel.setSelectedTemplateId(importModel.getSelectedTemplateId());
//                    sessionImportModel.setSelectedTemplateName(importModel.getSelectedTemplateName());
//                }

                //Re-save the session model
//                courseSessionService.addAttributeToSession(httpSession, courseId, KEY_IMPORT_MODEL, sessionImportModel);
                break;
            case ACTION_SUBMIT:
//                String templateHostingUrl = toolConfig.getTemplateHostingUrl();
//                // Use the current application as the template host if no other has been configured.
//                if (templateHostingUrl == null) {
//                    templateHostingUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
//                }
//                try {
//                    wizardService.doApplyTemplate(sessionImportModel, oidcTokenUtils.getUserLoginId(), templateHostingUrl);
//                    model.addAttribute("redirectUrl", getCanvasContentMigrationsToolUrl(courseId));
//                    // redirect to the Canvas tool
//                    return new ModelAndView(redirectToCanvas());
//                } catch (WizardServiceException e) {
//                    model.addAttribute("submitError", e.getMessage());
//                    return review(courseId, model, httpSession);
//                }
        }
        String url = MessageFormat.format(PAGES[pageIndex], courseId);
        return new ModelAndView("redirect:" + url);
    }

    @GetMapping("{courseId}/bannerimage/{categoryId}")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView banner(@PathVariable("courseId") String courseId, @PathVariable("categoryId") Long categoryId, Model model, HttpSession httpSession) {
        log.debug(String.format("in /bannerimage for courseId %s wanting category %d", courseId, categoryId));
        OidcAuthenticationToken token = getValidatedToken(courseId);

        BannerImageCategory bannerImageCategory =  bannerImageCategoryRepository.findById(categoryId)
                .orElse(new BannerImageCategory());
        model.addAttribute("bannerImages", bannerImageCategory.getBannerImages());

        return new ModelAndView("fragments :: themeBannerImageOptionsListItems");
    }

    private ThemeModel updateThemeModelFields(ThemeModel oldThemeModel, ThemeModel newThemeModel) {
        if (newThemeModel.getThemeId() != null) {
            oldThemeModel.setThemeId(newThemeModel.getThemeId());
        }

        if (newThemeModel.getIncludeBannerImage() != null) {
            oldThemeModel.setIncludeBannerImage(newThemeModel.getIncludeBannerImage());
        }

        if (newThemeModel.getBannerImageCategoryId() != null) {
            oldThemeModel.setBannerImageCategoryId(newThemeModel.getBannerImageCategoryId());
        }

        if (newThemeModel.getIncludeNavigation() != null) {
            oldThemeModel.setIncludeNavigation(newThemeModel.getIncludeNavigation());
        }

        if (newThemeModel.getIncludeGuidance() != null) {
            oldThemeModel.setIncludeGuidance(newThemeModel.getIncludeGuidance());
        }

        return oldThemeModel;
    }

}
