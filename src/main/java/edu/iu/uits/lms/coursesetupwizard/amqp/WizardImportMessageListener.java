package edu.iu.uits.lms.coursesetupwizard.amqp;

import com.rabbitmq.client.Channel;
import edu.iu.uits.lms.coursesetupwizard.service.WizardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "${course-setup-wizard.wizardImportQueueName}")
@Component
@Profile("!batch")
@Slf4j
public class WizardImportMessageListener {

   @Autowired
   private WizardService wizardService;

   @RabbitHandler
   public void receive(WizardImportMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
      log.info("Received <{}>", message);

      try {
         // ack the message
         channel.basicAck(deliveryTag, false);

         wizardService.doCourseImport(message.getImportModel());
      } catch (IOException e) {
         log.error("Error performing course import", e);
      }
   }

}
