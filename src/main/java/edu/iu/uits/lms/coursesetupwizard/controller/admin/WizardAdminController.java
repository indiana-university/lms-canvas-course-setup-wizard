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

import edu.iu.uits.lms.coursesetupwizard.service.WizardAdminException;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import static edu.iu.uits.lms.coursesetupwizard.Constants.WIZARD_ADMIN_COURSE;

@Controller
@RequestMapping("/app/admin")
@Slf4j
public class WizardAdminController extends OidcTokenAwareController {

    @Secured(LTIConstants.BASE_USER_AUTHORITY)
    @RequestMapping({"/launch", "/loading"})
    public String loading(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String courseId = oidcTokenUtils.getCourseId();

        model.addAttribute("adminTool", true);
        model.addAttribute("courseId", courseId);
        model.addAttribute("hideFooter", true);
        model.addAttribute("toolPath", "/app/admin/" + courseId + "/popup/list");
        return "loading";
    }

    protected boolean wizardAdminSecurity(String courseId) throws WizardAdminException {
        if (courseId == null) {
            throw new WizardAdminException("Missing courseId");
        }

        OidcAuthenticationToken token = getValidatedToken(courseId);
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        String adminCourseId = oidcTokenUtils.getCustomValue(WIZARD_ADMIN_COURSE);

        if (!courseId.equals(adminCourseId)) {
            log.info("User " + oidcTokenUtils.getUserLoginId() + " attempted to access the wizard admin tools from course " + courseId + ". Wizard course is: " + adminCourseId);
            throw new WizardAdminException("User is not authorized to use the Wizard Admin Tool.");
        }

        return true;
    }

}
