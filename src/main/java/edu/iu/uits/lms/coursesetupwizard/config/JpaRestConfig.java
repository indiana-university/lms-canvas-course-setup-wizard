package edu.iu.uits.lms.coursesetupwizard.config;

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

import edu.iu.uits.lms.coursesetupwizard.model.BannerImage;
import edu.iu.uits.lms.coursesetupwizard.model.BannerImageCategory;
import edu.iu.uits.lms.coursesetupwizard.model.Theme;
import edu.iu.uits.lms.common.swagger.LmsRepositoryDetectionStrategy;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.utils.SpringDocDataRestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
@Slf4j
public class JpaRestConfig implements RepositoryRestConfigurer {
    @Value("${lms.swagger.cors.origin}")
    private String corsSwaggerAllowedOrigin;

    @Autowired
    private SpringDocConfigProperties springDocConfigProperties;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        //  This is needed to allow the "ids" to be served up via the
        //  @RepositoryRestResource annotation (by default, it is suppressed)
        config.exposeIdsFor(BannerImage.class, BannerImageCategory.class, Theme.class);

        RepositoryRestConfigurer.super.configureRepositoryRestConfiguration(config, cors);
        config.setRepositoryDetectionStrategy(new LmsRepositoryDetectionStrategy(springDocConfigProperties.getPackagesToScan()));

        // These are needed for swagger to be able to call these JPA REST endpoints via its webpage
        cors.addMapping("/rest/bannerimage/**")
                .allowedOrigins(corsSwaggerAllowedOrigin)
                .allowedMethods("*");

        cors.addMapping("/rest/bannerimagecategory/**")
                .allowedOrigins(corsSwaggerAllowedOrigin)
                .allowedMethods("*");

        cors.addMapping("/rest/theme/**")
                .allowedOrigins(corsSwaggerAllowedOrigin)
                .allowedMethods("*");
    }

    /**
     * As of springdoc-openapi 3.0.0-3.1.0, hitting /api/v3/api-docs on a tool with
     * @RepositoryRestResource repos can throw a NullPointerException in
     * SpringDocDataRestUtils and 500 the whole swagger doc. Not fixed in any release as of
     * this writing (fix only on springdoc-openapi's unreleased main, PR #3329). This
     * workaround catches the NPE and continues without the Spring Data REST schema
     * enrichment rather than failing the whole api-docs endpoint.
     * See springdoc/springdoc-openapi#3337, #3328.
     */
    @Bean
    public SpringDocDataRestUtils springDocDataRestUtils(LinkRelationProvider linkRelationProvider,
                                                           RepositoryRestConfiguration repositoryRestConfiguration) {
        return new SpringDocDataRestUtils(linkRelationProvider, repositoryRestConfiguration) {
            @Override
            public void customise(OpenAPI openAPI, ResourceMappings mappings, PersistentEntities persistentEntities) {
                try {
                    super.customise(openAPI, mappings, persistentEntities);
                } catch (RuntimeException e) {
                    log.warn("springdoc Spring Data REST schema enrichment hit a known bug " +
                            "(springdoc/springdoc-openapi#3337), continuing without it: {}", e.toString());
                }
            }
        };
    }
}
