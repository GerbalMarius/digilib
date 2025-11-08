package org.digilib.library.configs;

import lombok.NonNull;
import org.digilib.library.LibraryApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", LibraryApplication.BACK_URL)
                .allowedHeaders("Content-Type", "Authorization")
                .allowedMethods("GET", "POST", "POST" , "PUT", "PATCH", "DELETE")
                .allowCredentials(true);
    }
}
