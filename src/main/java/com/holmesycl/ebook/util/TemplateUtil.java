package com.holmesycl.ebook.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class TemplateUtil {

    public static String index() throws IOException {
        return resourceToString("index.html");
    }

    public static String chapter() throws IOException {
        return resourceToString("chapter.html");
    }

    public static String bookshelf() throws IOException {
        return resourceToString("bookshelf.html");
    }

    public static String resourceToString(String fileName) throws IOException {
        return IOUtils.resourceToString(fileName, Charset.forName("utf-8"), ClassLoader.getSystemClassLoader());
    }
}
