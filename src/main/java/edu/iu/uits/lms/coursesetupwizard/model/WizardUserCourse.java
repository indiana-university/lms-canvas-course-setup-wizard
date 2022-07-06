package edu.iu.uits.lms.coursesetupwizard.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Entity
@Table(name = "WIZARD_USER_COURSE",
      uniqueConstraints = @UniqueConstraint(name = "UK_WIZARD_USER_COURSE", columnNames = {"username", "course_id"}))
@SequenceGenerator(name = "WIZARD_USER_COURSE_ID_SEQ", sequenceName = "WIZARD_USER_COURSE_ID_SEQ", allocationSize = 1)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WizardUserCourse {

   //Constant used for a courseID to indicate a global setting
   public static final String GLOBAL = "GLOBAL";

   @Id
   @GeneratedValue(generator = "WIZARD_USER_COURSE_ID_SEQ")
   private Long id;

   @Column(name = "USERNAME", nullable = false)
   private String username;

   @Column(name = "COURSE_ID", nullable = false)
   private String courseId;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "CREATED")
   private Date createdOn;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "MODIFIED")
   private Date modifiedOn;

   @PreUpdate
   @PrePersist
   public void updateTimeStamps() {
      modifiedOn = new Date();
      if (createdOn==null) {
         createdOn = new Date();
      }
   }
}
