package org.digilib.library.configs.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        String query = Objects.isNull(request.getQueryString()) ? "" : "?" + request.getQueryString();

        HttpErrorResponse responseBody = HttpErrorResponse.of(
                HttpStatus.FORBIDDEN,
                "Insufficient permissions to perform this action",
                request.getRequestURL().toString() + query
                );

        String json = objectMapper.writeValueAsString(responseBody);

        response.getWriter().write(json);
    }
}