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

import edu.iu.uits.lms.coursesetupwizard.model.BannerImageCategory;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/app/admin/{courseId}/bannerCategory")
@Secured(LTIConstants.BASE_USER_AUTHORITY)
@Slf4j
public class BannerCategoryAdminController extends WizardAdminController {

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
    public ModelAndView bannerCategoryList(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/bannerCategory/list");
        wizardAdminSecurity(courseId);

        List<BannerImageCategory> bannerCategoryList = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        model.addAttribute("categoryList", bannerCategoryList);

        return new ModelAndView("admin/bannerCategory");
    }

    @PostMapping(value = "/save")
    public ModelAndView saveBannerCategory(@PathVariable("courseId") String courseId, @RequestParam (required = false) Long categoryId, @RequestParam String categoryName,
                                           Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/bannerCategory/save");
        wizardAdminSecurity(courseId);

        BannerImageCategory category = new BannerImageCategory();
        String successMsg;

        if (categoryId == null) {
            // create a new banner image category
            successMsg = categoryName + " category was created.";
        } else {
            category = bannerCategoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                model.addAttribute("errorMsg", "No banner category found with id: " + categoryId);
                return bannerCategoryList(courseId, model, session);
            }

            successMsg = categoryName + " category was updated.";
        }

        category.setName(categoryName);
        bannerCategoryRepository.save(category);

        model.addAttribute("successMsg", successMsg);
        return bannerCategoryList(courseId, model, session);
    }

    /**
     *
     * @param category
     * @return the id of the newly created category
     */
    @PostMapping(value = "/createInline")
    public ResponseEntity<String> createBannerCategory(@PathVariable("courseId") String courseId, @RequestBody BannerImageCategory category) {
        log.debug("in /admin/" + courseId + "/bannerCategory/createInline");

        wizardAdminSecurity(courseId);

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

    @GetMapping("/all")
    public ResponseEntity<List<BannerImageCategory>> getAllCategories(@PathVariable("courseId") String courseId) {
        log.debug("in /admin/" + courseId + "/bannerCategory/all");

        wizardAdminSecurity(courseId);

        List<BannerImageCategory> allCategories = (List<BannerImageCategory>)bannerCategoryRepository.findAll(Sort.by("name"));
        return ResponseEntity.ok(allCategories);
    }

    @PostMapping(value = "/toggle")
    public ModelAndView toggleBannerCategory(@PathVariable("courseId") String courseId, @RequestParam Long categoryId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/bannerCategory/toggle");
        wizardAdminSecurity(courseId);

        BannerImageCategory category = bannerCategoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            model.addAttribute("errorMsg", "No banner category found with id: " + categoryId);
            return bannerCategoryList(courseId, model, session);
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

        return bannerCategoryList(courseId, model, session);
    }

}
