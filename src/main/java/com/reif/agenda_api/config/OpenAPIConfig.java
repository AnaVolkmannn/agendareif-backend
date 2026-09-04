package com.reif.agenda_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI reifBeautyStudioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reif Beauty Studio API")
                        .description("API de agendamento de horários do Reif Beauty Studio")
                        .version("v1"));
    }
}