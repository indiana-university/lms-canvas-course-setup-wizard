package edu.iu.uits.lms.coursesetupwizard.model;

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

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "WIZARD_COURSE_STATUS",
      uniqueConstraints = @UniqueConstraint(name = "UK_WIZARD_COURSE_STATUS", columnNames = {"course_id"}))
@SequenceGenerator(name = "WIZARD_COURSE_STATUS_ID_SEQ", sequenceName = "WIZARD_COURSE_STATUS_ID_SEQ", allocationSize = 1)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WizardCourseStatus {

   @Id
   @GeneratedValue(generator = "WIZARD_COURSE_STATUS_ID_SEQ")
   private Long id;

   @Column(name = "COURSE_ID", nullable = false)
   private String courseId;

   @Column(name = "COMPLETED_BY", nullable = false)
   private String completedBy;

   @Enumerated(EnumType.STRING)
   @Column(name = "MAIN_OPTION")
   private Constants.MAIN_OPTION mainOption;

   @Enumerated(EnumType.STRING)
   @Column(name = "CONTENT_OPTION")
   private Constants.CONTENT_OPTION contentOption;

   @Enumerated(EnumType.STRING)
   @Column(name = "DATE_ADJUSTMENT_OPTION")
   private Constants.DATE_OPTION dateAdjustmentOption;

   @Column(name = "SELECTED_TEMPLATE_ID")
   private String selectedTemplateId;

   @Column(name = "CONTENT_MIGRATION_ID")
   private String contentMigrationId;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "COMPLETED_ON")
   private Date completedOn;

   @PreUpdate
   @PrePersist
   public void updateTimeStamps() {
      completedOn = new Date();
   }
}
