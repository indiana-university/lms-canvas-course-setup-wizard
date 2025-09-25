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

import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.model.PopupDismissalDate;
import edu.iu.uits.lms.coursesetupwizard.repository.PopupDismissalDateRepository;
import edu.iu.uits.lms.coursesetupwizard.service.PopupDateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/rest/popupDates")
@Tag(name = "PopupDismissalDateRestController", description = "Interact with the PopupDismissalDate repository with CRUD operations")
@Slf4j
public class PopupDismissalDateRestController {

   @Autowired
   private PopupDismissalDateRepository popupDismissalDateRepository = null;

   @Autowired
   private ToolConfig toolConfig;

   @GetMapping("/{id}")
   @Operation(summary = "Get a PopupDismissalDate by id")
   public PopupDismissalDate get(@PathVariable Long id) {
      return popupDismissalDateRepository.findById(id).orElse(null);
   }

   @GetMapping("/all")
   @Operation(summary = "Get all PopupDismissalDate objects")
   public List<PopupDismissalDate> getAll() {
      return (List<PopupDismissalDate>)popupDismissalDateRepository.findAll();
   }

   @PutMapping("/{id}")
   @Operation(summary = "Update a PopupDismissalDate by id")
   public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InputDismissalData inputDismissalData) {
      PopupDismissalDate updatedPopupDismissalDate = popupDismissalDateRepository.findById(id).orElse(null);

      if (updatedPopupDismissalDate != null) {
         try {
            if (inputDismissalData.getShowOn() != null) {
               updatedPopupDismissalDate.setShowOn(PopupDateUtil.validateDate(inputDismissalData.getShowOn(), toolConfig.getTimezone()));
            }
            if (inputDismissalData.getNotes() != null) {
               updatedPopupDismissalDate.setNotes(inputDismissalData.getNotes());
            }
            if (inputDismissalData.isClearNotes()) {
               updatedPopupDismissalDate.setNotes(null);
            }
            return ResponseEntity.ok(popupDismissalDateRepository.save(updatedPopupDismissalDate));
         } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
         }
      } else {
         return ResponseEntity.badRequest().body("Existing record not found. Check the id provided and try again.");
      }
   }

   @PostMapping("/")
   @Operation(summary = "Create a new PopupDismissalDate")
   public ResponseEntity<?> create(@RequestBody InputDismissalData inputDismissalData) {
      try {
         PopupDismissalDate newPopupDismissalDate = PopupDismissalDate.builder()
               .showOn(PopupDateUtil.validateDate(inputDismissalData.getShowOn(), toolConfig.getTimezone()))
               .notes(inputDismissalData.getNotes())
               .build();
         return ResponseEntity.ok(popupDismissalDateRepository.save(newPopupDismissalDate));
      } catch (IllegalArgumentException e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @DeleteMapping("/{id}")
   @Operation(summary = "Delete a PopupDismissalDate by id")
   public String delete(@PathVariable Long id) {
      popupDismissalDateRepository.deleteById(id);
      return "Delete success.";
   }

   @Data
   static class InputDismissalData implements Serializable {
      private String showOn;
      private String notes;
      private boolean clearNotes;
   }
}
