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

import edu.iu.uits.lms.common.cors.CorsSwaggerConfig;
import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.coursesetupwizard.WebApplication;
import edu.iu.uits.lms.lti.config.TestUtils;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ox.ctl.lti13.nrps.NamesRoleService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {WebApplication.class},
        properties = {"oauth.tokenprovider.url=http://foo", "lms.rabbitmq.queue_env_suffix=CI",
                "canvas.host=asdf", "canvas.token=asdf", "lti.errorcontact.name=asdf", "lti.errorcontact.link=asdf",
                "catalog.token=asdf", "spring.rabbitmq.listener.simple.auto-startup=false", "lms.swagger.cors.origin=123",
                "lms.js.cors.origin=123"})
@AutoConfigureMockMvc
@ActiveProfiles({"csw", "it12"})
@EnableAutoConfiguration(exclude = {HealthContributorAutoConfiguration.class, HealthEndpointAutoConfiguration.class,
        MailHealthContributorAutoConfiguration.class})
@AutoConfigureTestDatabase
@Slf4j
public class It12LoggingTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CourseSessionService courseSessionService;

    @MockitoBean
    private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private BufferingApplicationStartup bufferingApplicationStartup;

    @MockitoBean
    private NamesRoleService namesRoleService;

    @MockitoBean
    private CorsSwaggerConfig corsSwaggerConfig;

    @Test
    public void testLmsEnhancementToIt12LogExistence() throws Exception {
        final String auditLoggerClassName = "edu.iu.es.esi.audit.AuditLogger";

        Class<?> clazz = null;

        try {
            clazz = Class.forName(auditLoggerClassName);
        } catch (ClassNotFoundException classNotFoundException) {
            log.info("Skipping test because AuditLogger not found");
        }

        Assumptions.assumeTrue(clazz != null);

        try (LogCaptor logCaptor = LogCaptor.forClass(clazz)) {
            final Jwt jwt = createJwtToken("asdf");

            final Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
            final JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

            final String uriToCall = "/rest/coursestatus/all";

            mvc.perform(get(uriToCall)
                            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(authentication(token)))
                    .andExpect(status().isOk());

            final List<String> it12LogEntries = logCaptor.getInfoLogs();

            Assertions.assertNotNull(it12LogEntries);
            Assertions.assertEquals(2, it12LogEntries.size());

            // Find the LMS custom log entry and the base IT12 log entry, just in case the order can't be guaranteed
            String lmsIt12LogEntry = null;
            String baseIt12LogEntry = null;

            for (String logEntry : it12LogEntries) {
                if (logEntry.contains("Successful authorization to uri")) {
                    lmsIt12LogEntry = logEntry;
                } else if (logEntry.contains("Successful access to")) {
                    baseIt12LogEntry = logEntry;
                }
            }

            // Verify LMS custom log entry
            Assertions.assertNotNull(lmsIt12LogEntry, "LMS custom log entry should be present");
            Assertions.assertFalse(lmsIt12LogEntry.isEmpty());
            Assertions.assertTrue(lmsIt12LogEntry.contains("\"type\":\"successful authorization\""));
            Assertions.assertTrue(lmsIt12LogEntry.contains("\"user\":\"asdf\""));
            Assertions.assertTrue(lmsIt12LogEntry.contains("\"ipAddress\":\"127.0.0.1\""));
            Assertions.assertTrue(lmsIt12LogEntry.contains("\"message\":\"Successful authorization to uri " + uriToCall +
                    " as asdf with clientId asdf with audience [aud1, aud2] and authorities [LMS_REST_ADMINS] and scopes [lms:rest]\""));

            // Verify base IT12 log entry
            Assertions.assertNotNull(baseIt12LogEntry, "Base IT12 log entry should be present");
            Assertions.assertFalse(baseIt12LogEntry.isEmpty());
            Assertions.assertTrue(baseIt12LogEntry.contains("\"type\":\"successful authorization\""));
            Assertions.assertTrue(baseIt12LogEntry.contains("\"user\":\"asdf\""));
            Assertions.assertTrue(baseIt12LogEntry.contains("\"ipAddress\":\"127.0.0.1\""));
            Assertions.assertTrue(baseIt12LogEntry.contains("\"message\":\"Successful access to " + uriToCall + "\""));
        }
    }

    public static Jwt createJwtToken(String client) {
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("typ", "JWT")
                .claim("user_name", client)
                .claim("client_id", client)
                .audience(List.of("aud1", "aud2"))
                .notBefore(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .subject(client)
                .build();

        return jwt;
    }
}
