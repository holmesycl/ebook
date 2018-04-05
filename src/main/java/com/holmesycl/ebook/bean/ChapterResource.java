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

    @Override
    public long getResourceId() {
        // https://www.biqugezw.com/18_18571/3143927.html
        String temp = getResourceUrl();
        temp = temp.replaceAll(".html", "");
        temp = temp.replaceAll("_", "");
        temp = temp.replaceAll("/", "");
        return Long.parseLong(temp.substring(temp.lastIndexOf(".com") + 4));
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
