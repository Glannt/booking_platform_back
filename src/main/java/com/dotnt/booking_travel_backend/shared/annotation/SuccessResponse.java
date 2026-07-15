package com.dotnt.booking_travel_backend.shared.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SuccessResponse {

    @AliasFor("value")
    String message() default "Operation successful";

    @AliasFor("message")
    String value() default "Operation successful";

    int status() default 200;
}
