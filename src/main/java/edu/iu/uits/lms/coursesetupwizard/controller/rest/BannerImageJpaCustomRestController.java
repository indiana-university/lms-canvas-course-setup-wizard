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

import edu.iu.uits.lms.coursesetupwizard.model.BannerImage;
import edu.iu.uits.lms.coursesetupwizard.model.BannerImageCategory;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/bannerimage")
@Tag(name = "BannerImageRepository")
@Slf4j
/*
 * This REST controller supplements the REST JPA endpoints auto-defined in BannerImageRepository.
 * That's why @Tag's name matches that repository so that via swagger these endpoints are grouped under
 * the JPA auto-generated ones.
 */
public class BannerImageJpaCustomRestController {
   @Autowired
   private BannerImageRepository bannerImageRepository;

   @Autowired
   private BannerImageCategoryRepository bannerImageCategoryRepository;


   @PutMapping("/{id}/addAssociatedCategory/{bannerImageCategoryId}")
   @Operation(summary = "Associate a given existing Banner Category with this Banner Image")
   public BannerImage associateById(@PathVariable("id") Long id,
                                    @PathVariable("bannerImageCategoryId") Long bannerImageCategoryId) {
      BannerImage bannerImage = bannerImageRepository.findById(id).orElse(null);
      BannerImageCategory bannerImageCategory = bannerImageCategoryRepository.findById(bannerImageCategoryId)
              .orElse(null);

      if (bannerImage != null && bannerImageCategory != null) {
         List<BannerImageCategory> bannerImageCategories = bannerImage.getBannerImageCategories();

         if (bannerImageCategories != null) {
            if (bannerImageCategories.stream()
                    .filter(bic -> bic.getId().equals(bannerImageCategoryId))
                    .findFirst()
                    .isEmpty()) {
               bannerImageCategories.add(bannerImageCategory);
               bannerImage = bannerImageRepository.save(bannerImage);
            }
         }
      }

      return bannerImage;
   }

   @PutMapping("/{id}/removeAssociatedCategory/{bannerImageCategoryId}")
   @Operation(summary = "Remove Association of an already associated Banner Category with this Banner Image")
   public BannerImage removeAssociationById(@PathVariable("id") Long id,
                                            @PathVariable("bannerImageCategoryId") Long bannerImageCategoryId) {
      BannerImage bannerImage = bannerImageRepository.findById(id).orElse(null);
      BannerImageCategory bannerImageCategory = bannerImageCategoryRepository.findById(bannerImageCategoryId)
              .orElse(null);

      if (bannerImage != null && bannerImageCategory != null) {
         List<BannerImageCategory> bannerImageCategories = bannerImage.getBannerImageCategories();

         if (bannerImageCategories != null) {
            if (bannerImageCategories.stream()
                    .anyMatch(bic -> bic.getId().equals(bannerImageCategoryId))) {
               bannerImageCategories.remove(bannerImageCategory);
               bannerImage = bannerImageRepository.save(bannerImage);
            }
         }
      }

      return bannerImage;
   }
}
