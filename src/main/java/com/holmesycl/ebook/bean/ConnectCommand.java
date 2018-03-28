package com.holmesycl.ebook.bean;

import com.holmesycl.ebook.util.Command;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class ConnectCommand implements Command<Document> {

    private String url;

    public ConnectCommand(String url) {
        this.url = url;
    }

    @Override
    public Document execute() throws IOException {
        return Jsoup.connect(url).get();
    }
}
