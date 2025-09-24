package edu.iu.uits.lms.coursesetupwizard.services;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2023 Indiana University
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

import edu.iu.uits.lms.common.test.CommonTestUtils;
import edu.iu.uits.lms.coursesetupwizard.Constants;
import edu.iu.uits.lms.coursesetupwizard.config.SecurityConfig;
import edu.iu.uits.lms.coursesetupwizard.config.ToolConfig;
import edu.iu.uits.lms.coursesetupwizard.controller.rest.BannerImageJpaCustomRestController;
import edu.iu.uits.lms.coursesetupwizard.controller.rest.PopupRestController;
import edu.iu.uits.lms.coursesetupwizard.controller.rest.WizardCourseStatusRestController;
import edu.iu.uits.lms.coursesetupwizard.model.PopupStatus;
import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = {"oauth.tokenprovider.url=http://foo", "lms.swagger.cors.origin=asdf", "lms.js.cors.origin=http://www.someurl.com"})
@ContextConfiguration(classes = {PopupRestController.class, WizardCourseStatusRestController.class, SecurityConfig.class})
@Slf4j
@ActiveProfiles("swagger")
public class CorsTest {

   @Autowired
   private MockMvc mvc;

   @MockitoBean
   private BannerImageJpaCustomRestController bannerImageJpaCustomRestController;

   @MockitoBean
   WizardService wizardService = null;

   @MockitoBean
   private WizardCourseStatusRepository wizardCourseStatusRepository = null;

   @MockitoBean
   private WizardUserCourseRepository wizardUserCourseRepository = null;

   @MockitoBean
   private PopupDismissalDateRepository popupDismissalDateRepository = null;

   @MockitoBean
   private BannerImageCategoryRepository bannerImageCategoryRepository;

   @MockitoBean
   private BannerImageRepository bannerImageRepository;

   @MockitoBean
   private ThemeContentRepository themeContentRepository;

   @MockitoBean
   private ThemeLogRepository themeLogRepository;

   @MockitoBean
   private ThemeRepository themeRepository;

   @MockitoBean
   private ThemeProcessingService themeProcessingService;

   @MockitoBean
   private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

   @MockitoBean
   private ClientRegistrationRepository clientRegistrationRepository;


   public static String COURSE_ID_TST = "1234";
   public static String USER_ID_TST = "qwerty";
   public static long WCS_ID = 1L;

   @BeforeEach
   public void setup() {
      PopupStatus status = new PopupStatus(COURSE_ID_TST, USER_ID_TST, true, null);
      when(wizardService.getPopupDismissedStatus(COURSE_ID_TST, USER_ID_TST)).thenReturn(status);

      WizardCourseStatus wcs = new WizardCourseStatus(WCS_ID, COURSE_ID_TST, USER_ID_TST,
            Constants.MAIN_OPTION.TEMPLATE, Constants.CONTENT_OPTION.ALL, Constants.DATE_OPTION.NOCHANGE,
            "7", "1234", new Date());
      when(wizardCourseStatusRepository.findById(WCS_ID)).thenReturn(Optional.of(wcs));
   }

   @Test
   public void restCheckCors() throws Exception {
      // This is not a secured endpoint so should be successful
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/popup/{course}/{user}/status", COURSE_ID_TST, USER_ID_TST)
                  .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                  .header(HttpHeaders.ORIGIN, "http://www.someurl.com")
                  .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.showPopup").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(USER_ID_TST))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseId").value(COURSE_ID_TST))
            .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://www.someurl.com"));
   }

   @Test
   public void restCheckCorsNoOrigin() throws Exception {
      //This is not a secured endpoint so should be successful
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/popup/{course}/{user}/status", COURSE_ID_TST, USER_ID_TST)
                  .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.showPopup").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(USER_ID_TST))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseId").value(COURSE_ID_TST))
            .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
   }

   @Test
   public void restCheckCorsOptions() throws Exception {
      //This is not a secured endpoint so should be successful
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(options("/rest/popup/{course}/{user}/status", COURSE_ID_TST, USER_ID_TST)
                  .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
   }

   @Test
   public void restCheckCors2() throws Exception {
      Jwt jwt = TestUtils.createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      mvc.perform(get("/rest/coursestatus/{id}", WCS_ID)
                  .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                  .header(HttpHeaders.ORIGIN, "http://www.someurl.com")
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(authentication(token)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.completedBy").value(USER_ID_TST))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseId").value(COURSE_ID_TST))
            .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
   }

   @Test
   public void restCheckCors2NoOrigin() throws Exception {
      Jwt jwt = TestUtils.createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      mvc.perform(get("/rest/coursestatus/{id}", WCS_ID)
                  .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(authentication(token)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.completedBy").value(USER_ID_TST))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseId").value(COURSE_ID_TST))
            .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
   }

   @Test
   public void restCheckCors2Options() throws Exception {
      Jwt jwt = TestUtils.createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      mvc.perform(options("/rest/coursestatus/{id}", WCS_ID)
                  .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                  .header(HttpHeaders.ORIGIN, "http://www.someurl.com")
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(authentication(token)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
   }

}