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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "WIZARD_POPUP_DISMISSAL_DATES")
@SequenceGenerator(name = "WIZARD_POPUP_DISMISSAL_DATES_ID_SEQ", sequenceName = "WIZARD_POPUP_DISMISSAL_DATES_ID_SEQ", allocationSize = 1)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PopupDismissalDate {

   @Id
   @GeneratedValue(generator = "WIZARD_POPUP_DISMISSAL_DATES_ID_SEQ")
   private Long id;

   @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
   @Column(name = "DISMISS_UNTIL")
   private Date dismissUntil;

}
