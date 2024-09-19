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

import edu.iu.uits.lms.coursesetupwizard.config.SecurityConfig;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.controller.rest.BannerImageJpaCustomRestController;
import edu.iu.uits.lms.coursesetupwizard.controller.rest.PopupRestController;
import edu.iu.uits.lms.coursesetupwizard.controller.rest.WizardCourseStatusRestController;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageCategoryRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.BannerImageRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.PopupDismissalDateRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeContentRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeLogRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.ThemeRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardCourseStatusRepository;
import edu.iu.uits.lms.coursesetupwizard.repository.WizardUserCourseRepository;
import edu.iu.uits.lms.coursesetupwizard.service.ThemeProcessingService;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import edu.iu.uits.lms.lti.config.TestUtils;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = {"oauth.tokenprovider.url=http://foo"})
@ContextConfiguration(classes = {ToolConfig.class, PopupRestController.class, WizardCourseStatusRestController.class, SecurityConfig.class})
@Slf4j
@ActiveProfiles("none")
public class RestLaunchSecurityTest {

   @Autowired
   private MockMvc mvc;

   @MockBean
   private BannerImageJpaCustomRestController bannerImageJpaCustomRestController;

   @MockBean
   private WizardService wizardService;

   @MockBean
   private WizardCourseStatusRepository wizardCourseStatusRepository;

   @MockBean
   private WizardUserCourseRepository wizardUserCourseRepository;

   @MockBean
   private PopupDismissalDateRepository popupDismissalDateRepository = null;

   @MockBean
   private BannerImageCategoryRepository bannerImageCategoryRepository;

   @MockBean
   private BannerImageRepository bannerImageRepository;

   @MockBean
   private ThemeContentRepository themeContentRepository;

   @MockBean
   private ThemeLogRepository themeLogRepository;

   @MockBean
   private ThemeRepository themeRepository;

   @MockBean
   private ThemeProcessingService themeProcessingService;

   @MockBean
   private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

   @MockBean
   private ClientRegistrationRepository clientRegistrationRepository;

   @Test
   public void restNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not allow access without authn
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/coursestatus/all")
                  .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
      //TODO - Is it okay that this is a 401 instead of a 403?
   }

   @Test
   public void restAuthnLaunch() throws Exception {
      Jwt jwt = TestUtils.createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/rest/coursestatus/all")
                  .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(authentication(token)))
            .andExpect(status().isOk());
   }

   @Test
   public void restAuthnLaunchWithWrongScope() throws Exception {
      Jwt jwt = TestUtils.createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read", "ROLE_NONE_YA");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      //This is a secured endpoint and should not allow access without authn
      mvc.perform(get("/rest/coursestatus/all")
                  .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(authentication(token)))
            .andExpect(status().isForbidden());
   }

   @Test
   public void restNoAuthnOpenEndpoint() throws Exception {
      //This is an unsecured endpoint and should allow access without authn
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/popup/1234/asdf/status")
                  .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
   }

   @Test
   public void restNoAuthnOpenEndpointPost() throws Exception {
      //This is an unsecured endpoint and should allow access without authn
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(post("/rest/popup/1234/asdf/dismiss")
                  .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
   }

}
