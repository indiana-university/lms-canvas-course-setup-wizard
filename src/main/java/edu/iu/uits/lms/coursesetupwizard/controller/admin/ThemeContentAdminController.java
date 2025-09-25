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

import edu.iu.uits.lms.coursesetupwizard.model.ThemeContent;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeContentRepository;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_CANCEL;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;

@Controller
@RequestMapping("/app/admin/{courseId}/themeContent")
@Secured(LTIConstants.BASE_USER_AUTHORITY)
@Slf4j
public class ThemeContentAdminController extends WizardAdminController {

    @Autowired
    protected ThemeContentRepository themeContentRepository;

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
    public ModelAndView themeContentList(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/themeContent/list");
        wizardAdminSecurity(courseId);

        List<ThemeContent> contentList = (List<ThemeContent>)themeContentRepository.findAll(Sort.by("name"));
        model.addAttribute("contentList", contentList);

        return new ModelAndView("admin/themeContent");
    }

    @GetMapping("/{contentName}/edit")
    public ModelAndView editThemeContent(@PathVariable("courseId") String courseId, @PathVariable("contentName") String contentName, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/themeContent/" + contentName + "/edit");
        wizardAdminSecurity(courseId);

        model.addAttribute("contentName", contentName);
        ThemeContent content = themeContentRepository.findById(contentName).orElse(null);

        model.addAttribute("contentForm", content);

        return new ModelAndView("admin/editThemeContent");
    }

    @GetMapping("/new")
    public ModelAndView createThemeContent(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/themeContent/new");
        wizardAdminSecurity(courseId);

        model.addAttribute("create", true);

        ThemeContent content = new ThemeContent();
        model.addAttribute("contentForm", content);

        return new ModelAndView("admin/editThemeContent");
    }

    @PostMapping(value = "/new/submit")
    public ModelAndView createThemeContentSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @ModelAttribute ThemeContent contentModel, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/themeContent/new/submit");
        wizardAdminSecurity(courseId);

        switch (action) {
            case ACTION_CANCEL:
                return themeContentList(courseId, model, session);
            case ACTION_SUBMIT:
                ThemeContent newContent = new ThemeContent();
                newContent.setName(contentModel.getName());
                newContent.setTemplateText(contentModel.getTemplateText());

                if (!newContent.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "New theme content is missing required fields.");
                    return createThemeContent(courseId, model, session);
                }

                themeContentRepository.save(newContent);

                model.addAttribute("successMsg", newContent.getName() + " theme content was created.");
                return themeContentList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to create new theme content: " + action);
        return createThemeContent(courseId, model, session);
    }

    @PostMapping(value = "/upload")
    public ModelAndView uploadThemeContent(@PathVariable("courseId") String courseId, @RequestPart String contentName, @RequestParam("templateTextFile")
    MultipartFile templateTextFile, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/themeContent/upload");
        wizardAdminSecurity(courseId);

        ThemeContent content = themeContentRepository.findById(contentName).orElse(null);
        if (content == null) {
            model.addAttribute("errorMsg", "No theme content found with name: " + contentName);
            return themeContentList(courseId, model, session);
        }

        try {
            content.setTemplateText(new String(templateTextFile.getBytes()));
        } catch (IOException e) {
            model.addAttribute("errorMsg", "An error occurred reading uploaded file: " + e.getMessage());
            return themeContentList(courseId, model, session);
        }

        if (!content.isValid()) {
            // We shouldn't get here because of the client side validation, but just in case...
            model.addAttribute("errorMsg", "Updated theme content is missing required fields.");
            return themeContentList(courseId, model, session);
        }

        themeContentRepository.save(content);

        model.addAttribute("successMsg", content.getName() + " theme content was updated.");
        return themeContentList(courseId, model, session);
    }

    @PostMapping(value = "/{contentName}/update")
    public ModelAndView editThemeContentSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @PathVariable("contentName") String contentName, @ModelAttribute ThemeContent themeContent, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/themeContent/" + contentName + "/update");
        wizardAdminSecurity(courseId);

        switch (action) {
            case ACTION_CANCEL:
                return themeContentList(courseId, model, session);
            case ACTION_SUBMIT:
                ThemeContent content = themeContentRepository.findById(contentName).orElse(null);
                if (content == null) {
                    model.addAttribute("errorMsg", "No theme content found with name: " + contentName);
                    return editThemeContent(courseId, contentName, model, session);
                }

                content.setTemplateText(themeContent.getTemplateText());
                themeContentRepository.save(themeContent);

                model.addAttribute("successMsg", themeContent.getName() + " theme content was updated.");
                return themeContentList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to edit theme content: " + action);
        return editThemeContent(courseId, contentName, model, session);
    }

    @PostMapping(value = "/delete")
    public ModelAndView deleteThemeContent(@PathVariable("courseId") String courseId, @RequestParam String contentName, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/themeContent/delete");
        wizardAdminSecurity(courseId);

        if (!themeContentRepository.existsById(contentName)) {
            model.addAttribute("errorMsg", "Theme content does not exist: " + contentName);
            return themeContentList(courseId, model, session);
        }

        // delete
        themeContentRepository.deleteById(contentName);

        model.addAttribute("successMsg", contentName + " theme content was deleted.");
        return themeContentList(courseId, model, session);
    }

}
