package edu.iu.uits.lms.coursesetupwizard.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

import static edu.iu.uits.lms.coursesetupwizard.Constants.COURSE_TEMPLATES_CACHE_NAME;
import static edu.iu.uits.lms.coursesetupwizard.Constants.INSTRUCTOR_COURSES_CACHE_NAME;

@Profile("redis-cache")
@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig {

    @Autowired
    private ToolConfig toolConfig;

    @Autowired
    private JedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisCacheConfiguration cswCacheConfiguration() {
        final int ttl = 3600;
        return RedisCacheConfiguration.defaultCacheConfig()
              .entryTtl(Duration.ofSeconds(ttl))
              .disableCachingNullValues()
              .prefixCacheNameWith(toolConfig.getEnv() + "-csw");
    }

    @Bean(name = "CourseSetupWizardCacheManager")
    @Primary
    public CacheManager cacheManager() {
        log.debug("cacheManager()");
        log.debug("Redis hostname: {}", redisConnectionFactory.getHostName());

        return RedisCacheManager.builder(redisConnectionFactory)
              .withCacheConfiguration(INSTRUCTOR_COURSES_CACHE_NAME, cswCacheConfiguration())
              .withCacheConfiguration(COURSE_TEMPLATES_CACHE_NAME, cswCacheConfiguration())
              .build();
    }
}
