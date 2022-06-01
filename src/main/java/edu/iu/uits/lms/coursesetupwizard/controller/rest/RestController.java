package edu.iu.uits.lms.coursesetupwizard.controller.rest;

import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/rest")
@Slf4j
public class RestController {

   @Autowired
   WizardService wizardService = null;

   @GetMapping("/{courseId}/info")
   public String info(@PathVariable String courseId) {
      return wizardService.sayHello(courseId);
   }

}
