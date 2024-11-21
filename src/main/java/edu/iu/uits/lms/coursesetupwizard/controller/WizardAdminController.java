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
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.iuonly.model.FeatureAccess;
import edu.iu.uits.lms.iuonly.repository.FeatureAccessRepository;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.lti.LTIConstants;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.iu.uits.lms.coursesetupwizard.Constants.*;

@Controller
@RequestMapping("/app/admin")
@Secured(LTIConstants.BASE_USER_AUTHORITY)
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
    public ModelAndView adminAction(@PathVariable(required = false) String action, Model model, HttpSession httpSession) {
        log.debug("in /admin");
        getTokenWithoutContext();

        Constants.AdminOption currentOption = Constants.AdminOption.findByName(action);
        if (currentOption == null) {
            currentOption = Constants.AdminOption.POPUP;
        }

        model.addAttribute("action", currentOption.name());

        switch (currentOption) {
            case POPUP:
                return popupList(model, httpSession);
            case FEATURE:
                return featureList(model, httpSession);
            case THEME:
                return themeList(model, httpSession);
            case THEMECONTENT:
                return themeContentList(model, httpSession);
            case BANNER:
                return bannerList(model, httpSession);
            case BANNERCATEGORY:
                return bannerCategoryList(model, httpSession);
            default:
                return popupList(model, httpSession);
        }

    }

    @GetMapping("/theme")
    public ModelAndView themeList(Model model, HttpSession httpSession) {
        log.debug("in /theme");
        getTokenWithoutContext();

        List<Theme> themeList = (List<Theme>)themeRepository.findAll(Sort.by("uiName"));

        model.addAttribute("themeList", themeList);

        return new ModelAndView("/admin/theme");
    }


    @GetMapping("/theme/{themeId}/edit")
    public ModelAndView editTheme(@PathVariable("themeId") Long themeId, Model model, HttpSession httpSession) {
        log.debug("in /admin/theme/" + themeId + "/edit");
        getTokenWithoutContext();

        model.addAttribute("themeId", themeId);

        Theme theme = themeRepository.findById(themeId).orElse(null);
        model.addAttribute("themeForm", theme);

        return new ModelAndView("/admin/editTheme");
    }

    @PostMapping(value = "/theme/{themeId}/update")
    public ModelAndView editThemeSubmit(@RequestParam(name = "action") String action, @PathVariable("themeId") Long themeId, @ModelAttribute Theme themeModel, Model model, HttpSession session) {
        log.debug("in /theme/" + themeId + "/update");
        getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return themeList(model, session);
            case ACTION_SUBMIT:
                Theme theme = themeRepository.findById(themeId).orElse(null);
                if (theme == null) {
                    model.addAttribute("errorMsg", "No theme found with id: " + themeId);
                    return themeList(model, session);
                }

                theme.mergeEditableFields(themeModel);

                if (!theme.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Theme is missing required fields.");
                    return editTheme(themeId, model, session);
                }

                model.addAttribute("successMsg", theme.getUiName() + " theme was updated.");
                return themeList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action  when attempting to edit a theme: " + action);
        return editTheme(themeId, model, session);
    }

    @GetMapping("/theme/new")
    public ModelAndView createTheme(Model model, HttpSession httpSession) {
        log.debug("in /admin/theme/new");
        getTokenWithoutContext();

        model.addAttribute("create", true);
        Theme newTheme = new Theme();
        newTheme.setActive(false);

        model.addAttribute("themeForm", newTheme);

        return new ModelAndView("/admin/editTheme");
    }

    @PostMapping(value = "/theme/new/submit")
    public ModelAndView createThemeSubmit(@RequestParam(name = "action") String action, @ModelAttribute Theme themeModel, Model model, HttpSession session) {
        log.debug("in /theme/new/submit");
        getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return themeList(model, session);
            case ACTION_SUBMIT:
                Theme newTheme = new Theme();
                newTheme.mergeEditableFields(themeModel);

                if (!newTheme.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Theme is missing required fields.");
                    return createTheme(model, session);
                }

                themeRepository.save(newTheme);

                model.addAttribute("successMsg", newTheme.getUiName() + " theme was created.");
                return themeList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to create a new theme: " + action);
        return createTheme(model, session);
    }

    @PostMapping(value = "/theme/toggle")
    public ModelAndView toggleTheme(@RequestParam Long themeId, Model model, HttpSession session) {
        log.debug(" in /theme/toggle");
        getTokenWithoutContext();

        Theme theme = themeRepository.findById(themeId).orElse(null);
        if (theme == null) {
            model.addAttribute("errorMsg", "No theme found with id: " + themeId);
            return themeList(model, session);
        }

        // activate/inactive this theme
        if (theme.isActive()) {
            theme.setActive(false);
        } else {
            theme.setActive(true);
        }

        themeRepository.save(theme);

        String successMsg = theme.getUiName() + " theme is now " + (theme.isActive() ? "active." : "inactive.");
        model.addAttribute("successMsg", successMsg);

        return themeList(model, session);
    }

    @GetMapping("/banner")
    public ModelAndView bannerList(Model model, HttpSession httpSession) {
        log.debug("in /banner");
        getTokenWithoutContext();

        List<BannerImage> bannerList = (List<BannerImage>)bannerRepository.findAll(Sort.by("uiName"));

        model.addAttribute("bannerList", bannerList);


        return new ModelAndView("/admin/banner");
    }

    @GetMapping("/banner/{bannerId}/edit")
    public ModelAndView editBanner(@PathVariable("bannerId") Long bannerId, Model model, HttpSession httpSession) {
        log.debug("in /admin/banner/" + bannerId + "/edit");
        getTokenWithoutContext();

        // needed for the fetch to create a category while editing
        model.addAttribute("customId", httpSession.getId());

        model.addAttribute("bannerId", bannerId);
        BannerImage banner = bannerRepository.findById(bannerId).orElse(null);

        model.addAttribute("bannerForm", banner);
        List<BannerImageCategory> categories = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categories", categories);

        List<Long> selectedCategories = banner.getBannerImageCategories().stream()
                .map(BannerImageCategory::getId)
                .collect(Collectors.toList());
        model.addAttribute("selectedCategories", selectedCategories);

        return new ModelAndView("/admin/editBanner");
    }

    @PostMapping(value = "/banner/{bannerId}/update")
    public ModelAndView editBannerSubmit(@RequestParam(name = "action") String action, @PathVariable("bannerId") Long bannerId,
                                         @ModelAttribute BannerImage bannerModel, Model model, HttpSession session) {
        log.debug("in /banner/" + bannerId + "/update");
        getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return bannerList(model, session);
            case ACTION_SUBMIT:
                BannerImage banner = bannerRepository.findById(bannerId).orElse(null);
                if (banner == null) {
                    model.addAttribute("errorMsg", "No banner image found with id: " + bannerId);
                    return bannerList(model, session);
                }

                banner.mergeEditableFields(bannerModel);

                if (!banner.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Banner image is missing required fields.");
                    return editBanner(bannerId, model, session);
                }

                bannerRepository.save(banner);
                model.addAttribute("successMsg", banner.getUiName() + " banner image was updated.");
                return bannerList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to edit a banner image: " + action);
        return editBanner(bannerId, model, session);
    }

    @GetMapping("/banner/new")
    public ModelAndView createBanner(Model model, HttpSession httpSession) {
        log.debug("in /admin/banner/new");
        getTokenWithoutContext();

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
        log.debug("in /banner/new/submit");
        getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return bannerList(model, session);
            case ACTION_SUBMIT:
                BannerImage newBanner = new BannerImage();
                newBanner.mergeEditableFields(bannerModel);

                if (!newBanner.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Banner image is missing required fields.");
                    return createBanner(model, session);
                }

                bannerRepository.save(newBanner);

                model.addAttribute("successMsg", newBanner.getUiName() + " banner image was created.");
                return bannerList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action " + action + " when attempting to save a new banner image.");
        return createBanner(model, session);
    }

    @PostMapping(value = "/banner/toggle")
    public ModelAndView toggleBanner(@RequestParam Long bannerId, Model model, HttpSession session) {
        log.debug("in /banner/toggle");
        getTokenWithoutContext();

        BannerImage banner = bannerRepository.findById(bannerId).orElse(null);
        if (banner == null) {
            model.addAttribute("errorMsg", "No banner image found with id: " + bannerId);
            return bannerList(model, session);
        }

        // activate/inactive this banner image
        if (banner.isActive()) {
            banner.setActive(false);
        } else {
            banner.setActive(true);
        }

        bannerRepository.save(banner);

        String successMsg = banner.getUiName() + " banner image is now " + (banner.isActive() ? "active." : "inactive.");
        model.addAttribute("successMsg", successMsg);

        return bannerList(model, session);
    }

    @GetMapping("/bannerCategory")
    public ModelAndView bannerCategoryList(Model model, HttpSession httpSession) {
        log.debug("in /bannerCategory");
        getTokenWithoutContext();

        List<BannerImageCategory> bannerCategoryList = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categoryList", bannerCategoryList);

        return new ModelAndView("/admin/bannerCategory");
    }

    @PostMapping(value = "/bannerCategory/save")
    public ModelAndView saveBannerCategory(@RequestParam (required = false) Long categoryId, @RequestParam String categoryName,
                                         Model model, HttpSession session) {
        log.debug("in /bannerCategory/save");
        getTokenWithoutContext();

        BannerImageCategory category = new BannerImageCategory();
        String successMsg;

        if (categoryId == null) {
            // create a new banner image category
            successMsg = categoryName + " category was created.";
        } else {
            category = bannerCategoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                model.addAttribute("errorMsg", "No banner category found with id: " + categoryId);
                return bannerCategoryList(model, session);
            }

            successMsg = categoryName + " category was updated.";
        }

        category.setName(categoryName);
        bannerCategoryRepository.save(category);

        model.addAttribute("successMsg", successMsg);
        return bannerCategoryList(model, session);
    }

    /**
     *
     * @param category
     * @return the id of the newly created category
     */
    @PostMapping(value = "/bannerCategory/createInline")
    public ResponseEntity<String> createBannerCategory(@RequestBody BannerImageCategory category) {
        BannerImageCategory newCategory = new BannerImageCategory();
        newCategory.setActive(category.isActive());
        newCategory.setName(category.getName());

        if (!newCategory.isValid()) {
            return ResponseEntity.badRequest().body("Category name is required.");
        }

        BannerImageCategory savedCategory;
        try {
            savedCategory = bannerCategoryRepository.save(newCategory);
        } catch (Exception e) {
            log.error("Unable to save new banner image category: " + category.getName(), e);
            return ResponseEntity.badRequest().body("Error saving banner image category");
        }

        return ResponseEntity.ok(savedCategory.getId().toString());
    }

    @GetMapping("/bannerCategory/all")
    public ResponseEntity<List<BannerImageCategory>> getAllCategories() {
        List<BannerImageCategory> allCategories = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        return ResponseEntity.ok(allCategories);
    }

    @PostMapping(value = "/bannerCategory/toggle")
    public ModelAndView toggleBannerCategory(@RequestParam Long categoryId, Model model, HttpSession session) {
        log.debug("in /bannerCategory/toggle");
        getTokenWithoutContext();

        BannerImageCategory category = bannerCategoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            model.addAttribute("errorMsg", "No banner category found with id: " + categoryId);
            return bannerCategoryList(model, session);
        }

        // activate/inactive this banner image category
        if (category.isActive()) {
            category.setActive(false);
        } else {
            category.setActive(true);
        }

        bannerCategoryRepository.save(category);

        String successMsg = category.getName() + " category is now " + (category.isActive() ? "active." : "inactive.");
        model.addAttribute("successMsg", successMsg);

        return bannerCategoryList(model, session);
    }

    @GetMapping("/popup")
    public ModelAndView popupList(Model model, HttpSession session) {
        log.debug("in /popup");
        getTokenWithoutContext();

        List<PopupDismissalDate> popupList = (List<PopupDismissalDate>)popupRepository.findAll(Sort.by("showOn").descending());

        model.addAttribute("popupList", popupList);

        return new ModelAndView("/admin/popup");
    }

    @GetMapping("/popup/{popupId}/edit")
    public ModelAndView editPopup(@PathVariable("popupId") Long popupId, Model model, HttpSession session) {
        log.debug("in /admin/popup/" + popupId + "/edit");
        getTokenWithoutContext();

        model.addAttribute("popupId", popupId);

        PopupDismissalDate popup = popupRepository.findById(popupId).orElse(null);

        // If the popup is in the past, show a read-only view
        if (popup.getShowOn().before(new Date())) {
            model.addAttribute("readOnly", true);
        }

        model.addAttribute("popupForm", popup);

        return new ModelAndView("/admin/editPopup");
    }

    @GetMapping("/popup/new")
    public ModelAndView createPopupDate(Model model, HttpSession session) {
        log.debug("in /admin/popup/new");
        getTokenWithoutContext();

        model.addAttribute("create", true);

        if (model.getAttribute("popupForm") == null) {
            model.addAttribute("popupForm", new PopupDismissalDate());
        }

        return new ModelAndView("/admin/editPopup");
    }

    @PostMapping(value = "/popup/{popupId}/update")
    public ModelAndView editPopupSubmit(@RequestParam(name = "action") String action, @PathVariable("popupId") Long popupId, @ModelAttribute PopupDismissalDate popupModel, Model model, HttpSession session) {
        log.debug("in /popup/" + popupId + "/update");
        getTokenWithoutContext();

        PopupDismissalDate updatedPopupDismissalDate = popupRepository.findById(popupId).orElse(null);

        if (updatedPopupDismissalDate == null) {
            model.addAttribute("errorMsg", "Popup reset with id " + popupId + " does not exist.");
            return editPopup(popupId, model, session);
        }

        switch (action) {
            case ACTION_CANCEL:
                return popupList(model, session);
            case ACTION_SUBMIT:
                try {
                    PopupDateUtil.validate(popupModel.getShowOn(), PopupDateUtil.INPUT_DATE_FORMAT_MMDDYYYY);
                } catch (IllegalArgumentException iae) {
                    model.addAttribute("errorMsg", iae.getMessage());
                    return editPopup(popupId, model, session);
                }

                updatedPopupDismissalDate.setShowOn(popupModel.getShowOn());
                updatedPopupDismissalDate.setNotes(popupModel.getNotes());

                if (!updatedPopupDismissalDate.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Updated popup reset is missing required fields.");
                    return editPopup(popupId, model, session);
                }

                popupRepository.save(updatedPopupDismissalDate);

                model.addAttribute("successMsg", "The popup reset was updated successfully.");
                return popupList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to edit a popup: " + action);
        return createPopupDate(model, session);
    }

    @PostMapping(value = "/popup/new/submit")
    public ModelAndView createPopupSubmit(@RequestParam(name = "action") String action, @ModelAttribute PopupDismissalDate popupModel, Model model, HttpSession session) {
        log.debug("in /popup/new/submit");
        getTokenWithoutContext();

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

                PopupDismissalDate newPopup = new PopupDismissalDate();
                newPopup.setShowOn(popupModel.getShowOn());
                newPopup.setNotes(popupModel.getNotes());

                if (!newPopup.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "New popup reset is missing required fields.");
                    return createPopupDate(model, session);
                }

                popupRepository.save(newPopup);

                model.addAttribute("successMsg", "The popup reset " + popupModel.getShowOn() + " was created successfully.");
                return popupList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to create a new popup: " + action);
        return createPopupDate(model, session);
    }

    @GetMapping("/feature")
    public ModelAndView featureList(Model model, HttpSession httpSession) {
        log.debug("in /feature");
        getTokenWithoutContext();

        List<String> featureIds = Stream.of(WizardFeature.values()).map(WizardFeature::getFeatureId).collect(Collectors.toList());

        List<FeatureAccess> allFeatures = featureAccessRepository.findAll();
        List<FeatureAccess> wizardFeatures = allFeatures.stream()
                .filter(feature -> featureIds.contains(feature.getFeatureId()))
                .collect(Collectors.toList());

        List<WizardFeatureModel> featureList = new ArrayList<>();
        for (FeatureAccess feature : wizardFeatures) {
            WizardFeatureModel featureModel = new WizardFeatureModel(feature.getId(), feature.getFeatureId(), feature.getAccountId(), WizardFeature.findDisplayNameById(feature.getFeatureId()));
            featureList.add(featureModel);
        }

        model.addAttribute("featureList", featureList);

        List<WizardFeature> featureOptions = Arrays.asList(WizardFeature.values());
        model.addAttribute("featureOptions", featureOptions);

        return new ModelAndView("/admin/feature");
    }

    @PostMapping(value = "/feature/enable")
    public ModelAndView enableFeature(@RequestParam String featureId, @RequestParam String accountId, Model model, HttpSession session) {
        log.debug("in /feature/enable");
        getTokenWithoutContext();

        // verify that feature isn't already enabled
        if(featureAccessService.isFeatureEnabledForAccount(featureId, accountId, null)) {
            model.addAttribute("errorMsg", WizardFeature.findDisplayNameById(featureId) + " is already enabled for account " + accountId);
            return featureList(model, session);
        };

        FeatureAccess newAccess = new FeatureAccess();
        newAccess.setFeatureId(featureId);
        newAccess.setAccountId(accountId);

        featureAccessRepository.save(newAccess);

        model.addAttribute("successMsg", WizardFeature.findDisplayNameById(featureId) + " feature enabled for account " + accountId + ".");
        return featureList(model, session);
    }

    @PostMapping(value = "/feature/delete")
    public ModelAndView deleteFeature(@RequestParam Long deleteId, Model model, HttpSession session) {
        log.debug("in /feature/delete");
        getTokenWithoutContext();

        FeatureAccess feature = featureAccessRepository.findById(deleteId).orElse(null);

        if (!featureAccessRepository.existsById(deleteId)) {
            model.addAttribute("errorMsg", "Feature with id does not exist: " + deleteId);
            return featureList(model, session);
        }
        // delete
        featureAccessRepository.deleteById(deleteId);

        model.addAttribute("successMsg", WizardFeature.findDisplayNameById(feature.getFeatureId()) + " feature deleted for account " + feature.getAccountId() + ".");
        return featureList(model, session);
    }

    @GetMapping("/themeContent")
    public ModelAndView themeContentList(Model model, HttpSession httpSession) {
        log.debug("in /themeContent");
        getTokenWithoutContext();

        List<ThemeContent> contentList = (List<ThemeContent>)themeContentRepository.findAll(Sort.by("name"));
        model.addAttribute("contentList", contentList);

        return new ModelAndView("/admin/themeContent");
    }

    @GetMapping("/themeContent/{contentName}/edit")
    public ModelAndView editThemeContent(@PathVariable("contentName") String contentName, Model model, HttpSession httpSession) {
        log.debug("in /admin/themeContent/" + contentName + "/edit");
        getTokenWithoutContext();

        model.addAttribute("contentName", contentName);
        ThemeContent content = themeContentRepository.findById(contentName).orElse(null);

        model.addAttribute("contentForm", content);

        return new ModelAndView("/admin/editThemeContent");
    }

    @GetMapping("/themeContent/new")
    public ModelAndView createThemeContent(Model model, HttpSession httpSession) {
        log.debug("in /admin/themeContent/new");
        getTokenWithoutContext();

        model.addAttribute("create", true);

        ThemeContent content = new ThemeContent();
        model.addAttribute("contentForm", content);

        return new ModelAndView("/admin/editThemeContent");
    }

    @PostMapping(value = "/themeContent/new/submit")
    public ModelAndView createThemeContentSubmit(@RequestParam(name = "action") String action, @ModelAttribute ThemeContent contentModel, Model model, HttpSession session) {
        log.debug(" in /themeContent/new/submit");
        getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return themeContentList(model, session);
            case ACTION_SUBMIT:
                ThemeContent newContent = new ThemeContent();
                newContent.setName(contentModel.getName());
                newContent.setTemplateText(contentModel.getTemplateText());

                if (!newContent.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "New theme content is missing required fields.");
                    return createThemeContent(model, session);
                }

                themeContentRepository.save(newContent);

                model.addAttribute("successMsg", newContent.getName() + " theme content was created.");
                return themeContentList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to create new theme content: " + action);
        return createThemeContent(model, session);
    }

    @PostMapping(value = "/themeContent/upload")
    public ModelAndView uploadThemeContent(@RequestParam String contentName, @RequestParam("templateTextFile")
        MultipartFile templateTextFile, Model model, HttpSession session) {
        log.debug(" in /themeContent/upload");
        getTokenWithoutContext();

        ThemeContent content = themeContentRepository.findById(contentName).orElse(null);
        if (content == null) {
            model.addAttribute("errorMsg", "No theme content found with name: " + contentName);
            return themeContentList(model, session);
        }

        try {
            content.setTemplateText(new String(templateTextFile.getBytes()));
        } catch (IOException e) {
            model.addAttribute("errorMsg", "An error occurred reading uploaded file: " + e.getMessage());
            return themeContentList(model, session);
        }

        if (!content.isValid()) {
            // We shouldn't get here because of the client side validation, but just in case...
            model.addAttribute("errorMsg", "Updated theme content is missing required fields.");
            return themeContentList(model, session);
        }

        themeContentRepository.save(content);

        model.addAttribute("successMsg", content.getName() + " theme content was updated.");
        return themeContentList(model, session);
    }

    @PostMapping(value = "/themeContent/{contentName}/update")
    public ModelAndView editThemeContentSubmit(@RequestParam(name = "action") String action, @PathVariable("contentName") String contentName, @ModelAttribute ThemeContent themeContent, Model model, HttpSession session) {
        log.debug("in /themeContent/" + contentName + "/update");
        getTokenWithoutContext();

        switch (action) {
            case ACTION_CANCEL:
                return themeContentList(model, session);
            case ACTION_SUBMIT:
                ThemeContent content = themeContentRepository.findById(contentName).orElse(null);
                if (content == null) {
                    model.addAttribute("errorMsg", "No theme content found with name: " + contentName);
                    return editThemeContent(contentName, model, session);
                }

                content.setTemplateText(themeContent.getTemplateText());
                themeContentRepository.save(themeContent);

                model.addAttribute("successMsg", themeContent.getName() + " theme content was updated.");
                return themeContentList(model, session);
        }

        model.addAttribute("errorMsg", "Unknown action  when attempting to edit theme content: " + action);
        return editThemeContent(contentName, model, session);
    }

    @PostMapping(value = "/themeContent/delete")
    public ModelAndView deleteThemeContent(@RequestParam String contentName, Model model, HttpSession session) {
        log.debug("in /themeContent/delete");
        getTokenWithoutContext();

        if (!themeContentRepository.existsById(contentName)) {
            model.addAttribute("errorMsg", "Theme content does not exist: " + contentName);
            return themeContentList(model, session);
        }

        // delete
        themeContentRepository.deleteById(contentName);

        model.addAttribute("successMsg", contentName + " theme content was deleted.");
        return themeContentList(model, session);
    }

}
