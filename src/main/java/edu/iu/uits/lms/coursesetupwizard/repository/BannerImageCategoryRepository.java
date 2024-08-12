package edu.iu.uits.lms.coursesetupwizard.repository;

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

import edu.iu.uits.lms.coursesetupwizard.model.BannerImageCategory;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;
import java.util.Optional;

@Component
@RepositoryRestResource(path = "bannerimagecategory",
        itemResourceDescription = @Description("Banner Image Category"),
        collectionResourceDescription = @Description("Banner Image Categories"))
@Tag(name = "BannerImageCategoryRepository", description = "Interact with Banner Image Categories CRUD operations")
/*
 * These REST JPA auto-defined endpoints are supplemented by some in BannerImageCategoryJpaCustomRestController
 */
public interface BannerImageCategoryRepository extends PagingAndSortingRepository<BannerImageCategory, Long> {
    @Modifying
    @Query("UPDATE BannerImageCategory as bic SET bic.active = 'N' WHERE bic.id = :id")
    @Transactional
    @RestResource(exported = false)
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE BannerImageCategory as bic SET bic.active = 'Y' WHERE bic.id = :id")
    @Transactional
    @RestResource(exported = false)
    void unSoftDeleteById(@Param("id") Long id);

    @Override
    @RestResource(exported = false)
    void deleteById(@Param("id") Long id);

    List<BannerImageCategory> findByActiveTrueOrderByName();
}