package edu.iu.uits.lms.coursesetupwizard.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class WrappedPopupStatus implements Serializable {

   private PopupStatus popupStatus;
   private String csrfToken;

}
