package edu.iu.uits.lms.coursesetupwizard.config;

import edu.iu.uits.lms.coursesetupwizard.model.BannerImage;
import edu.iu.uits.lms.coursesetupwizard.model.BannerImageCategory;
import edu.iu.uits.lms.coursesetupwizard.model.Theme;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeContent;
import edu.iu.uits.lms.coursesetupwizard.model.ThemeLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
@Slf4j
public class CustomRepositoryRestConfigurer implements RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry corsRegistry) {
        config.getExposureConfiguration()
                .forDomainType(BannerImageCategory.class)
                .withItemExposure((metadata, httpMethods) -> httpMethods.disable(HttpMethod.PATCH));

        config.getExposureConfiguration()
                .forDomainType(BannerImage.class)
                .withItemExposure((metadata, httpMethods) -> httpMethods.disable(HttpMethod.PATCH));

        config.getExposureConfiguration()
                .forDomainType(ThemeContent.class)
                .withItemExposure((metadata, httpMethods) -> httpMethods.disable(HttpMethod.PATCH));

        config.getExposureConfiguration()
                .forDomainType(ThemeLog.class)
                .withItemExposure((metadata, httpMethods) -> httpMethods.disable(HttpMethod.PATCH));

        config.getExposureConfiguration()
                .forDomainType(Theme.class)
                .withItemExposure((metadata, httpMethods) -> httpMethods.disable(HttpMethod.PATCH));
    }
}