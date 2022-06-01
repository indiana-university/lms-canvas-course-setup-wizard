package edu.iu.uits.lms.coursesetupwizard.services;

import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.lti.service.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = {"oauth.tokenprovider.url=http://foo"})
@Import(ToolConfig.class)
@Slf4j
@ActiveProfiles("none")
public class RestLaunchSecurityTest {

   public static String COURSE_ID_TST = "1234";

   @Autowired
   private MockMvc mvc;

   @MockBean
   private WizardService wizardService;

   @BeforeEach
   public void setup() {
      String message = "Hello, 1234";
      when(wizardService.sayHello(COURSE_ID_TST)).thenReturn(message);
   }

   @Test
   public void restNoAuthnLaunch() throws Exception {
      //This is not a secured endpoint so should be successful
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/1234/info")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.buttonDisplayText").value("Unlock Course"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.buttonRendered").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseLocked").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseId").value(COURSE_ID_TST));
   }

}
