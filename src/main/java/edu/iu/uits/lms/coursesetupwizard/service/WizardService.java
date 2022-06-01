package edu.iu.uits.lms.coursesetupwizard.service;

import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WizardService {

   public PopupStatus getPopupDismissedStatus(String courseId, String userId) {
      //TODO Add correct implementation (LMSA-8259)
      throw new NotImplementedException();
   }

   public PopupStatus dismissPopup(String courseId, String userId, boolean global) {
      //TODO Add correct implementation (LMSA-8259)
      throw new NotImplementedException();
   }

}
