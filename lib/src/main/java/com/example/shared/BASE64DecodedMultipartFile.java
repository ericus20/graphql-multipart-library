package com.example.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLConnection;
import java.util.Objects;

@Slf4j
public class BASE64DecodedMultipartFile implements MultipartFile {

    private final byte[] imgContent;
    private final String fileName;
    private final String ext;

    public BASE64DecodedMultipartFile(byte[] imgContent, String fileName) {
        this(imgContent, fileName, null);
    }

    public BASE64DecodedMultipartFile(byte[] imgContent, String fileName, String ext) {
        this.imgContent = imgContent;
        this.fileName = fileName;
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    @NonNull
    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        if (Objects.isNull(getExt())) {
            return URLConnection.getFileNameMap().getContentTypeFor(fileName);
        }

        return getExt();
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @NonNull
    @Override
    public byte[] getBytes() {
        return imgContent;
    }

    @NonNull
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException {
        try (var outputStream = new FileOutputStream(dest)) {
            outputStream.write(imgContent);
        }
    }
}
