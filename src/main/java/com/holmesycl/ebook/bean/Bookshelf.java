package com.holmesycl.ebook.bean;

public class Bookshelf {

    private String imgUrl;

    private long bookId;

    private String bookName;

    private Chapter firstChapter;

    private String author;

    private Chapter lastChapter;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Chapter getFirstChapter() {
        return firstChapter;
    }

    public void setFirstChapter(Chapter firstChapter) {
        this.firstChapter = firstChapter;
    }

    public Chapter getLastChapter() {
        return lastChapter;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public void setLastChapter(Chapter lastChapter) {
        this.lastChapter = lastChapter;
    }

    /**
     * <li class="media" onclick="viewBook('')">
     * <div class="media-left">
     * <img style="height: 80px;" class="media-object"
     * src="https://www.biqugezw.com/files/article/image/9/9767/9767s.jpg" alt="放开那女巫">
     * </div>
     * <div class="media-body">
     * <h3 class="media-heading">放开那女巫</h3>
     * <p class="text-muted">二目</p>
     * <p class="text-muted">第二十章 怎么又是你</p>
     * </div>
     * </li>
     *
     * @return
     */
    public String html() {
        String htmlTemplate = "<li class=\"media\" onclick=\"viewBook('%s', '%s')\">\n" +
                "            <div class=\"media-left\">\n" +
                "                <img style=\"height: 80px;\" class=\"media-object\"\n" +
                "                     src=\"%s\" alt=\"%s\">\n" +
                "            </div>\n" +
                "            <div class=\"media-body\">\n" +
                "                <h3 class=\"media-heading\">%s</h3>\n" +
                "                <p class=\"text-muted\">%s</p>\n" +
                "                <p class=\"text-muted\">%s</p>\n" +
                "            </div>\n" +
                "        </li>";
        return String.format(htmlTemplate, bookId, firstChapter.getResourceId(), imgUrl, bookName, bookName, author, lastChapter.getResourceName());
    }
}
