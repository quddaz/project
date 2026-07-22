package com.woowapractice.grading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@AutoConfigurationPackage(basePackages = "com.woowapractice")
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = "com.woowapractice",
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = com.woowapractice.api.ApiApplication.class))
public class GradingWorkerApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(GradingWorkerApplication.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.setAdditionalProfiles("worker");
    application.run(args);
  }
}
