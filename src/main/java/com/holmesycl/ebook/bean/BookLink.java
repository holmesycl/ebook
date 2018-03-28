package com.holmesycl.ebook.bean;

public class BookLink {

    private String bookName;
    private String linkUrl;

    public BookLink() {
    }

    public BookLink(String bookName, String linkUrl) {
        this.bookName = bookName;
        this.linkUrl = linkUrl;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public int getBookId() {
        String temp = this.linkUrl.replaceAll("/", "");
        temp = temp.replaceAll("_", "");
        temp = temp.substring(temp.lastIndexOf("com") + 3);
        return Integer.parseInt(temp);
    }

    @Override
    public String toString() {
        return "BookLink{" +
                "bookName='" + bookName + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                '}';
    }
}
