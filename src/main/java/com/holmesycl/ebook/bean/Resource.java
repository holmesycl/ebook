package com.holmesycl.ebook.bean;

public abstract class Resource {

    private String resourceName;

    private String resourceUrl;

    public Resource() {
    }

    public Resource(String resourceName, String resourceUrl) {
        this.resourceName = resourceName;
        this.resourceUrl = resourceUrl;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public abstract long getResourceId();

}
