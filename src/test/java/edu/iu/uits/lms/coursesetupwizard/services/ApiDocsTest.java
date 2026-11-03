package edu.iu.uits.lms.coursesetupwizard.services;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2026 Indiana University
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import uk.ac.ox.ctl.lti13.ags.DeepLinkService;
import uk.ac.ox.ctl.lti13.ags.LineItemService;
import uk.ac.ox.ctl.lti13.ags.ResultService;
import uk.ac.ox.ctl.lti13.ags.ScoreService;
import uk.ac.ox.ctl.lti13.nrps.NamesRoleService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * As of springdoc-openapi 3.0.0-3.1.0, hitting /api/v3/api-docs on a tool with
 * @RepositoryRestResource repos can throw a NullPointerException in
 * SpringDocDataRestUtils and 500 the whole swagger doc. {@link edu.iu.uits.lms.coursesetupwizard.config.JpaRestConfig}
 * defensively works around this (catch-and-log instead of letting it propagate).
 * See springdoc/springdoc-openapi#3337, #3328.
 *
 * Verified by temporarily removing the workaround bean and re-running this test: for
 * this tool's current springdoc-openapi version and {@code springdoc.packagesToScan}
 * restriction (scoped to this tool's own controller.rest package), the NPE does not
 * actually reproduce here, and the Spring Data REST auto-CRUD paths (e.g.
 * /rest/bannerimage) are never added to api-docs either way - only this tool's own
 * hand-written REST controllers are documented. The workaround bean is still kept as
 * cheap, harmless insurance per the quarterly-update process for any tool with
 * @RepositoryRestResource repos. This test instead guards the thing that IS true and
 * verifiable for this tool: /api/v3/api-docs must keep returning 200 with a valid
 * OpenAPI document, not a 500, whether or not that bug ever starts firing here.
 */
@SpringBootTest(classes = {WebApplication.class},
        properties = {"oauth.tokenprovider.url=http://foo", "lms.rabbitmq.queue_env_suffix=CI",
                "canvas.host=asdf", "canvas.token=asdf", "lti.errorcontact.name=asdf", "lti.errorcontact.link=asdf",
                "catalog.token=asdf", "spring.rabbitmq.listener.simple.auto-startup=false", "lms.swagger.cors.origin=123",
                "lms.js.cors.origin=123",
                // canvas.oauth2.encryptionPassword/encryptionSalt are required the moment
                // @EnableCanvasOAuth2Client is active (CanvasOAuth2AuthorizedClientRepository's
                // constructor needs them to build its token-encryption cipher) - see
                // docs/oauth/COURSE_SETUP_WIZARD_CANVAS_OAUTH2_EVALUATION.md and courselist's own
                // WebApplicationTests.java, which hit this first.
                "canvas.oauth2.encryptionPassword=test-only-placeholder-password",
                "canvas.oauth2.encryptionSalt=deadbeef"})
@AutoConfigureMockMvc
@ActiveProfiles({"csw", "swagger"})
@AutoConfigureTestDatabase
public class ApiDocsTest {

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

    // LtiClientConfig eagerly builds real LineItemService/ScoreService/ResultService/DeepLinkService
    // beans that call Lti13Service.getJKS(), which needs a real signing keypair from the
    // KeyPairRepository table - not present in the test H2 database. Mock them out so context load
    // doesn't try to construct the real ones. (Confirmed still present in lti-framework at the
    // 7.0.7-SNAPSHOT pin used for the Canvas OAuth2 upgrade - this is not version-specific.)
    @MockitoBean
    private LineItemService lineItemService;

    @MockitoBean
    private ScoreService scoreService;

    @MockitoBean
    private ResultService resultService;

    @MockitoBean
    private DeepLinkService deepLinkService;

    @MockitoBean
    private CorsSwaggerConfig corsSwaggerConfig;

    @Test
    public void apiDocsReturnsOk() throws Exception {
        mvc.perform(get("/api/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").exists())
                // A handful of this tool's own hand-written REST endpoints, to confirm
                // the doc is actually fully populated and not an empty/partial shell.
                .andExpect(jsonPath("$.paths['/rest/coursestatus/all']").exists())
                .andExpect(jsonPath("$.paths['/rest/popupDates/all']").exists())
                .andExpect(jsonPath("$.paths['/rest/wuc/all']").exists());
    }
}
