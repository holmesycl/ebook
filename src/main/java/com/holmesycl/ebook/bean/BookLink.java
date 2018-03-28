package com.holmesycl.ebook.bean;

public class BookLink {
    private int id;
    private String url;

    public BookLink(String url) {
        this.url = url;
    }

    public int getId() {
        if (this.id == 0) {
            int beginIndex = this.url.lastIndexOf("_") + 1;
            int endIndex = this.url.lastIndexOf("/");
            if (endIndex < beginIndex) {
                endIndex = url.length();
            }
            this.id = Integer.parseInt(this.url.substring(beginIndex, endIndex));
        }
        return this.id;
    }

    public String getUrl() {
        return url;
    }

    public static void main(String[] args) {
        System.out.println(new BookLink("http://www.biqugezw.com/18_18571").getId());
    }
}
