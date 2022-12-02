package com.example.annotation;

import com.example.annotation.impl.ImageFileValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = {ImageFileValidator.class})
public @interface ValidImage {

    String message() default "Invalid image file";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
