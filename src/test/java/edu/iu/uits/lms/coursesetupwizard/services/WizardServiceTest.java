package edu.iu.uits.lms.coursesetupwizard.services;

import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@ContextConfiguration(classes={WizardService.class})
@SpringBootTest
@Disabled
@Slf4j
public class WizardServiceTest {

   @Autowired
   private WizardService wizardService;

   @MockBean
   private CourseService courseService;

//   @Test
//   void testMerging() throws Exception {
//      ImportModel mod1 = new ImportModel();
//      mod1.setAction("foo");
//      mod1.setCourseId("12345");
//      mod1.setSelectedCourseId("qwerty");
//
//      ImportModel mod2 = new ImportModel();
//      mod2.setCurrentPage(4);
//      ImportModel.ClassDates dates = new ImportModel.ClassDates();
//      dates.setOrigLast("1/2/34");
//      mod2.setClassDates(dates);
//      mod2.setSelectedCourseId("asdf");
//
//      ImportModel mergedMod = wizardService.mergeObjects(mod1, mod2);
//
//      Assertions.assertEquals("foo", mergedMod.getAction());
//      Assertions.assertEquals("12345", mergedMod.getCourseId());
//      Assertions.assertEquals(4, mergedMod.getCurrentPage());
//      Assertions.assertEquals(dates, mergedMod.getClassDates());
//      Assertions.assertEquals("qwerty", mergedMod.getSelectedCourseId());
//   }

   @Test
   void testMaps() {
      MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add("A", "foo");
      multiValueMap.add("A", "bar");
      multiValueMap.add("B", "qwerty");

      log.info("{}", multiValueMap);
   }
}
