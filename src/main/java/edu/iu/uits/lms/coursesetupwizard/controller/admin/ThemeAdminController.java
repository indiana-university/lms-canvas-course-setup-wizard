package edu.iu.uits.lms.coursesetupwizard.controller.admin;

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

import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.Theme;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeRepository;
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

import java.util.List;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_CANCEL;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;

@Controller
@RequestMapping("/app/admin/{courseId}/theme/")
@Secured(LTIConstants.BASE_USER_AUTHORITY)
@Slf4j
public class ThemeAdminController extends WizardAdminController {

    @Autowired
    protected ToolConfig toolConfig = null;

    @Autowired
    protected ThemeRepository themeRepository;

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
    public ModelAndView themeList(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /theme");
        wizardAdminSecurity(courseId);

        List<Theme> themeList = (List<Theme>)themeRepository.findAll(Sort.by("uiName"));

        model.addAttribute("themeList", themeList);

        return new ModelAndView("admin/theme");
    }

    @GetMapping("/{themeId}/edit")
    public ModelAndView editTheme(@PathVariable("courseId") String courseId, @PathVariable("themeId") Long themeId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/theme/" + themeId + "/edit");
        wizardAdminSecurity(courseId);

        model.addAttribute("themeId", themeId);

        Theme theme = themeRepository.findById(themeId).orElse(null);
        model.addAttribute("themeForm", theme);

        return new ModelAndView("admin/editTheme");
    }

    @PostMapping(value = "/{themeId}/update")
    public ModelAndView editThemeSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @PathVariable("themeId") Long themeId, @ModelAttribute Theme themeModel, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/theme/" + themeId + "/update");
        wizardAdminSecurity(courseId);

        switch (action) {
            case ACTION_CANCEL:
                return themeList(courseId, model, session);
            case ACTION_SUBMIT:
                Theme theme = themeRepository.findById(themeId).orElse(null);
                if (theme == null) {
                    model.addAttribute("errorMsg", "No theme found with id: " + themeId);
                    return themeList(courseId, model, session);
                }

                theme.mergeEditableFields(themeModel);

                if (!theme.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Theme is missing required fields.");
                    return editTheme(courseId, themeId, model, session);
                }

                themeRepository.save(theme);

                model.addAttribute("successMsg", theme.getUiName() + " theme was updated.");
                return themeList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action  when attempting to edit a theme: " + action);
        return editTheme(courseId, themeId, model, session);
    }

    @GetMapping("/new")
    public ModelAndView createTheme(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/theme/new");
        wizardAdminSecurity(courseId);

        model.addAttribute("create", true);
        Theme newTheme = new Theme();
        newTheme.setActive(false);

        model.addAttribute("themeForm", newTheme);

        return new ModelAndView("admin/editTheme");
    }

    @PostMapping(value = "/new/submit")
    public ModelAndView createThemeSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @ModelAttribute Theme themeModel, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/theme/new/submit");
        wizardAdminSecurity(courseId);

        switch (action) {
            case ACTION_CANCEL:
                return themeList(courseId, model, session);
            case ACTION_SUBMIT:
                Theme newTheme = new Theme();
                newTheme.mergeEditableFields(themeModel);

                if (!newTheme.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Theme is missing required fields.");
                    return createTheme(courseId, model, session);
                }

                themeRepository.save(newTheme);

                model.addAttribute("successMsg", newTheme.getUiName() + " theme was created.");
                return themeList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to create a new theme: " + action);
        return createTheme(courseId, model, session);
    }

    @PostMapping(value = "/toggle")
    public ModelAndView toggleTheme(@PathVariable("courseId") String courseId, @RequestParam Long themeId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/theme/toggle");
        wizardAdminSecurity(courseId);

        Theme theme = themeRepository.findById(themeId).orElse(null);
        if (theme == null) {
            model.addAttribute("errorMsg", "No theme found with id: " + themeId);
            return themeList(courseId, model, session);
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

        return themeList(courseId, model, session);
    }

}
