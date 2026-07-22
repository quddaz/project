package com.woowapractice.common.presentation;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI woowaPracticeOpenApi() {
    Components components = new Components();
    ModelConverters.getInstance().readAll(ApiErrorResponse.class).forEach(components::addSchemas);
    components
        .addResponses("BadRequest", createErrorResponse("잘못된 요청"))
        .addResponses("NotFound", createErrorResponse("요청한 리소스를 찾을 수 없음"))
        .addResponses("MethodNotAllowed", createErrorResponse("지원하지 않는 HTTP 메서드"))
        .addResponses("InternalServerError", createErrorResponse("예기치 않은 서버 오류"));

    return new OpenAPI()
        .components(components)
        .info(
            new Info()
                .title("Woowa Practice API")
                .version("v1")
                .description("우아한테크코스 프리코스 연습 플랫폼 API"));
  }

  private ApiResponse createErrorResponse(String description) {
    Schema<Object> schema = new Schema<>();
    schema.set$ref("#/components/schemas/ApiErrorResponse");
    MediaType mediaType = new MediaType().schema(schema);
    Content content = new Content().addMediaType("application/json", mediaType);
    return new ApiResponse().description(description).content(content);
  }
}
