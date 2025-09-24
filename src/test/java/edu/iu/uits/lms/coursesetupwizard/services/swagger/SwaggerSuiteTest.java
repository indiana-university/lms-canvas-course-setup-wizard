package edu.iu.uits.lms.coursesetupwizard.services.swagger;

/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 - 2024 Indiana University
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

import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.swagger.AbstractSwaggerCustomTest;
import edu.iu.uits.lms.lti.swagger.AbstractSwaggerDisabledTest;
import edu.iu.uits.lms.lti.swagger.AbstractSwaggerEmbeddedToolTest;
import edu.iu.uits.lms.lti.swagger.AbstractSwaggerUiCustomTest;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static edu.iu.uits.lms.email.EmailConstants.EMAILREST_PROFILE;
import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;
import static org.springframework.test.context.NestedTestConfiguration.EnclosingConfiguration.INHERIT;

@NestedTestConfiguration(INHERIT)
public class SwaggerSuiteTest {
    @MockitoBean
    private BufferingApplicationStartup bufferingApplicationStartup;

    @MockitoBean
    private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Nested
    @SpringBootTest(classes = {WizardSwaggerTestConfig.class})
    public class SwaggerCustomTest extends AbstractSwaggerCustomTest {

    }

    @Nested
    @SpringBootTest(classes = {WizardSwaggerTestConfig.class})
    public class SwaggerDisabledTest extends AbstractSwaggerDisabledTest {

    }

    @Nested
    @SpringBootTest(classes = {WizardSwaggerTestConfig.class})
    @ActiveProfiles({IUCUSTOMREST_PROFILE, EMAILREST_PROFILE})
    public class SwaggerEmbeddedToolTest extends AbstractSwaggerEmbeddedToolTest {

    }

    @Nested
    @SpringBootTest(classes = {WizardSwaggerTestConfig.class})
    public class SwaggerUiCustomTest extends AbstractSwaggerUiCustomTest {

    }
}
