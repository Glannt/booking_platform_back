package com.dotnt.booking_travel_backend.shared.advice;

import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import com.dotnt.booking_travel_backend.shared.dto.ApiResponse;
import com.dotnt.booking_travel_backend.shared.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String packageName = returnType.getContainingClass().getPackageName();
        if (packageName.startsWith("org.springdoc") || packageName.startsWith("org.springframework")) {
            return false;
        }
        // Wrap responses for all RestControllers, or if SuccessResponse annotation is present
        return returnType.getContainingClass().isAnnotationPresent(RestController.class)
                || returnType.hasMethodAnnotation(SuccessResponse.class)
                || returnType.getContainingClass().isAnnotationPresent(SuccessResponse.class);
    }

    @Override
    @SneakyThrows
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // If the body is already an ApiResponse or ErrorResponse, do not wrap it
        if (body instanceof ApiResponse || body instanceof ErrorResponse) {
            return body;
        }

        // Get annotation details
        SuccessResponse successResponse = returnType.getMethodAnnotation(SuccessResponse.class);
        if (successResponse == null) {
            successResponse = AnnotationUtils.findAnnotation(returnType.getContainingClass(), SuccessResponse.class);
        }

        // Determine current HTTP status (e.g. from ResponseEntity if returned by Controller)
        int status = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse servletResponse) {
            status = servletResponse.getServletResponse().getStatus();
        }

        String message = "Operation successful";

        if (successResponse != null) {
            // Determine custom message
            message = successResponse.message().equals("Operation successful") ? successResponse.value() : successResponse.message();
            
            // Override status only if successResponse has custom status or if servletResponse is default 200
            if (successResponse.status() != 200 || status == HttpStatus.OK.value()) {
                status = successResponse.status();
                if (response instanceof ServletServerHttpResponse servletResponse) {
                    servletResponse.getServletResponse().setStatus(status);
                }
            }
        }

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .statusCode(status)
                .message(message)
                .data(body)
                .build();

        // Special handling for String return type to avoid ClassCastException in StringHttpMessageConverter
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.writeValueAsString(apiResponse);
        }

        return apiResponse;
    }
}
