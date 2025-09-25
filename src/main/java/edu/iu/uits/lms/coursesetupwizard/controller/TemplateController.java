package edu.iu.uits.lms.coursesetupwizard.controller;

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

import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import edu.iu.uits.lms.coursesetupwizard.service.WizardServiceException;
import edu.iu.uits.lms.iuonly.model.HierarchyResource;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_BACK;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_HOME;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_NEXT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.ACTION_SUBMIT;
import static edu.iu.uits.lms.coursesetupwizard.Constants.KEY_IMPORT_MODEL;

@Controller
@RequestMapping("/app/template")
@Slf4j
public class TemplateController extends WizardController {

   private static final String[] PAGES = {"/app/{0}/index", "/app/template/{0}/choose",
           "/app/template/{0}/review"};

   @GetMapping("/{courseId}/choose")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView choose(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /choose");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      Map<String, List<HierarchyResource>> templatesForCourse = wizardService.getTemplatesForCourse(courseId);

      model.addAttribute("templatesForCourse", templatesForCourse);

      return new ModelAndView("template/choose");
   }

   @GetMapping("/{courseId}/review")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView review(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
      log.debug("in /review");
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel importModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);
      if (importModel != null) {
         model.addAttribute("selectedTemplateName", importModel.getSelectedTemplateName());
      }

      return new ModelAndView("template/review");
   }

   @PostMapping("/{courseId}/navigate")
   @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
   public ModelAndView navigate(@PathVariable("courseId") String courseId, Model model, @ModelAttribute ImportModel importModel,
                                @RequestParam(name = "action") String action, @RequestParam(name = "currentPage") int currentPage,
                                HttpSession httpSession) {
      OidcAuthenticationToken token = getValidatedToken(courseId);
      OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

      ImportModel sessionImportModel = courseSessionService.getAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL, ImportModel.class);

      int pageIndex = 0;

      switch (action) {
         case ACTION_HOME:
            //Reset stuff
            courseSessionService.removeAttributeFromSession(httpSession, courseId, KEY_IMPORT_MODEL);
            break;
         case ACTION_BACK:
            pageIndex = currentPage - 1;
            break;
         case ACTION_NEXT:
            pageIndex = currentPage + 1;

            //Template selection page
            if (importModel.getSelectedTemplateId() != null) {
               sessionImportModel.setSelectedTemplateId(importModel.getSelectedTemplateId());
               sessionImportModel.setSelectedTemplateName(importModel.getSelectedTemplateName());
            }

            //Re-save the session model
            courseSessionService.addAttributeToSession(httpSession, courseId, KEY_IMPORT_MODEL, sessionImportModel);
            break;
         case ACTION_SUBMIT:
            String templateHostingUrl = toolConfig.getTemplateHostingUrl();
            // Use the current application as the template host if no other has been configured.
            if (templateHostingUrl == null) {
               templateHostingUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            }
            try {
               wizardService.doApplyTemplate(sessionImportModel, oidcTokenUtils.getUserLoginId(), templateHostingUrl);
               model.addAttribute("redirectUrl", getCanvasContentMigrationsToolUrl(courseId));
               // redirect to the Canvas tool
               return new ModelAndView(redirectToCanvas());
            } catch (WizardServiceException e) {
               model.addAttribute("submitError", e.getMessage());
               return review(courseId, model, httpSession);
            }
      }
      String url = MessageFormat.format(PAGES[pageIndex], courseId);
      return new ModelAndView("redirect:" + url);
   }
}
