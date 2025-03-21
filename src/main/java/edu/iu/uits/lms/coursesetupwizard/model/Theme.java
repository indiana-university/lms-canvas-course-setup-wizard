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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Entity
@Table(name = "CSW_THEME")
@SequenceGenerator(name = "CSW_THEME_ID_SEQ", sequenceName = "CSW_THEME_ID_SEQ", allocationSize = 1)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Theme {
    @Id
    @GeneratedValue(generator = "CSW_THEME_ID_SEQ")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "UI_NAME", nullable = false)
    private String uiName;

    @Column(name = "ALT_TEXT", nullable = false)
    private String altText;

    @Column(name = "IMAGE_URL", nullable = false)
    private String imageUrl;

    @Column(name = "NAV_IMAGE_PREVIEW_URL", nullable = false)
    private String navImagePreviewUrl;

    @Column(name = "NAV_IMAGE_ALT_TEXT", nullable = false)
    private String navImageAltText;

    @Column(name = "JUST_BANNER_IMAGE_PREVIEW_URL", nullable = false)
    private String justBannerImagePreviewUrl;

    @Column(name = "JUST_BANNER_IMAGE_ALT_TEXT", nullable = false)
    private String justBannerImageAltText;

    @Column(name = "JUST_NAV_IMAGE_PREVIEW_URL", nullable = false)
    private String justNavImagePreviewUrl;

    @Column(name = "JUST_NAV_IMAGE_ALT_TEXT", nullable = false)
    private String justNavImageAltText;

    @Column(name = "JUST_HEADER_IMAGE_PREVIEW_URL", nullable = false)
    private String justHeaderImagePreviewUrl;

    @Column(name = "JUST_HEADER_IMAGE_ALT_TEXT", nullable = false)
    private String justHeaderImageAltText;

    @Column(name = "WRAPPER_CSS_CLASSES", nullable = false)
    private String wrapperCssClasses;

    @Column(name = "HEADER_CSS_CLASSES", nullable = false)
    private String headerCssClasses;

    @Column(name = "BANNER_IMAGE_CSS_CLASSES", nullable = false)
    private String bannerImageCssClasses;

    @Column(name = "NAVIGATION_CSS_CLASSES", nullable = false)
    private String navigationCssClasses;

    @Column(name = "ACTIVE", columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(name = "CREATEDON")
    private Date createdOn;

    @Column(name = "MODIFIEDON")
    private Date modifiedOn;

    public void mergeEditableFields(Theme editable) {
        if (editable != null) {
            this.active = editable.isActive();
            this.name = editable.getName().trim();
            this.uiName = editable.getUiName().trim();
            this.altText = editable.getAltText().trim();
            this.imageUrl = editable.getImageUrl().trim();
            this.navImagePreviewUrl = editable.getNavImagePreviewUrl().trim();
            this.navImageAltText = editable.getNavImageAltText().trim();
            this.justBannerImagePreviewUrl = editable.getJustBannerImagePreviewUrl().trim();
            this.justBannerImageAltText = editable.getJustBannerImageAltText().trim();
            this.justNavImagePreviewUrl = editable.getJustNavImagePreviewUrl().trim();
            this.justNavImageAltText = editable.getJustNavImageAltText().trim();
            this.justHeaderImagePreviewUrl = editable.getJustHeaderImagePreviewUrl().trim();
            this.justHeaderImageAltText = editable.getJustHeaderImageAltText().trim();
            this.wrapperCssClasses = editable.getWrapperCssClasses().trim();
            this.headerCssClasses = editable.getHeaderCssClasses().trim();
            this.bannerImageCssClasses = editable.getBannerImageCssClasses().trim();
            this.navigationCssClasses = editable.getNavigationCssClasses().trim();
        }
    }

    public boolean isValid() {
        return StringUtils.isNoneBlank(name, uiName, altText, imageUrl, navImagePreviewUrl, navImageAltText, justBannerImagePreviewUrl, justBannerImageAltText, justNavImagePreviewUrl, justNavImageAltText, justHeaderImagePreviewUrl, justHeaderImageAltText);
    }

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modifiedOn = new Date();
        if (createdOn == null) {
            createdOn = new Date();
        }
    }
}