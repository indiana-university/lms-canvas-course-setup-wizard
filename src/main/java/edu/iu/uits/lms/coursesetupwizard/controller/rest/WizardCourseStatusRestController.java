package edu.iu.uits.lms.coursesetupwizard.controller.rest;

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

import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "WizardCourseStatusRestController", description = "Interact with the WizardCourseStatus repository with CRUD operations")
@Slf4j
public class WizardCourseStatusRestController {

   @Autowired
   private WizardCourseStatusRepository wizardCourseStatusRepository = null;

   @GetMapping("/{id}")
   @Operation(summary = "Get a WizardCourseStatus by id")
   public WizardCourseStatus get(@PathVariable Long id) {
      return wizardCourseStatusRepository.findById(id).orElse(null);
   }

   @GetMapping("/course/{courseId}")
   @Operation(summary = "Get a WizardCourseStatus by courseId")
   public WizardCourseStatus getByCourse(@PathVariable String courseId) {
      return wizardCourseStatusRepository.findByCourseId(courseId);
   }

   @GetMapping("/all")
   @Operation(summary = "Get a all WizardCourseStatus objects")
   public List<WizardCourseStatus> getAll() {
      return (List<WizardCourseStatus>)wizardCourseStatusRepository.findAll();
   }

   @PutMapping("/{id}")
   @Operation(summary = "Update a WizardCourseStatus by id")
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
   @Operation(summary = "Create a new WizardCourseStatus")
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
   @Operation(summary = "Delete a WizardCourseStatus by id")
   public String delete(@PathVariable Long id) {
      wizardCourseStatusRepository.deleteById(id);
      return "Delete success.";
   }

   @DeleteMapping("/course/{courseId}")
   @Operation(summary = "Delete a WizardCourseStatus by courseId")
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
