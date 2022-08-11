package edu.iu.uits.lms.coursesetupwizard.config;

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
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

        // let TAs use the tool
        if (userRoleList.contains(CanvasConstants.TA_ROLE)) {
            return LTIConstants.INSTRUCTOR_AUTHORITY;
        }

        // let Designers use the tool
        if (userRoleList.contains(CanvasConstants.DESIGNER_ROLE)) {
            return LTIConstants.INSTRUCTOR_AUTHORITY;
        }

        for (String instructorRole : instructorRoles) {
            if (userRoleList.contains(instructorRole)) {
                return LTIConstants.INSTRUCTOR_AUTHORITY;
            }
        }

        return LTIConstants.STUDENT_AUTHORITY;
    }
}
