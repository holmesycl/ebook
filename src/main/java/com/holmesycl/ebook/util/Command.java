package com.holmesycl.ebook.util;

import java.io.IOException;

public interface Command<T> {
    public T execute() throws IOException;
}
