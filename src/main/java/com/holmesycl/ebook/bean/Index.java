package com.holmesycl.ebook.bean;

import java.util.ArrayList;
import java.util.List;

public class Index {

    private List<ChapterReference> items = new ArrayList<ChapterReference>();

    public void addChapterItem(ChapterReference chapterItem) {
        this.items.add(chapterItem);
    }

    public List<ChapterReference> getItems() {
        return items;
    }

    public void setItems(List<ChapterReference> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Index{" +
                "items=" + items +
                '}';
    }
}
