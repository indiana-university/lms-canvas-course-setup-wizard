package edu.iu.uits.lms.coursesetupwizard.services;

import edu.iu.uits.lms.coursesetupwizard.service.PopupDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

@Slf4j
public class PopupDateUtilTest {

   @Test
   void testMissingDate() {
      IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate(null));
      Assertions.assertEquals("Please provide a date", t.getMessage());
   }

   @Test
   void testBadDateFormat() {
      IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate(""));
      Assertions.assertEquals("Please provide a date in the format: yyyy/MM/dd", t.getMessage());

      t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate("asdf"));
      Assertions.assertEquals("Please provide a date in the format: yyyy/MM/dd", t.getMessage());
   }

   @Test
   void testDateHasPassed() {
      IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
            PopupDateUtil.validateDate("1999/01/01"));
      Assertions.assertEquals("Please provide a date in the future", t.getMessage());
   }

   @Test
   void testGoodDate() {
      Date d = PopupDateUtil.validateDate("2222/01/01");
      Assertions.assertNotNull(d);

      Calendar cal = Calendar.getInstance();
      cal.setTime(d);

      cal.get(Calendar.YEAR);
   }

}
