package edu.iu.uits.lms.coursesetupwizard.controller.rest;

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
