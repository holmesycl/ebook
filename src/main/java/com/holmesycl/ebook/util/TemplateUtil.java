package com.holmesycl.ebook.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TemplateUtil {

    public static String index() throws IOException {
        return FileUtils.readFileToString(getResourceFile("index.html"), "utf-8");
    }

    public static String chapter() throws IOException {
        return FileUtils.readFileToString(getResourceFile("chapter.html"), "utf-8");
    }

    public static File getResourceFile(String fileName) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());
    }
}
