package com.holmesycl.ebook.bean;

import java.io.Serializable;
import java.util.Date;

public class BookChapter implements Serializable {
    private int bookId;
    private int pageNumber;
    private int previousChapter;
    private int nextChapter;
    private String title;
    private String content;
    private Date createDate;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPreviousChapter() {
        return previousChapter;
    }

    public void setPreviousChapter(int previousChapter) {
        this.previousChapter = previousChapter;
    }

    public int getNextChapter() {
        return nextChapter;
    }

    public void setNextChapter(int nextChapter) {
        this.nextChapter = nextChapter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "BookChapter{" +
                "bookId=" + bookId +
                ", pageNumber=" + pageNumber +
                ", previousChapter=" + previousChapter +
                ", nextChapter=" + nextChapter +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
