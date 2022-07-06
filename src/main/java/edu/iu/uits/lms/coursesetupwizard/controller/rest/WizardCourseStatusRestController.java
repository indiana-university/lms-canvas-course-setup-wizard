package edu.iu.uits.lms.coursesetupwizard.controller.rest;

import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/rest/coursestatus")
@Slf4j
public class WizardCourseStatusRestController {

   @Autowired
   private WizardCourseStatusRepository wizardCourseStatusRepository = null;

   @GetMapping("/{id}")
   public WizardCourseStatus get(@PathVariable Long id) {
      return wizardCourseStatusRepository.findById(id).orElse(null);
   }

   @GetMapping("/course/{courseId}")
   public WizardCourseStatus getByCourse(@PathVariable String courseId) {
      return wizardCourseStatusRepository.findByCourseId(courseId);
   }

   @GetMapping("/all")
   public List<WizardCourseStatus> getAll() {
      return (List<WizardCourseStatus>)wizardCourseStatusRepository.findAll();
   }

   @PutMapping("/{id}")
   public WizardCourseStatus update(@PathVariable Long id, @RequestBody WizardCourseStatus wizardCourseStatus) {
      WizardCourseStatus updatedWizardCourseStatus = wizardCourseStatusRepository.findById(id).orElse(null);

      if (updatedWizardCourseStatus != null) {
         if (wizardCourseStatus.getCourseId() != null) {
            updatedWizardCourseStatus.setCourseId(wizardCourseStatus.getCourseId());
         }
         if (wizardCourseStatus.getCompletedBy() != null) {
            updatedWizardCourseStatus.setCompletedBy(wizardCourseStatus.getCompletedBy());
         }
         if (wizardCourseStatus.getMainOption() != null) {
            updatedWizardCourseStatus.setMainOption(wizardCourseStatus.getMainOption());
         }
         if (wizardCourseStatus.getContentOption() != null) {
            updatedWizardCourseStatus.setContentOption(wizardCourseStatus.getContentOption());
         }
         if (wizardCourseStatus.getDateAdjustmentOption() != null) {
            updatedWizardCourseStatus.setDateAdjustmentOption(wizardCourseStatus.getDateAdjustmentOption());
         }

         return wizardCourseStatusRepository.save(updatedWizardCourseStatus);
      }
      return null;
   }

   @PostMapping("/")
   public WizardCourseStatus create(@RequestBody WizardCourseStatus wizardCourseStatus) {
      WizardCourseStatus newWizardCourseStatus = WizardCourseStatus.builder()
            .courseId(wizardCourseStatus.getCourseId())
            .completedBy(wizardCourseStatus.getCompletedBy())
            .mainOption(wizardCourseStatus.getMainOption())
            .contentOption(wizardCourseStatus.getContentOption())
            .dateAdjustmentOption(wizardCourseStatus.getDateAdjustmentOption())
            .build();
      return wizardCourseStatusRepository.save(newWizardCourseStatus);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      wizardCourseStatusRepository.deleteById(id);
      return "Delete success.";
   }

   @DeleteMapping("/course/{courseId}")
   public ResponseEntity<HttpStatus> deleteByCourse(@PathVariable String courseId) {
      try {
         WizardCourseStatus courseStatus = wizardCourseStatusRepository.findByCourseId(courseId);
         wizardCourseStatusRepository.delete(courseStatus);
         return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } catch (Exception e) {
         return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }
}
