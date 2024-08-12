package edu.iu.uits.lms.coursesetupwizard.controller.rest;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2024 Indiana University
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

import edu.iu.uits.lms.coursesetupwizard.model.Theme;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/theme")
@Tag(name = "ThemeRepository")
@Slf4j
/*
 * This REST controller supplements the REST JPA endpoints auto-defined in ThemeRepository.
 * That's why @Tag's name matches that repository so that via swagger these endpoints are grouped under
 * the JPA auto-generated ones.
 */
public class ThemeJpaCustomRestController {
   @Autowired
   private ThemeRepository themeRepository;

   @DeleteMapping("/{id}")
   @Operation(summary = "Soft Delete Theme By id ")
   public String softDeleteById(@PathVariable("id") Long id) {
      String result;

      Theme theme = themeRepository.findById(id).orElse(null);

      if (theme == null) {
         result = String.format("Could not find Theme with id %d", id);
      }
       else {
         themeRepository.softDeleteById(id);
         result = String.format("Soft Deleted Theme with id %d (%s)",
                 id, theme.getName());
      }

       return result;
   }

   @PostMapping("/undelete/{id}")
   @Operation(summary = "Un-Soft Delete Theme By id ")
   public String unSoftDeleteById(@PathVariable("id") Long id) {
      String result;

      Theme theme = themeRepository.findById(id).orElse(null);

      if (theme == null) {
         result = String.format("Could not find Theme with id %d", id);
      }
      else {
         themeRepository.unSoftDeleteById(id);
         result = String.format("Un Soft Deleted Theme with id %d (%s)",
                 id, theme.getName());
      }

      return result;
   }
}