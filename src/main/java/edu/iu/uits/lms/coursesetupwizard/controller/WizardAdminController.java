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

import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.*;
import edu.iu.uits.lms.coursesetupwizard.repository.*;
import edu.iu.uits.lms.coursesetupwizard.service.PopupDateUtil;
import edu.iu.uits.lms.coursesetupwizard.service.WizardAdminService;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.iuonly.model.FeatureAccess;
import edu.iu.uits.lms.iuonly.repository.FeatureAccessRepository;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.lti.LTIConstants;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.iu.uits.lms.coursesetupwizard.Constants.*;

@Controller
@RequestMapping("/app/admin")
@Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
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

    @Autowired
    protected WizardAdminService wizardAdminService;


    @RequestMapping({"","/","/{action}"})
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
    public ModelAndView themeList(Model model, HttpSession httpSession) {
        log.debug("in /theme");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<Theme> themeList = (List<Theme>)themeRepository.findAll(Sort.by("uiName"));

        model.addAttribute("themeList", themeList);

        return new ModelAndView("/admin/theme");
    }

    @GetMapping("/banner")
    public ModelAndView bannerList(Model model, HttpSession httpSession) {
        log.debug("in /banner");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<BannerImage> bannerList = (List<BannerImage>)bannerRepository.findAll(Sort.by("uiName"));

        model.addAttribute("bannerList", bannerList);


        return new ModelAndView("/admin/banner");
    }

    @RequestMapping(value = "/banner/edit/submit", method = RequestMethod.POST, params = {"action=cancel"})
    public ModelAndView bannerEditCancel(Model model, HttpSession session) {
        log.debug("cancelling banner edit");
        getTokenWithoutContext();
        return bannerList(model, session);
    }

    @RequestMapping(value = "/banner/edit/submit", method = RequestMethod.POST, params = {"action=edit"})
    public ModelAndView bannerEditSubmit(Model model, HttpSession session) {
        log.debug("saving banner changes");
        getTokenWithoutContext();


        return bannerList(model, session);
    }

    @GetMapping("/bannerCategory")
    public ModelAndView bannerCategoryList(Model model, HttpSession httpSession) {
        log.debug("in /bannerCategory");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<BannerImageCategory> bannerCategoryList = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categoryList", bannerCategoryList);

        return new ModelAndView("/admin/bannerCategory");
    }

    @GetMapping("/popup")
    public ModelAndView popupList(Model model, HttpSession httpSession) {
        log.debug("in /popup");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<PopupDismissalDate> popupList = (List<PopupDismissalDate>)popupRepository.findAll(Sort.by("showOn").descending());

        model.addAttribute("popupList", popupList);

        return new ModelAndView("/admin/popup");
    }

    @GetMapping("/popup/{popupId}/edit")
    public ModelAndView editPopup(@PathVariable("popupId") Long popupId, Model model, HttpSession httpSession) {
        log.debug("in /admin/popup/" + popupId + "/edit");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("popupId", popupId);

        PopupDismissalDate popup = popupRepository.findById(popupId).orElse(null);
        String formattedShownOn = PopupDateUtil.date2String(popup.getShowOn(), PopupDateUtil.INPUT_DATE_FORMAT_MMDDYYYY);

        model.addAttribute("popupForm", popup);

        return new ModelAndView("/admin/editPopup");
    }

    @GetMapping("/popup/new")
    public ModelAndView createPopupDate(Model model, HttpSession httpSession) {
        log.debug("in /admin/popup/new");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("create", true);

        if (model.getAttribute("popupForm") == null) {
            model.addAttribute("popupForm", new PopupDismissalDate());
        }

        return new ModelAndView("/admin/editPopup");
    }

    @PostMapping(value = "/popup/{popupId}/update")
    public ModelAndView popupEditSave(@RequestParam(name = "action") String action, @PathVariable("popupId") Long popupId, @ModelAttribute PopupDismissalDate popupModel, Model model, HttpSession session) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        PopupDismissalDate updatedPopupDismissalDate = popupRepository.findById(popupId).orElse(null);

        if (updatedPopupDismissalDate == null) {
            model.addAttribute("errorMsg", "Popup reset with id " + popupId + " does not exist.");
            return editPopup(popupId, model, session);
        }
        String db = updatedPopupDismissalDate.getShowOn().toString();
        String modeldate = popupModel.getShowOn().toString();

        switch (action) {
            case ACTION_CANCEL:
                return popupList(model, session);
            case ACTION_SUBMIT:
                // validate the date if it has been updated
                if (!updatedPopupDismissalDate.getShowOn().equals(popupModel.getShowOn())) {
                    try {
                        PopupDateUtil.validate(popupModel.getShowOn(), PopupDateUtil.INPUT_DATE_FORMAT_MMDDYYYY);
                        updatedPopupDismissalDate.setShowOn(popupModel.getShowOn());
                    } catch (IllegalArgumentException iae) {
                        model.addAttribute("errorMsg", iae.getMessage());
                        return editPopup(popupId, model, session);
                    }
                }

                updatedPopupDismissalDate.setNotes(popupModel.getNotes());

                //popupRepository.save(updatedPopupDismissalDate);

                model.addAttribute("successMsg", "The popup reset was updated successfully.");
                return popupList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action  when attempting to edit a popup: " + action);
        return createPopupDate(model, session);
    }

    @PostMapping(value = "/popup/new/submit")
    public ModelAndView createPopupSave(@RequestParam(name = "action") String action, @ModelAttribute PopupDismissalDate popupModel, Model model, HttpSession session) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return popupList(model, session);
            case ACTION_SUBMIT:
                // validate the date
                try {
                    PopupDateUtil.validate(popupModel.getShowOn(), PopupDateUtil.INPUT_DATE_FORMAT_MMDDYYYY);
                } catch (IllegalArgumentException iae) {
                    model.addAttribute("errorMsg", iae.getMessage());
                    model.addAttribute("popupForm", popupModel);
                    return createPopupDate(model, session);
                }

                //popupRepository.save(popupModel);

                model.addAttribute("successMsg", "The popup reset " + popupModel.getShowOn() + " was created successfully.");
                return popupList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to create a new popup: " + action);
        return createPopupDate(model, session);
    }

    @GetMapping("/feature")
    public ModelAndView featureList(Model model, HttpSession httpSession) {
        log.debug("in /feature");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<String> featureIds = Stream.of(WizardFeature.values()).map(WizardFeature::getId).collect(Collectors.toList());

        List<FeatureAccess> allFeatures = featureAccessRepository.findAll();
        List<FeatureAccess> wizardFeatures = allFeatures.stream()
                .filter(feature -> featureIds.contains(feature.getFeatureId()))
                .collect(Collectors.toList());

        List<WizardFeatureModel> featureList = new ArrayList<>();
        for (FeatureAccess feature : wizardFeatures) {
            WizardFeatureModel newFeature = new WizardFeatureModel();
            newFeature.setId(feature.getFeatureId());
            newFeature.setAccountId(feature.getAccountId());
            newFeature.setDisplayName(WizardFeature.findDisplayNameById(feature.getFeatureId()));

            featureList.add(newFeature);
        }

        model.addAttribute("featureList", featureList);

        List<String> featureOptions = Stream.of(WizardFeature.values()).sorted().map(WizardFeature::getDisplayName).collect(Collectors.toList());
        model.addAttribute("featureOptions", featureOptions);

        return new ModelAndView("/admin/feature");
    }

    @GetMapping("/themeContent")
    public ModelAndView themeContentList(Model model, HttpSession httpSession) {
        log.debug("in /themeContent");
        OidcAuthenticationToken token = getTokenWithoutContext();

        List<ThemeContent> contentList = (List<ThemeContent>)themeContentRepository.findAll(Sort.by("name"));
        model.addAttribute("contentList", contentList);

        return new ModelAndView("/admin/themeContent");
    }

    @GetMapping("/themeContent/{contentName}/edit")
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
    public ModelAndView createThemeContent(Model model, HttpSession httpSession) {
        log.debug("in /admin/themeContent/new");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("create", true);

        ThemeContent content = new ThemeContent();
        model.addAttribute("contentForm", content);

        return new ModelAndView("/admin/editThemeContent");
    }



    @PostMapping(value = "/themeContent/edit/submit", params = {"action=cancel"})
    public ModelAndView themeContentEditCancel(Model model, HttpSession session) {
        return themeContentList(model, session);
    }


    @PostMapping(value = "/theme/edit/submit", params = {"action=cancel"})
    public ModelAndView themeEditCancel(Model model, HttpSession session) {
        return themeList(model, session);
    }

    @GetMapping("/theme/{themeId}/edit")
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
    public ModelAndView editBanner(@PathVariable("bannerId") String bannerId, Model model, HttpSession httpSession) {
        log.debug("in /admin/banner/" + bannerId);
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("bannerId", bannerId);
        BannerImage banner = bannerRepository.findById(Long.parseLong(bannerId)).orElse(null);

        //TODO throw error if banner not found?
        model.addAttribute("bannerForm", banner);
        List<BannerImageCategory> categories = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categories", categories);

        List<Long> selectedCategories = banner.getBannerImageCategories().stream()
                .map(BannerImageCategory::getId)
                .collect(Collectors.toList());
        model.addAttribute("selectedCategories", selectedCategories);

        return new ModelAndView("/admin/editBanner");
    }

    @GetMapping("/banner/new")
    public ModelAndView createBanner(Model model, HttpSession httpSession) {
        log.debug("in /admin/banner/new");
        OidcAuthenticationToken token = getTokenWithoutContext();

        model.addAttribute("create", true);

        List<BannerImageCategory> categories = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categories", categories);

        BannerImage newBanner = new BannerImage();
        newBanner.setActive(false);

        model.addAttribute("selectedCategories", new ArrayList<>());
        model.addAttribute("bannerForm", newBanner);
        return new ModelAndView("/admin/editBanner");
    }

    @PostMapping(value = "/banner/new/submit")
    public ModelAndView createBannerSubmit(@RequestParam(name = "action") String action, @ModelAttribute BannerImage bannerModel, Model model, HttpSession session) {
        OidcAuthenticationToken token = getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return bannerList(model, session);
            case ACTION_SUBMIT:
                //wizardAdminService.saveBannerImageAndCategories(bannerModel, null);

                model.addAttribute("successMsg", "Banner image " + bannerModel.getUiName() + " was created.");
                return bannerList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action " + action + " when attempting to save a new banner image.");
        return createBanner(model, session);
    }


}
