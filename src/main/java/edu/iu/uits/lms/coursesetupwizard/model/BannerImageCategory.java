package edu.iu.uits.lms.coursesetupwizard.model;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "CSW_BANNER_IMAGE_CATEGORY")
@SequenceGenerator(name = "CSW_BANNER_IMAGE_CATEGORY_ID_SEQ", sequenceName = "CSW_BANNER_IMAGE_CATEGORY_ID_SEQ", allocationSize = 1)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BannerImageCategory {
    @Id
    @GeneratedValue(generator = "CSW_BANNER_IMAGE_CATEGORY_ID_SEQ")
    private Long id;

    @Column(name = "ACTIVE", columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(name = "NAME", nullable = false)
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "bannerImageCategories")
    @OrderBy("name ASC")
    @ToString.Exclude
    private List<BannerImage> bannerImages;

    @JsonIgnore
    public List<BannerImage> getActiveBannerImages() {
        return bannerImages.stream()
                .filter(BannerImage::isActive)
                .collect(Collectors.toList());
    }

    @Column(name = "CREATEDON")
    private Date createdOn;

    @Column(name = "MODIFIEDON")
    private Date modifiedOn;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modifiedOn = new Date();
        if (createdOn == null) {
            createdOn = new Date();
        }
    }

    public boolean isValid() {
        return StringUtils.isNoneBlank(name);
    }

}
