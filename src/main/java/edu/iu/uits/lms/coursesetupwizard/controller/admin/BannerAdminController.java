package edu.iu.uits.lms.coursesetupwizard.controller.admin;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2025 Indiana University
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

import edu.iu.uits.lms.coursesetupwizard.model.BannerImage;
import edu.iu.uits.lms.coursesetupwizard.model.BannerImageCategory;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
import edu.iu.uits.lms.lti.LTIConstants;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_CANCEL;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;

@Controller
@RequestMapping("/app/admin/{courseId}/banner")
@Secured(LTIConstants.BASE_USER_AUTHORITY)
@Slf4j
public class BannerAdminController extends WizardAdminController {

    @Autowired
    protected BannerImageRepository bannerRepository;

    @Autowired
    protected BannerImageCategoryRepository bannerCategoryRepository;

    /**
     * Gets called before EVERY controller method
     * @param courseId Expected to be extracted as a path variable
     * @param model model
     */
    @ModelAttribute
    public void addCommonAttributesToModel(@PathVariable("courseId") String courseId, Model model) {
        model.addAttribute("courseId", courseId);
    }


    @GetMapping("/list")
    public ModelAndView bannerList(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/banner/list");
        wizardAdminSecurity(courseId);

        List<BannerImage> bannerList = (List<BannerImage>)bannerRepository.findAll(Sort.by("uiName"));

        model.addAttribute("bannerList", bannerList);


        return new ModelAndView("admin/banner");
    }

    @GetMapping("/{bannerId}/edit")
    public ModelAndView editBanner(@PathVariable("courseId") String courseId, @PathVariable("bannerId") Long bannerId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/banner/" + bannerId + "/edit");
        wizardAdminSecurity(courseId);

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

        return new ModelAndView("admin/editBanner");
    }

    @PostMapping(value = "/{bannerId}/update")
    public ModelAndView editBannerSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @PathVariable("bannerId") Long bannerId,
                                         @ModelAttribute BannerImage bannerModel, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/banner/" + bannerId + "/update");
        wizardAdminSecurity(courseId);

        switch (action) {
            case ACTION_CANCEL:
                return bannerList(courseId, model, session);
            case ACTION_SUBMIT:
                BannerImage banner = bannerRepository.findById(bannerId).orElse(null);
                if (banner == null) {
                    model.addAttribute("errorMsg", "No banner image found with id: " + bannerId);
                    return bannerList(courseId, model, session);
                }

                banner.mergeEditableFields(bannerModel);

                if (!banner.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Banner image is missing required fields.");
                    return editBanner(courseId, bannerId, model, session);
                }

                bannerRepository.save(banner);
                model.addAttribute("successMsg", banner.getUiName() + " banner image was updated.");
                return bannerList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to edit a banner image: " + action);
        return editBanner(courseId, bannerId, model, session);
    }

    @GetMapping("/new")
    public ModelAndView createBanner(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/new");
        wizardAdminSecurity(courseId);

        model.addAttribute("create", true);

        List<BannerImageCategory> categories = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categories", categories);

        BannerImage newBanner = new BannerImage();
        newBanner.setActive(false);

        model.addAttribute("selectedCategories", new ArrayList<>());
        model.addAttribute("bannerForm", newBanner);
        return new ModelAndView("admin/editBanner");
    }

    @PostMapping(value = "/new/submit")
    public ModelAndView createBannerSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @ModelAttribute BannerImage bannerModel, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/banner/new/submit");
        wizardAdminSecurity(courseId);

        switch (action) {
            case ACTION_CANCEL:
                return bannerList(courseId, model, session);
            case ACTION_SUBMIT:
                BannerImage newBanner = new BannerImage();
                newBanner.mergeEditableFields(bannerModel);

                if (!newBanner.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Banner image is missing required fields.");
                    return createBanner(courseId, model, session);
                }

                bannerRepository.save(newBanner);

                model.addAttribute("successMsg", newBanner.getUiName() + " banner image was created.");
                return bannerList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action " + action + " when attempting to save a new banner image.");
        return createBanner(courseId, model, session);
    }

    @PostMapping(value = "/toggle")
    public ModelAndView toggleBanner(@PathVariable("courseId") String courseId, @RequestParam Long bannerId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/banner/toggle");
        wizardAdminSecurity(courseId);

        BannerImage banner = bannerRepository.findById(bannerId).orElse(null);
        if (banner == null) {
            model.addAttribute("errorMsg", "No banner image found with id: " + bannerId);
            return bannerList(courseId, model, session);
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

        return bannerList(courseId, model, session);
    }

}
