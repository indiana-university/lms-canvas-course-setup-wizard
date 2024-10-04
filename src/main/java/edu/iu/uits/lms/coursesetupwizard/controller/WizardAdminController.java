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

import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.model.Section;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.*;
import edu.iu.uits.lms.coursesetupwizard.repository.*;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.iuonly.model.FeatureAccess;
import edu.iu.uits.lms.iuonly.repository.FeatureAccessRepository;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.List;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.coursesetupwizard.Constants.*;

@Controller
@RequestMapping("/app/admin")
@Slf4j
public class WizardAdminController extends WizardController {

    @Autowired
    protected ToolConfig toolConfig = null;

    @Autowired
    protected WizardService wizardService = null;

    @Autowired
    protected ThemeRepository themeRepository;

    @Autowired
    protected ThemeContentRepository themeContentRepository;

    @Autowired
    protected BannerImageRepository bannerRepository;

    @Autowired
    protected BannerImageCategoryRepository bannerCategoryRepository;

    @Autowired
    protected PopupDismissalDateRepository popupRepository;

    @Autowired
    protected FeatureAccessServiceImpl featureAccessService;

    @Autowired
    protected CourseSessionService courseSessionService;

    @Autowired
    protected FeatureAccessRepository featureAccessRepository;

    @Autowired
    protected AccountService accountService;

    @RequestMapping({"","/","/{action}"})
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView adminAction(@PathVariable(required = false) String action, Model model, HttpSession httpSession) {
        log.debug("in /admin");
        OidcAuthenticationToken token = getTokenWithoutContext();

        Constants.AdminOption currentOption = Constants.AdminOption.findByName(action);
        if (currentOption == null) {
            currentOption = Constants.AdminOption.POPUP;
        }

        model.addAttribute("action", currentOption.name());

        switch (currentOption) {
            case POPUP:
                return popupList(model, httpSession);
            case EDITPOPUP:
                return new ModelAndView("/admin/editPopup");
            case FEATURE:
                return new ModelAndView("/admin/feature");
//            case MANAGEFEATURES:
//                return new ModelAndView("/admin/manageFeatures");
            case THEME:
                return themeList(model, httpSession);
            case EDITTHEME:
                return new ModelAndView("/admin/editTheme");
            case THEMECONTENT:
                return themeContentList(model, httpSession);
            case EDITTHEMECONTENT:
                return new ModelAndView("/admin/editThemeContent");
            case BANNER:
                return bannerList(model, httpSession);
            case EDITBANNER:
                return new ModelAndView("/admin/editBanner");
            case BANNERCATEGORY:
                return bannerCategoryList(model, httpSession);
            default:
                return popupList(model, httpSession);
        }

    }

    @GetMapping("/theme")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView themeList(Model model, HttpSession httpSession) {
        log.debug("in /theme");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<Theme> themeList = (List<Theme>)themeRepository.findAll(Sort.by("uiName"));

        model.addAttribute("themeList", themeList);

        return new ModelAndView("/admin/theme");
    }

    @GetMapping("/banner")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView bannerList(Model model, HttpSession httpSession) {
        log.debug("in /banner");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<BannerImage> bannerList = (List<BannerImage>)bannerRepository.findAll(Sort.by("uiName"));

        model.addAttribute("bannerList", bannerList);

        return new ModelAndView("/admin/banner");
    }

    @GetMapping("/bannerCategory")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView bannerCategoryList(Model model, HttpSession httpSession) {
        log.debug("in /bannerCategory");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<BannerImageCategory> bannerCategoryList = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));

        model.addAttribute("categoryList", bannerCategoryList);

        return new ModelAndView("/admin/bannerCategory");
    }

    @GetMapping("/popup")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView popupList(Model model, HttpSession httpSession) {
        log.debug("in /popup");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<PopupDismissalDate> popupList = (List<PopupDismissalDate>)popupRepository.findAll(Sort.by("showOn").descending());

        model.addAttribute("popupList", popupList);

        return new ModelAndView("/admin/popup");
    }

    @GetMapping("/themeContent")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView themeContentList(Model model, HttpSession httpSession) {
        log.debug("in /themeContent");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<ThemeContent> contentList = (List<ThemeContent>)themeContentRepository.findAll(Sort.by("name"));


        model.addAttribute("contentList", contentList);

        return new ModelAndView("/admin/themeContent");
    }

    @GetMapping("/feature")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView featureList(Model model, HttpSession httpSession) {
        log.debug("in /feature");
        OidcAuthenticationToken token = getTokenWithoutContext();

//        List<ThemeContent> contentList = (List<ThemeContent>)themeContentRepository.findAll(Sort.by("name"));

//        model.addAttribute("contentList", contentList);

        List<FeatureAccess> allFeatures = featureAccessRepository.findAll();
        List<FeatureAccess> featureList = allFeatures.stream()
                .filter(feature -> WIZARD_FEATURES.contains(feature.getFeatureId()))
                .collect(Collectors.toList());

        model.addAttribute("featureList", featureList);
        model.addAttribute("featureOptions", WIZARD_FEATURES);

        return new ModelAndView("/admin/feature");
    }

    @RequestMapping({"/manageFeatures","/manageFeatures/{accountId}"})
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView manageFeatures(@PathVariable(required = false) String accountId, Model model, HttpSession httpSession) {
        log.debug("in /manageFeatures");
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String currentUser = oidcTokenUtils.getUserLoginId();

        accountId = accountId == null ? accountId : accountId.trim();

        List<Account> userAccounts = accountService.getAccountsForUser("agschmid");
        List<String> accountList = userAccounts.stream().map(Account::getId).distinct().collect(Collectors.toList());

        String selectedAccount = accountList.contains(accountId) ? accountId : null;
        model.addAttribute("accountId", selectedAccount);
        model.addAttribute("accountList", accountList);

        List<FeatureAccess> allFeatures = featureAccessRepository.findAll();
        List<FeatureAccess> featureList = allFeatures.stream()
                .filter(feature -> WIZARD_FEATURES.contains(feature.getFeatureId()))
                .collect(Collectors.toList());

        model.addAttribute("featureList", featureList);

        return new ModelAndView("/admin/manageFeatures");
    }

    @GetMapping("/themeContent/{contentName}/edit")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView editThemeContent(@PathVariable("contentName") String contentName, Model model, HttpSession httpSession) {
        log.debug("in /admin/themeContent/" + contentName + "/edit");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("contentName", contentName);
        ThemeContent content = themeContentRepository.findById(contentName).orElse(null);

        //TODO throw error if content not found?
        model.addAttribute("contentForm", content);

        return new ModelAndView("/admin/editThemeContent");
    }

    @GetMapping("/themeContent/new")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView createThemeContent(Model model, HttpSession httpSession) {
        log.debug("in /admin/themeContent/new");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("create", true);

        ThemeContent content = new ThemeContent();
        model.addAttribute("contentForm", content);

        return new ModelAndView("/admin/editThemeContent");
    }

    @GetMapping("/popup/{popupId}/edit")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView editPopup(@PathVariable("popupId") String popupId, Model model, HttpSession httpSession) {
        log.debug("in /admin/popup/" + popupId);
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("popupId", popupId);

        PopupDismissalDate popup = popupRepository.findById(Long.parseLong(popupId)).orElse(null);
        //TODO throw error if not found?
        model.addAttribute("popupForm", popup);

        return new ModelAndView("/admin/editPopup");
    }

    @GetMapping("/popup/new")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView createPopupDate(Model model, HttpSession httpSession) {
        log.debug("in /admin/popup/new");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("create", true);
        model.addAttribute("popupForm", new PopupDismissalDate());

        return new ModelAndView("/admin/editPopup");
    }

    @GetMapping("/theme/{themeId}/edit")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView editTheme(@PathVariable("themeId") String themeId, Model model, HttpSession httpSession) {
        log.debug("in /admin/theme/" + themeId);
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("themeId", themeId);
        Theme theme = themeRepository.findById(Long.parseLong(themeId)).orElse(null);
        //TODO throw error if theme not found
        model.addAttribute("themeForm", theme);

        return new ModelAndView("/admin/editTheme");
    }

    @GetMapping("/theme/new")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView createTheme(Model model, HttpSession httpSession) {
        log.debug("in /admin/theme/new");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("create", true);
        Theme newTheme = new Theme();
        newTheme.setActive(false);

        model.addAttribute("themeForm", newTheme);
        return new ModelAndView("/admin/editTheme");
    }

    @GetMapping("/banner/{bannerId}/edit")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView editBanner(@PathVariable("bannerId") String bannerId, Model model, HttpSession httpSession) {
        log.debug("in /admin/banner/" + bannerId);
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("bannerId", bannerId);
        BannerImage banner = bannerRepository.findById(Long.parseLong(bannerId)).orElse(null);

        //TODO throw error if banner not found?
        model.addAttribute("bannerForm", banner);
        List<BannerImageCategory> categories = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categories", categories);

        return new ModelAndView("/admin/editBanner");
    }

    @GetMapping("/banner/new")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView createBanner(Model model, HttpSession httpSession) {
        log.debug("in /admin/theme/new");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("create", true);

        List<BannerImageCategory> categories = bannerCategoryRepository.findAll();
        model.addAttribute("categories", categories);

        BannerImage newBanner = new BannerImage();
        newBanner.setActive(false);

        model.addAttribute("bannerForm", newBanner);
        return new ModelAndView("/admin/editBanner");
    }

}
