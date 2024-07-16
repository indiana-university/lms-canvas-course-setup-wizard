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

import edu.iu.uits.lms.coursesetupwizard.model.ThemeContent;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeContentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/rest/themecontent")
@Tag(name = "ThemeContentRepository")
@Slf4j
/*
 * This REST controller supplements the REST JPA endpoints auto-defined in BannerImageRepository.
 * That's why @Tag's name matches that repository so that via swagger these endpoints are grouped under
 * the JPA auto-generated ones.
 */
public class ThemeContentJpaCustomRestController {
   @Autowired
   private ThemeContentRepository themeContentRepository;

   @GetMapping("/{name}/getTemplateTextAsFile")
   @Operation(summary = "Get the Template Text for the given themeContent as a file to download")
   public ResponseEntity<byte[]> getTemplateTextAsFile(@PathVariable("name") String name) {
      ThemeContent themeContent = themeContentRepository.findById(name).orElseThrow(EntityNotFoundException::new);

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + themeContent.getName() + ".txt\"");
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

      return ResponseEntity.ok()
              .headers(headers)
              .body(themeContent.getTemplateText().getBytes());
   }

   @PutMapping(value = "/{name}/putTemplateTextAsFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   @Operation(summary = "Update the Template Text for the given themeContent from the file uploaded")
   public ResponseEntity<ThemeContent> putTemplateTextAsFile(@PathVariable("name") String name, @RequestParam("templateTextFile") MultipartFile templateTextFile) throws IOException {
      ThemeContent themeContent = themeContentRepository.findById(name).orElseThrow(EntityNotFoundException::new);

      themeContent.setTemplateText(new String(templateTextFile.getBytes()));

      return ResponseEntity.ok(themeContentRepository.save(themeContent));
   }
}