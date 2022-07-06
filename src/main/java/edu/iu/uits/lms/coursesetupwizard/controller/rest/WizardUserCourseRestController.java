package edu.iu.uits.lms.coursesetupwizard.controller.rest;

import edu.iu.uits.lms.coursesetupwizard.model.WizardUserCourse;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardUserCourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/wuc")
@Slf4j
public class WizardUserCourseRestController {

   @Autowired
   private WizardUserCourseRepository wizardUserCourseRepository = null;

   @GetMapping("/{id}")
   public WizardUserCourse get(@PathVariable Long id) {
      return wizardUserCourseRepository.findById(id).orElse(null);
   }

   @GetMapping("/username/{username}")
   public List<WizardUserCourse> getByUsername(@PathVariable String username) {
      return wizardUserCourseRepository.findByUsername(username);
   }

   @GetMapping("/all")
   public List<WizardUserCourse> getAll() {
      return (List<WizardUserCourse>)wizardUserCourseRepository.findAll();
   }

   @PutMapping("/{id}")
   public WizardUserCourse update(@PathVariable Long id, @RequestBody WizardUserCourse wizardUserCourse) {
      WizardUserCourse updatedWizardUserCourse = wizardUserCourseRepository.findById(id).orElse(null);

      if (updatedWizardUserCourse != null) {
         if (wizardUserCourse.getCourseId() != null) {
            updatedWizardUserCourse.setCourseId(wizardUserCourse.getCourseId());
         }
         if (wizardUserCourse.getUsername() != null) {
            updatedWizardUserCourse.setUsername(wizardUserCourse.getUsername());
         }

         return wizardUserCourseRepository.save(updatedWizardUserCourse);
      }
      return null;
   }

   @PostMapping("/")
   public WizardUserCourse create(@RequestBody WizardUserCourse wizardUserCourse) {
      WizardUserCourse newWizardUserCourse = WizardUserCourse.builder()
            .courseId(wizardUserCourse.getCourseId())
            .username(wizardUserCourse.getUsername())
            .build();
      return wizardUserCourseRepository.save(newWizardUserCourse);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      wizardUserCourseRepository.deleteById(id);
      return "Delete success.";
   }

}
