package edu.iu.uits.lms.coursesetupwizard.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PopupStatus {
   private String courseId;
   private String userId;
   private boolean dismissed;

}
