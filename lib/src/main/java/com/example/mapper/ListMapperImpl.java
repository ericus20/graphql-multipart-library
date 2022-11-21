package com.example.mapper;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

class ListMapperImpl implements Mapper<List<Object>> {
    @Override
    public Object set(List<Object> location, String target, MultipartFile value) {
        return location.set(Integer.parseInt(target), value);
    }

    @Override
    public Object recurse(List<Object> location, String target) {
        return location.get(Integer.parseInt(target));
    }
}
