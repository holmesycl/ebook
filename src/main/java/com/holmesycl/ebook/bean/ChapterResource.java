package com.holmesycl.ebook.bean;

public class ChapterResource extends Resource {

    private ChapterResource prefixChapterResource;

    private ChapterResource nextChapterResource;

    public ChapterResource() {
    }

    public ChapterResource(String resourceName, String resourceUrl) {
        super(resourceName, resourceUrl);
        this.prefixChapterResource = prefixChapterResource;
        this.nextChapterResource = nextChapterResource;
    }

    public ChapterResource getPrefixChapterResource() {
        return prefixChapterResource;
    }

    public void setPrefixChapterResource(ChapterResource prefixChapterResource) {
        this.prefixChapterResource = prefixChapterResource;
    }

    public ChapterResource getNextChapterResource() {
        return nextChapterResource;
    }

    public void setNextChapterResource(ChapterResource nextChapterResource) {
        this.nextChapterResource = nextChapterResource;
    }
}
