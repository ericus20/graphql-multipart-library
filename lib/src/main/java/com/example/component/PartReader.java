package com.example.component;

import javax.servlet.http.Part;
import java.lang.reflect.Type;

public interface PartReader {
    <T> T readPart(Part part, Type targetType);
}
