package com.holmesycl.ebook.bean;

public class ChapterLink {

    private String chapterTitle;
    private ChapterLink prefixChapterLink;
    private String chapterLink;
    private ChapterLink nextChapterLink;

    public ChapterLink() {
    }

    public ChapterLink(String chapterLink) {
        this.chapterLink = chapterLink;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public String getChapterLink() {
        return chapterLink;
    }

    public void setChapterLink(String chapterLink) {
        this.chapterLink = chapterLink;
    }

    public int getPageNumber() {
        return Integer.parseInt(chapterLink.substring(chapterLink.lastIndexOf("/") + 1, chapterLink.lastIndexOf(".")));
    }

    @Override
    public String toString() {
        return "ChapterLink{" +
                "chapterTitle='" + chapterTitle + '\'' +
                ", prefixChapterLink=" + prefixChapterLink +
                ", chapterLink='" + chapterLink + '\'' +
                ", nextChapterLink=" + nextChapterLink +
                '}';
    }

    public ChapterLink getPrefixChapterLink() {
        return prefixChapterLink;
    }

    public void setPrefixChapterLink(ChapterLink prefixChapterLink) {
        this.prefixChapterLink = prefixChapterLink;
    }

    public ChapterLink getNextChapterLink() {
        return nextChapterLink;
    }

    public void setNextChapterLink(ChapterLink nextChapterLink) {
        this.nextChapterLink = nextChapterLink;
    }
}
