package edu.iu.uits.lms.coursesetupwizard.repository;

import edu.iu.uits.lms.coursesetupwizard.model.WizardUserCourse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface WizardUserCourseRepository extends PagingAndSortingRepository<WizardUserCourse, Long> {

   @Query("SELECT wuc FROM WizardUserCourse wuc WHERE wuc.username = :username and (wuc.courseId = 'GLOBAL' or wuc.courseId = :courseId)")
   List<WizardUserCourse> findByUsernameAndCourseIdOrGlobal(@Param("username") String username, @Param("courseId") String courseId);

   @Query("SELECT wuc FROM WizardUserCourse wuc WHERE wuc.username = :username and wuc.courseId = :courseId")
   WizardUserCourse findByUsernameAndCourseId(@Param("username") String username, @Param("courseId") String courseId);

   List<WizardUserCourse> findByUsername(String username);
}
