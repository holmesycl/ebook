package com.holmesycl.ebook.bean;

import java.io.Serializable;
import java.util.Date;

public class BookIndex implements Serializable {

    private int bookId;
    private int pageNumber;
    private String title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "BookIndex{" +
                "bookId=" + bookId +
                ", pageNumber=" + pageNumber +
                ", title='" + title + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
