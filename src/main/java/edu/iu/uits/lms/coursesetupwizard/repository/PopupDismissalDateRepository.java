package edu.iu.uits.lms.coursesetupwizard.repository;

import edu.iu.uits.lms.coursesetupwizard.model.PopupDismissalDate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public interface PopupDismissalDateRepository extends PagingAndSortingRepository<PopupDismissalDate, Long> {

   @Query(value = "SELECT pdd FROM PopupDismissalDate pdd WHERE pdd.dismissUntil > sysdate AND ROWNUM = 1 ORDER BY pdd.dismissUntil")
   PopupDismissalDate getNextDismissalDate();

   @Modifying
   @Query("DELETE FROM PopupDismissalDate pdd where pdd.dismissUntil < sysdate")
   @Transactional("cswTransactionMgr")
   int removePastDates();

}
