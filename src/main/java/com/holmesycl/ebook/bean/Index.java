package com.holmesycl.ebook.bean;

import java.util.ArrayList;
import java.util.List;

public class Index {

    private String name = "index";

    private List<ChapterReference> items = new ArrayList<ChapterReference>();

    public List<ChapterReference> getItems() {
        return items;
    }

    public void setItems(List<ChapterReference> items) {
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Index{" +
                "name='" + name + '\'' +
                ", items=" + items +
                '}';
    }
}
