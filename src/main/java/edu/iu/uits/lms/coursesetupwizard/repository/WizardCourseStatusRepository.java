package edu.iu.uits.lms.coursesetupwizard.repository;

import edu.iu.uits.lms.coursesetupwizard.model.WizardCourseStatus;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface WizardCourseStatusRepository extends PagingAndSortingRepository<WizardCourseStatus, Long> {

   WizardCourseStatus findByCourseId(String courseId);
}
