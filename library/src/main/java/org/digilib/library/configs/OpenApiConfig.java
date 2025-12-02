package org.digilib.library.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Digilib API",
        version = "1.0",
        description = "API for managing library books",
        summary = "Digilib API"
))
public class OpenApiConfig{
}
