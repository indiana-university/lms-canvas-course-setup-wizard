package edu.iu.uits.lms.coursesetupwizard.model;

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

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "CSW_BANNER_IMAGE")
@SequenceGenerator(name = "CSW_BANNER_IMAGE_ID_SEQ", sequenceName = "CSW_BANNER_IMAGE_ID_SEQ", allocationSize = 1)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BannerImage {
    @Id
    @GeneratedValue(generator = "CSW_BANNER_IMAGE_ID_SEQ")
    private Long id;

    @Column(name = "ACTIVE", columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "UI_NAME", nullable = false)
    private String uiName;

    @Column(name = "ALT_TEXT")
    private String altText;

    @Column(name = "BANNER_IMAGE_URL", nullable = false)
    private String bannerImageUrl;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "CSW_BANNER_IMAGE_X_CATEGORY", joinColumns = @JoinColumn(name = "BANNER_IMAGE_ID"),
               inverseJoinColumns = @JoinColumn(name = "BANNER_IMAGE_CATEGORY_ID"),
               indexes = {@Index(name = "IDX_CSW_BAN_IMG_X_CAT_BID", columnList = "BANNER_IMAGE_ID"),
                          @Index(name = "IDX_CSW_BAN_IMG_X_CAT_CAT", columnList = "BANNER_IMAGE_CATEGORY_ID")},
               foreignKey = @ForeignKey(name = "FK_CSW_BANNER_IMAGE_ID"), inverseForeignKey = @ForeignKey(name = "FK_CSW_BANNER_IMAGE_CATEGORY_ID"),
               uniqueConstraints = @UniqueConstraint(columnNames = {"BANNER_IMAGE_ID", "BANNER_IMAGE_CATEGORY_ID"}))
    @OrderBy("name ASC")
    private List<BannerImageCategory> bannerImageCategories;

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
}