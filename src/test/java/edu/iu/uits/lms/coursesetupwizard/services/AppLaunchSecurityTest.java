package edu.iu.uits.lms.coursesetupwizard.services;

import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardUserCourseRepository;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.service.TestUtils;
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
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = {"oauth.tokenprovider.url=http://foo"})
@Import(ToolConfig.class)
@ActiveProfiles("none")
public class AppLaunchSecurityTest {

   public static String COURSE_ID_TST = "1234";

   @Autowired
   private MockMvc mvc;

   @MockBean
   private WizardService wizardService;

   @MockBean
   private WizardCourseStatusRepository wizardCourseStatusRepository;

   @MockBean
   private WizardUserCourseRepository wizardUserCourseRepository;

   @BeforeEach
   public void setup() {
//      Model status = new Model(true, true, "1234");
//      when(courseUnlockerService.getCourseUnlockStatus(COURSE_ID_TST)).thenReturn(status);
   }

   @Test
   public void appNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/"  + COURSE_ID_TST + "/index")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void appAuthnWrongContextLaunch() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
            "asdf", LTIConstants.INSTRUCTOR_AUTHORITY);

      SecurityContextHolder.getContext().setAuthentication(token);

      mvc.perform(get("/app/"  + COURSE_ID_TST + "/index")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isInternalServerError())
              .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
              .andExpect(MockMvcResultMatchers.view().name ("error"));
   }

   @Test
   public void appAuthnLaunch() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
              COURSE_ID_TST, LTIConstants.INSTRUCTOR_AUTHORITY);

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/"  + COURSE_ID_TST + "/index")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk());
   }

   @Test
   public void appAuthnLaunchNonInstructor() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
              COURSE_ID_TST, LTIConstants.TA_AUTHORITY);

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not allow access without instructor authn
      mvc.perform(get("/app/"  + COURSE_ID_TST + "/index")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlNoAuth() throws Exception {
      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlWithAuth() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
              COURSE_ID_TST, LTIConstants.BASE_USER_AUTHORITY);
      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
   }

}
