package com.bifrost.demo.annotation;

import com.bifrost.demo.dto.model.BifrostUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    BifrostUser.Role[] value();
}
