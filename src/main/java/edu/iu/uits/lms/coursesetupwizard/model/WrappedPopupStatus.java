package edu.iu.uits.lms.coursesetupwizard.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class WrappedPopupStatus implements Serializable {

   private PopupStatus popupStatus;
   private String csrfToken;

   public WrappedPopupStatus(PopupStatus popupStatus, String csrfToken) {
      this.popupStatus = popupStatus;
      this.csrfToken = csrfToken;
   }

}
