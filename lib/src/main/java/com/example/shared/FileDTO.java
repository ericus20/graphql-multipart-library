package com.example.shared;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
@Getter
public class FileDTO {
    private final File file;
    private final byte[] contents;

    public FileDTO(String file) {
        this.file = new File(file);
        this.contents = file.getBytes(StandardCharsets.UTF_8);
    }

    public MultipartFile toMultipartFile() {
        return new BASE64DecodedMultipartFile(contents, file.getName());
    }
}
