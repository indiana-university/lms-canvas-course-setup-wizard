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

import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.model.WizardFeatureModel;
import edu.iu.uits.lms.iuonly.model.FeatureAccess;
import edu.iu.uits.lms.iuonly.repository.FeatureAccessRepository;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.lti.LTIConstants;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/app/admin/{courseId}/feature")
@Secured(LTIConstants.BASE_USER_AUTHORITY)
@Slf4j
public class FeatureAdminController extends WizardAdminController {

    @Autowired
    protected FeatureAccessRepository featureAccessRepository;

    @Autowired
    protected FeatureAccessServiceImpl featureAccessService;

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
    public ModelAndView featureList(@PathVariable("courseId") String courseId, Model model, HttpSession httpSession) {
        log.debug("in /admin/" + courseId + "/feature/list");
        wizardAdminSecurity(courseId);

        List<String> featureIds = Stream.of(Constants.WizardFeature.values()).map(Constants.WizardFeature::getFeatureId).collect(Collectors.toList());

        List<FeatureAccess> allFeatures = featureAccessRepository.findAll();
        List<FeatureAccess> wizardFeatures = allFeatures.stream()
                .filter(feature -> featureIds.contains(feature.getFeatureId()))
                .collect(Collectors.toList());

        List<WizardFeatureModel> featureList = new ArrayList<>();
        for (FeatureAccess feature : wizardFeatures) {
            WizardFeatureModel featureModel = new WizardFeatureModel(feature.getId(), feature.getFeatureId(), feature.getAccountId(), Constants.WizardFeature.findDisplayNameById(feature.getFeatureId()));
            featureList.add(featureModel);
        }

        model.addAttribute("featureList", featureList);

        List<Constants.WizardFeature> featureOptions = Arrays.asList(Constants.WizardFeature.values());
        model.addAttribute("featureOptions", featureOptions);

        return new ModelAndView("admin/feature");
    }

    @PostMapping(value = "/enable")
    public ModelAndView enableFeature(@PathVariable("courseId") String courseId, @RequestParam String featureId, @RequestParam String accountId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/feature/enable");
        wizardAdminSecurity(courseId);

        // verify that feature isn't already enabled
        if(featureAccessService.isFeatureEnabledForAccount(featureId, accountId, null)) {
            model.addAttribute("errorMsg", Constants.WizardFeature.findDisplayNameById(featureId) + " is already enabled for account " + accountId);
            return featureList(courseId, model, session);
        };

        FeatureAccess newAccess = new FeatureAccess();
        newAccess.setFeatureId(featureId);
        newAccess.setAccountId(accountId);

        featureAccessRepository.save(newAccess);

        model.addAttribute("successMsg", Constants.WizardFeature.findDisplayNameById(featureId) + " feature enabled for account " + accountId + ".");
        return featureList(courseId, model, session);
    }

    @PostMapping(value = "/delete")
    public ModelAndView deleteFeature(@PathVariable("courseId") String courseId, @RequestParam Long deleteId, Model model, HttpSession session) {
        log.debug("in /admin/" + courseId + "/feature/delete");
        wizardAdminSecurity(courseId);

        FeatureAccess feature = featureAccessRepository.findById(deleteId).orElse(null);

        if (!featureAccessRepository.existsById(deleteId)) {
            model.addAttribute("errorMsg", "Feature with id does not exist: " + deleteId);
            return featureList(courseId, model, session);
        }
        // delete
        featureAccessRepository.deleteById(deleteId);

        model.addAttribute("successMsg", Constants.WizardFeature.findDisplayNameById(feature.getFeatureId()) + " feature deleted for account " + feature.getAccountId() + ".");
        return featureList(courseId, model, session);
    }

}
