package com.example.mapper;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

class MapMapperImpl implements Mapper<Map<String, Object>> {
    @Override
    public Object set(Map<String, Object> location, String target, MultipartFile value) {
        return location.put(target, value);
    }

    @Override
    public Object recurse(Map<String, Object> location, String target) {
        return location.get(target);
    }
}
