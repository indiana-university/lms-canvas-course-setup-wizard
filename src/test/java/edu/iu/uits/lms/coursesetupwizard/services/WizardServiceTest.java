package edu.iu.uits.lms.coursesetupwizard.services;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
