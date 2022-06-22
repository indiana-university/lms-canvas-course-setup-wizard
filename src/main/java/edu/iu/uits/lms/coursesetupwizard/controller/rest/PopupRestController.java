package edu.iu.uits.lms.coursesetupwizard.controller.rest;

import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/popup")
@Slf4j
public class PopupRestController {

   @Autowired
   WizardService wizardService = null;

   @GetMapping("/{courseId}/{userId}/status")
   public PopupStatus popupStatus(@PathVariable String courseId, @PathVariable String userId) {
      return wizardService.getPopupDismissedStatus(courseId, userId);
   }

   @PostMapping("/{courseId}/{userId}/dismiss")
   public PopupStatus popupDismiss(@PathVariable String courseId, @PathVariable String userId,
                                   @RequestParam(defaultValue = "false", required = false) boolean global) {
      return wizardService.dismissPopup(courseId, userId, global);
   }
}
