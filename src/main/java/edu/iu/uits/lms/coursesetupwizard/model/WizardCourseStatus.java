package edu.iu.uits.lms.coursesetupwizard.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
