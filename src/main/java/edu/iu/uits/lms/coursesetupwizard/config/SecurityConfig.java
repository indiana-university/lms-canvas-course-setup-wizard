package edu.iu.uits.lms.coursesetupwizard.config;

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

import edu.iu.uits.lms.common.it12logging.LmsFilterSecurityInterceptorObjectPostProcessor;
import edu.iu.uits.lms.common.it12logging.RestSecurityLoggingConfig;
import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import edu.iu.uits.lms.iuonly.services.DeptProvisioningUserServiceImpl;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import uk.ac.ox.ctl.lti13.Lti13Configurer;

import static edu.iu.uits.lms.lti.LTIConstants.INSTRUCTOR_AUTHORITY;
import static edu.iu.uits.lms.lti.LTIConstants.INSTRUCTOR_ROLE;
import static edu.iu.uits.lms.lti.LTIConstants.WELL_KNOWN_ALL;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Configuration
//    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4)
//    public static class RestSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
//
//        @Override
//        public void configure(HttpSecurity http) throws Exception {
//            http
//                  .cors().and()
//                    .requestMatchers().antMatchers("/api/**", "/rest/**")
//                    .and()
//                    .authorizeRequests()
//                    .antMatchers("/api/**").permitAll()
//                    .antMatchers("/rest/popup/**").permitAll()
////                    .access("hasAuthority('SCOPE_lms:edsdev') or (hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS'))")
//                    .antMatchers("/rest/**").access("hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS')")
//                    .and()
//                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                    .and()
//                    .oauth2ResourceServer()
//                    .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());
//
//            http.apply(new RestSecurityLoggingConfig());
//
//            //Disable csrf for the popup endpoints
//            http.csrf().ignoringAntMatchers("/rest/popup/**");
//        }
//    }

    @Order(4)
    @Bean
    public SecurityFilterChain restFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .securityMatcher("/rest/**", "/api/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/rest/popup/**").permitAll()
//                        .access(new WebExpressionAuthorizationManager("hasAuthority('SCOPE_lms:edsdev') or (hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS'))"))
                        .requestMatchers("/rest/**")
                        .access(new WebExpressionAuthorizationManager("hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS')"))
                        .requestMatchers("/api/**").permitAll()
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt ->
                                jwt.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())))
                .with(new RestSecurityLoggingConfig(), log -> {});

        // Disable csrf for the popup endpoints
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/rest/popup/**")
        );

        return http.build();
    }

//
//    @Configuration
//    @Order(SecurityProperties.BASIC_AUTH_ORDER - 3)
//    public static class AppSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
//
//        @Autowired
//        private DefaultInstructorRoleRepository defaultInstructorRoleRepository;
//
//        @Override
//        protected void configure(HttpSecurity http) throws Exception {
//            http
//                  .requestMatchers()
//                  .antMatchers(WELL_KNOWN_ALL, "/error", "/app/**", "/tool/**")
//                  .and()
//                  .authorizeRequests()
//                  .antMatchers(WELL_KNOWN_ALL, "/error").permitAll()
//                  .antMatchers("/app/**", "/tool/**").hasRole(LTIConstants.INSTRUCTOR_ROLE)
//                  .withObjectPostProcessor(new LmsFilterSecurityInterceptorObjectPostProcessor())
//                  .and()
//                  .headers()
//                  .contentSecurityPolicy("style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'self' https://*.instructure.com")
//                  .and()
//                  .referrerPolicy(referrer -> referrer
//                          .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN));
//
//            //Setup the LTI handshake
//            Lti13Configurer lti13Configurer = new Lti13Configurer()
//                    .grantedAuthoritiesMapper(new CustomRoleMapper(defaultInstructorRoleRepository));
//
//            http.apply(lti13Configurer);
//
//            //Fallback for everything else
//            http.requestMatchers().antMatchers("/**")
//                    .and()
//                    .authorizeRequests()
//                    .anyRequest().authenticated()
//                    .withObjectPostProcessor(new LmsFilterSecurityInterceptorObjectPostProcessor())
//                    .and()
//                    .headers()
//                    .contentSecurityPolicy("style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'self' https://*.instructure.com")
//                    .and()
//                    .referrerPolicy(referrer -> referrer
//                            .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN));
//        }
//
//        @Override
//        public void configure(WebSecurity web) throws Exception {
//            // ignore everything except paths specified
//            web.ignoring().antMatchers("/app/jsrivet/**", "/app/jsreact/**", "/app/webjars/**",
//                  "/app/css/**", "/app/js/**", "/app/images/**", "/favicon.ico");
//        }

    @Order(6)
    @Bean
    public SecurityFilterChain appFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(WELL_KNOWN_ALL, "/error", "/app/**", "/tool/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(WELL_KNOWN_ALL, "/error").permitAll()
                        .requestMatchers("/app/**", "/tool/**").hasAuthority(INSTRUCTOR_AUTHORITY)
                        .withObjectPostProcessor(new LmsFilterSecurityInterceptorObjectPostProcessor())
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'self' https://*.instructure.com"))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/app/jsrivet/**", "/app/jsreact/**", "/app/webjars/**",
                "/app/css/**", "/app/js/**", "/app/images/**", "/favicon.ico");
    }

    @Autowired
    private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

    @Bean
    public SecurityFilterChain catchallFilterChain(HttpSecurity http) throws Exception {
        //Setup the LTI handshake
        http.with(new Lti13Configurer(), lti ->
                lti.setSecurityContextRepository(new HttpSessionSecurityContextRepository())
                        .grantedAuthoritiesMapper(new CustomRoleMapper(defaultInstructorRoleRepository)));

        http.securityMatcher("/**")
                .authorizeHttpRequests((authz) -> authz.anyRequest().authenticated()
                        .withObjectPostProcessor(new LmsFilterSecurityInterceptorObjectPostProcessor()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'self' https://*.instructure.com"))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                );

        return http.build();
    }
}
