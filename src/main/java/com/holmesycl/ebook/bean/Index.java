package com.holmesycl.ebook.bean;

import java.util.ArrayList;
import java.util.List;

public class Index extends Page {

    private List<ChapterItem> items = new ArrayList<ChapterItem>();

    public void addChapterItem(ChapterItem chapterItem) {
        this.items.add(chapterItem);
    }

    public List<ChapterItem> getItems() {
        return items;
    }

    public void setItems(List<ChapterItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Index{" +
                "items=" + items +
                '}';
    }
}
