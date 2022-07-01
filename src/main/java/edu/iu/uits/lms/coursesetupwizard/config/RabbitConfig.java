package edu.iu.uits.lms.coursesetupwizard.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

   @Autowired
   private ToolConfig toolConfig = null;

   @Bean(name = "importQueue")
   Queue importQueue() {
      return new Queue(toolConfig.getWizardImportQueueName());
   }

}
