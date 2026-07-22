package com.woowapractice.common.presentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI woowaPracticeOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Woowa Practice API")
                .version("v1")
                .description("우아한테크코스 프리코스 연습 플랫폼 API"));
  }
}
