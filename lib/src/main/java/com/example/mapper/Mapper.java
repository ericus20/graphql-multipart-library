package com.example.mapper;

import org.springframework.web.multipart.MultipartFile;

interface Mapper<T> {

    Object set(final T location, final String target, final MultipartFile value);

    Object recurse(final T location, final String target);
}
