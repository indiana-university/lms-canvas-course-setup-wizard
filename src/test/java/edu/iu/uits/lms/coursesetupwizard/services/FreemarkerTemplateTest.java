package edu.iu.uits.lms.coursesetupwizard.services;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@Slf4j
public class FreemarkerTemplateTest {

   @Autowired
   private FreeMarkerConfigurer freeMarkerConfigurer;

   @Test
   public void testTemplate() throws Exception {

      Map<String, Object> templateModel = new HashMap<>();
      templateModel.put("showIt", true);

      String template1 = """
                <ul>
                    <li>Item 1</li>
                    <#if showIt><li>Conditional Item 2</li></#if>
                    <li>Item 3</li>
                </ul>
                """;

      String template1Result1 = """
                <ul>
                    <li>Item 1</li>
                    <li>Conditional Item 2</li>
                    <li>Item 3</li>
                </ul>
                """;

      String template1Result2 = """
                <ul>
                    <li>Item 1</li>
                    
                    <li>Item 3</li>
                </ul>
                """;



      try {
         StringTemplateLoader dbTemplateLoader = new StringTemplateLoader();
         dbTemplateLoader.putTemplate("template1", template1);
         dbTemplateLoader.putTemplate("template2", "");

         Configuration configuration = freeMarkerConfigurer.createConfiguration();
         configuration.setTemplateLoader(dbTemplateLoader);


         Template freemarkerTemplate = configuration.getTemplate("template1");

         String body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, templateModel);
         Assertions.assertEquals(template1Result1, body);


         templateModel.put("showIt", false);
         body = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, templateModel);
         Assertions.assertEquals(template1Result2, body);

      } catch (IOException | TemplateException e) {
         throw new RuntimeException(e);
      }

   }

   @TestConfiguration
   static class FreemarkerTemplateTestContextConfiguration {

      @Bean
      public FreeMarkerConfigurer freeMarkerConfigurer() {
         FreeMarkerConfigurer fmc = new FreeMarkerConfigurer();
         fmc.setTemplateLoaderPath("classpath:/templates");
         return fmc;
      }
   }
}
