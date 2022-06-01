package edu.iu.uits.lms.coursesetupwizard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WizardService {

   public String sayHello(String courseId) {
      return "Hello, " + courseId;
   }

}
