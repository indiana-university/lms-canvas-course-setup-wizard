package edu.iu.uits.lms.coursesetupwizard.services;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2025 Indiana University
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

import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.common.server.ServerInfo;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.coursesetupwizard.config.SecurityConfig;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.controller.WizardController;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.config.TestUtils;
import edu.iu.uits.lms.lti.controller.InvalidTokenContextException;
import edu.iu.uits.lms.canvasoauth2.CanvasOAuth2Registration;
import edu.iu.uits.lms.canvasoauth2.security.CanvasOAuth2AuthorizedClientRepository;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = {"oauth.tokenprovider.url=http://foo"})
@ContextConfiguration(classes = {ToolConfig.class, WizardController.class, SecurityConfig.class, AppLaunchSecurityTest.TestConfig.class})
@ActiveProfiles("none")
public class AppLaunchSecurityTest {

   /**
    * Plain {@code @Bean}s rather than {@code @MockitoBean} - see blueprint-manager's identical
    * TestConfig in its AppLaunchSecurityTest for why: OAuth2ClientWebSecurityAutoConfiguration's
    * {@code @ConditionalOnMissingBean(OAuth2AuthorizedClientRepository.class)} doesn't recognize a
    * same-named {@code @MockitoBean} of the narrower concrete type as already satisfying it, so both
    * beans get created and autowiring the interface type elsewhere becomes ambiguous. A regular
    * {@code @Bean} factory method participates in that condition check correctly.
    */
   @TestConfiguration
   static class TestConfig {
      @Bean
      public CanvasOAuth2Registration canvasOAuth2Registration() {
         return new CanvasOAuth2Registration("coursesetupwizard", "/app/jsrivet");
      }

      @Bean
      public CanvasOAuth2AuthorizedClientRepository canvasOAuth2AuthorizedClientRepository() {
         return mock(CanvasOAuth2AuthorizedClientRepository.class);
      }
   }

   public static String COURSE_ID_TST = "1234";

   @Autowired
   private MockMvc mvc;

   // SecurityConfig now @Autowired-injects this from CanvasOAuth2ClientConfig, which this narrow
   // @WebMvcTest slice deliberately doesn't pull in (see TestConfig above) - it's never invoked by
   // any of these tests, only needed to satisfy the filter chain's dependency at context-build time.
   @MockitoBean
   private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> canvasOAuth2AccessTokenResponseClient;

   // Provided by TestConfig's @Bean (not @MockitoBean - see its javadoc).
   @Autowired
   private CanvasOAuth2AuthorizedClientRepository canvasOAuth2AuthorizedClientRepository;

   @BeforeEach
   void resetCanvasOAuth2AuthorizedClientRepositoryMock() {
      // TestConfig's @Bean isn't a @MockitoBean, so it doesn't get Mockito's automatic reset-between-
      // tests behavior - do it manually, since the ApplicationContext (and this same mock instance) is
      // cached and reused across every test method in this class.
      reset(canvasOAuth2AuthorizedClientRepository);
      // Defaults to "resolvable" so every test below - all built from fully-populated
      // OidcAuthenticationTokens - is unaffected by the Canvas OAuth2 consent fail-fast check.
      when(canvasOAuth2AuthorizedClientRepository.hasResolvableCanvasUserId(any())).thenReturn(true);
   }

   @MockitoBean
   private FeatureAccessServiceImpl featureAccessService;

   @MockitoBean
   private WizardService wizardService = null;

   @MockitoBean
   private CanvasService canvasService = null;

   @MockitoBean
   private CourseSessionService courseSessionService;

   @MockitoBean
   private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

   @MockitoBean
   private ClientRegistrationRepository clientRegistrationRepository;

   @MockitoBean(name = ServerInfo.BEAN_NAME)
   private ServerInfo serverInfo;

   @BeforeEach
   public void setup() {
//      Model status = new Model(true, true, "1234");
//      when(courseUnlockerService.getCourseUnlockStatus(COURSE_ID_TST)).thenReturn(status);
   }

   @Test
   public void appNoAuthnLaunch() throws Exception {
      // This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/"  + COURSE_ID_TST + "/index")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void appAuthnWrongContextLaunch() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
            "asdf", LTIConstants.INSTRUCTOR_AUTHORITY);

      ServletException t = Assertions.assertThrows(ServletException.class, () ->
              mvc.perform(get("/app/" + COURSE_ID_TST + "/index")
                      .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                      .contentType(MediaType.APPLICATION_JSON)
                      .with(authentication(token)))
      );

      Assertions.assertInstanceOf(InvalidTokenContextException.class, t.getCause());
      Assertions.assertEquals("Context in authentication token does not match request context", t.getCause().getMessage());

   }

   @Test
   public void appAuthnLaunch() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
              COURSE_ID_TST, LTIConstants.INSTRUCTOR_AUTHORITY);

      // This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/app/"  + COURSE_ID_TST + "/index")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON)
              .with(authentication(token)))
              .andExpect(status().isOk());
   }

   @Test
   public void appAuthnLaunchNonInstructor() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
              COURSE_ID_TST, LTIConstants.TA_AUTHORITY);

      // This is a secured endpoint and should not allow access without instructor authn
      mvc.perform(get("/app/"  + COURSE_ID_TST + "/index")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON)
              .with(authentication(token)))
              .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlNoAuth() throws Exception {
      // This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlWithAuth() throws Exception {
      OidcAuthenticationToken token = TestUtils.buildToken("userId",
              COURSE_ID_TST, LTIConstants.INSTRUCTOR_AUTHORITY);
      // This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON)
            .with(authentication(token)))
            .andExpect(status().isNotFound());
   }
}
