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

import edu.iu.uits.lms.canvas.helpers.EnrollmentHelper;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.service.LmsDefaultGrantedAuthoritiesMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class CustomRoleMapper extends LmsDefaultGrantedAuthoritiesMapper {

    public CustomRoleMapper(DefaultInstructorRoleRepository defaultInstructorRoleRepository) {
        super(defaultInstructorRoleRepository);
    }

    @Override
    protected String returnEquivalentAuthority(String[] userRoles, List<String> instructorRoles) {
        List<String> userRoleList = Arrays.asList(userRoles);

        for (String instructorRole : instructorRoles) {
            if (userRoleList.contains(instructorRole)) {
                return LTIConstants.INSTRUCTOR_AUTHORITY;
            }
        }

        // let TAs use the tool
        if (userRoleList.contains(EnrollmentHelper.TYPE_TA)) {
            return LTIConstants.INSTRUCTOR_AUTHORITY;
        }

        // let Designers use the tool
        if (userRoleList.contains(EnrollmentHelper.TYPE_DESIGNER)) {
            return LTIConstants.INSTRUCTOR_AUTHORITY;
        }

        return LTIConstants.STUDENT_AUTHORITY;
    }
}
