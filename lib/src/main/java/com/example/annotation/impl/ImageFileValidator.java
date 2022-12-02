package com.example.annotation.impl;

import com.example.annotation.ValidImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@Slf4j
public class ImageFileValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    @Override
    public void initialize(ValidImage constraintAnnotation) {
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        Objects.requireNonNull(multipartFile);

        var valid = true;
        var contentType = multipartFile.getContentType();
        if (!isSupportedContentType(Objects.requireNonNull(contentType))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Only PNG or JPG images are allowed.")
                    .addConstraintViolation();

            valid = false;
        }

        return valid;
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
    }
}
