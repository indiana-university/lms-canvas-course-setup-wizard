package edu.iu.uits.lms.coursesetupwizard.amqp;

import edu.iu.uits.lms.coursesetupwizard.model.ImportModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WizardImportMessage implements Serializable {
   private ImportModel importModel;

}
