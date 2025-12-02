package org.digilib.library.configs;

import jakarta.servlet.Filter;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

    private static final MediaType UTF_8_JSON = new MediaType("application", "json", StandardCharsets.UTF_8);
    private static final MediaType OPEN_API_YAML = new MediaType("application", "vnd.oai.openapi", StandardCharsets.UTF_8);

    @Override
    public void configureContentNegotiation(@NonNull ContentNegotiationConfigurer config) {
        config.favorParameter(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(UTF_8_JSON)
                .mediaTypes(Map.of(
                        "json", UTF_8_JSON,
                        "openapi", OPEN_API_YAML
                ))
        ;
    }

    @Bean
    public Filter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}
