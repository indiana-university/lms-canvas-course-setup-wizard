package edu.iu.uits.lms.coursesetupwizard.model;

import edu.iu.uits.lms.canvas.model.Course;
import lombok.Data;

import java.io.Serializable;
import java.text.MessageFormat;

@Data
public class SelectableCourse implements Serializable {

   private String label;
   private String value;

   public SelectableCourse (Course course) {
      this.value = course.getId();
      this.label = course.getName();

      if (course.getSisCourseId() != null) {
         this.label = MessageFormat.format("{0} ({1})", course.getName(), course.getSisCourseId());
      }
   }
}
