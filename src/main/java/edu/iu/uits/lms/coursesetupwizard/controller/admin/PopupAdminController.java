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

import edu.iu.uits.lms.coursesetupwizard.model.PopupDismissalDate;
import edu.iu.uits.lms.coursesetupwizard.repository.PopupDismissalDateRepository;
import edu.iu.uits.lms.coursesetupwizard.service.PopupDateUtil;
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

import java.util.Date;
import java.util.List;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_CANCEL;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;

@Controller
@RequestMapping("/app/admin/{courseId}/popup")
@Secured(LTIConstants.BASE_USER_AUTHORITY)
@Slf4j
public class PopupAdminController extends WizardAdminController {

    @Autowired
    protected PopupDismissalDateRepository popupRepository;

    /**
     * Gets called before EVERY controller method
     * @param courseId Expected to be extracted as a path variable
     * @param model model
     */
    @ModelAttribute
    public void addCommonAttributesToModel(@PathVariable("courseId") String courseId, Model model) {
        model.addAttribute("courseId", courseId);
    }

    @GetMapping({"/list"})
    public ModelAndView popupList(@PathVariable("courseId") String courseId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/popup/list");

        wizardAdminSecurity(courseId);

        List<PopupDismissalDate> popupList = (List<PopupDismissalDate>)popupRepository.findAll(Sort.by("showOn").descending());

        model.addAttribute("popupList", popupList);

        return new ModelAndView("admin/popup");
    }

    @GetMapping("/{popupId}/edit")
    public ModelAndView editPopup(@PathVariable("courseId") String courseId, @PathVariable("popupId") Long popupId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/popup/" + popupId + "/edit");
        wizardAdminSecurity(courseId);

        model.addAttribute("popupId", popupId);

        PopupDismissalDate popup = popupRepository.findById(popupId).orElse(null);

        // If the popup is in the past, show a read-only view
        if (popup.getShowOn().before(new Date())) {
            model.addAttribute("readOnly", true);
        }

        model.addAttribute("popupForm", popup);

        return new ModelAndView("admin/editPopup");
    }

    @GetMapping("/new")
    public ModelAndView createPopupDate(@PathVariable("courseId") String courseId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/popup/new");
        wizardAdminSecurity(courseId);

        model.addAttribute("create", true);

        if (model.getAttribute("popupForm") == null) {
            model.addAttribute("popupForm", new PopupDismissalDate());
        }

        return new ModelAndView("admin/editPopup");
    }

    @PostMapping(value = "/{popupId}/update")
    public ModelAndView editPopupSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @PathVariable("popupId") Long popupId, @ModelAttribute PopupDismissalDate popupModel, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/popup/" + popupId + "/update");
        wizardAdminSecurity(courseId);

        PopupDismissalDate updatedPopupDismissalDate = popupRepository.findById(popupId).orElse(null);

        if (updatedPopupDismissalDate == null) {
            model.addAttribute("errorMsg", "Popup reset with id " + popupId + " does not exist.");
            return editPopup(courseId, popupId, model, session);
        }

        switch (action) {
            case ACTION_CANCEL:
                return popupList(courseId, model, session);
            case ACTION_SUBMIT:
                try {
                    PopupDateUtil.validate(popupModel.getShowOn(), PopupDateUtil.INPUT_DATE_FORMAT_MMDDYYYY);
                } catch (IllegalArgumentException iae) {
                    model.addAttribute("errorMsg", iae.getMessage());
                    return editPopup(courseId, popupId, model, session);
                }

                updatedPopupDismissalDate.setShowOn(popupModel.getShowOn());
                updatedPopupDismissalDate.setNotes(popupModel.getNotes());

                if (!updatedPopupDismissalDate.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "Updated popup reset is missing required fields.");
                    return editPopup(courseId, popupId, model, session);
                }

                popupRepository.save(updatedPopupDismissalDate);

                model.addAttribute("successMsg", "The popup reset was updated successfully.");
                return popupList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to edit a popup: " + action);
        return createPopupDate(courseId, model, session);
    }

    @PostMapping(value = "/new/submit")
    public ModelAndView createPopupSubmit(@PathVariable("courseId") String courseId, @RequestParam(name = "action") String action, @ModelAttribute PopupDismissalDate popupModel, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/popup/new/submit");
        wizardAdminSecurity(courseId);

        switch (action) {
            case ACTION_CANCEL:
                return popupList(courseId, model, session);
            case ACTION_SUBMIT:
                // validate the date
                try {
                    PopupDateUtil.validate(popupModel.getShowOn(), PopupDateUtil.INPUT_DATE_FORMAT_MMDDYYYY);
                } catch (IllegalArgumentException iae) {
                    model.addAttribute("errorMsg", iae.getMessage());
                    model.addAttribute("popupForm", popupModel);
                    return createPopupDate(courseId, model, session);
                }

                PopupDismissalDate newPopup = new PopupDismissalDate();
                newPopup.setShowOn(popupModel.getShowOn());
                newPopup.setNotes(popupModel.getNotes());

                if (!newPopup.isValid()) {
                    // We shouldn't get here because of the client side validation, but just in case...
                    model.addAttribute("errorMsg", "New popup reset is missing required fields.");
                    return createPopupDate(courseId, model, session);
                }

                popupRepository.save(newPopup);

                model.addAttribute("successMsg", "The popup reset was created successfully.");
                return popupList(courseId, model, session);
        }

        model.addAttribute("errorMsg", "Unknown action when attempting to create a new popup: " + action);
        return createPopupDate(courseId, model, session);
    }
}
